package matter_manipulator.common.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.joml.Vector3d;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import matter_manipulator.MMMod;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.items.MMUpgrades;
import matter_manipulator.common.networking.SoundResource;
import matter_manipulator.common.state.MMState;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.i18n.MMTextBuilder;
import matter_manipulator.core.interop.interfaces.BlockResetter;
import matter_manipulator.core.manipulator_resource.EnergyManipulatorResource;
import matter_manipulator.core.manipulator_resource.ManipulatorResource;
import matter_manipulator.core.misc.BuildFeedback;
import matter_manipulator.core.misc.FeedbackSeverity;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIdentity;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.ResourceStack.IntResourceStack;
import matter_manipulator.core.resources.ResourceStack.LongResourceStack;
import matter_manipulator.core.resources.ResourceTrait;

public class BuildingContextImpl extends ManipulatorContextImpl implements BlockPlacingContext {

    private final Map<Resource<?>, ResourceProvider> cachedProviders = new Object2ObjectArrayMap<>();

    public BlockPos pos;
    public IBlockSpec spec;
    public double dist, distEUMult;

    public List<BuildFeedback> feedback = new ArrayList<>(0);

    /// TODO: turn this into a config or something
    protected static final double BASE_EU_COST = 128.0, EU_DISTANCE_EXP = 1.25;

    private final HashMap<Pair<SoundResource, World>, SoundInfo> pendingSounds = new HashMap<>();

    private final Object2LongOpenHashMap<ResourceIdentity> extractionFailures = new Object2LongOpenHashMap<>();

    private static class SoundInfo {

        private int eventCount;
        private double sumX, sumY, sumZ;
    }

    public BuildingContextImpl(World world, EntityPlayerMP player, ItemStack manipulator, MMState state) {
        super(world, player, manipulator, state);
    }

    @Override
    public void setTarget(BlockPos pos, IBlockSpec spec) {
        this.pos = pos;
        this.spec = spec;

        double dx = pos.getX() - player.posX;
        double dy = pos.getY() - player.posY;
        double dz = pos.getZ() - player.posZ;

        this.dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        this.distEUMult = Math.pow(this.dist, EU_DISTANCE_EXP);
    }

    @Override
    public IBlockSpec getSpec() {
        return spec;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public <P extends ResourceProvider> P resource(Resource<P> resource) {
        var cached = cachedProviders.get(resource);

        if (cached != null) {
            //noinspection unchecked
            return (P) cached;
        }

        @SuppressWarnings("unchecked")
        ResourceProviderFactory<P> factory = MMRegistriesInternal.RESOURCES.get(resource);

        if (factory == null) {
            MMMod.LOG.error("Tried to get a ResourceProvider for a Resource that does not have a registered ResourceProviderFactory: {}", resource);
            return null;
        }

        P provider = factory.createProvider(this);

        cachedProviders.put(resource, provider);

        return provider;
    }

    @Override
    public boolean drainEnergy(double multiplier) {
        if (getRealPlayer().capabilities.isCreativeMode) return true;

        double cost = BASE_EU_COST * multiplier * distEUMult;

        if (hasUpgrade(MMUpgrades.PowerEff)) {
            cost *= 0.5;
        }

        for (ManipulatorResource res : state.getResources(this).values()) {
            if (res instanceof EnergyManipulatorResource energy) {
                cost -= energy.extract(cost);

                if (cost <= 0.001) return true;
            }
        }

        return false;
    }

    @Override
    public boolean drainEnergy(BlockPos pos, double multiplier) {
        if (getRealPlayer().capabilities.isCreativeMode) return true;

        double dx = pos.getX() - player.posX;
        double dy = pos.getY() - player.posY;
        double dz = pos.getZ() - player.posZ;

        this.dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double distEUMult = Math.pow(this.dist, EU_DISTANCE_EXP);

        double cost = BASE_EU_COST * multiplier * distEUMult;

        if (hasUpgrade(MMUpgrades.PowerEff)) {
            cost *= 0.5;
        }

        for (ManipulatorResource res : state.getResources(this).values()) {
            if (res instanceof EnergyManipulatorResource energy) {
                cost -= energy.extract(cost);

                if (cost <= 0.001) return true;
            }
        }

        return false;
    }

    @Override
    public void removeBlock() {
        if (this.pos == null) throw new IllegalStateException("Target was not set");

        List<ResourceStack> drops = new ArrayList<>();

        for (BlockResetter resetter : MMRegistriesInternal.BLOCK_RESETTERS.sorted()) {
            drops.addAll(resetter.resetBlock(this, this.pos));
        }

        insert(drops);
    }

    @Override
    public void warn(Localized message) {
        feedback.add(new BuildFeedback(this.pos, message, FeedbackSeverity.WARNING));
    }

    @Override
    public void error(Localized message) {
        feedback.add(new BuildFeedback(this.pos, message, FeedbackSeverity.ERROR));
    }

    @Override
    public void playSound(BlockPos pos, SoundResource sound) {
        Pair<SoundResource, World> pair = Pair.of(sound, world);

        SoundInfo info = pendingSounds.computeIfAbsent(pair, ignored -> new SoundInfo());

        info.eventCount++;
        info.sumX += pos.getX();
        info.sumY += pos.getY();
        info.sumZ += pos.getZ();
    }

    @Override
    public void extractionFailure(ResourceStack stack) {
        long amount = 0;

        if (stack.hasTrait(ResourceTrait.LongAmount)) {
            amount = ((LongResourceStack) stack).getAmountLong();
        } else if (stack.hasTrait(ResourceTrait.IntAmount)) {
            amount = ((IntResourceStack) stack).getAmountInt();
        } else {
            throw new IllegalStateException("Resource " + stack + " must have either the IntAmount or the LongAmount resource trait");
        }

        this.extractionFailures.addTo(stack.getIdentity(), amount);
    }

    @Override
    public boolean isSimulation() {
        return false;
    }

    public void onBuildTickFinished() {
        pendingSounds.forEach((pair, info) -> {
            int avgX = (int) (info.sumX / info.eventCount);
            int avgY = (int) (info.sumY / info.eventCount);
            int avgZ = (int) (info.sumZ / info.eventCount);

            float distance = (float) new Vector3d(player.posX - avgX, player.posY - avgY, player.posZ - avgZ).length();

            pair.left().sendPlayToAll((WorldServer) pair.right(), new BlockPos(avgX, avgY, avgZ), (distance / 16f) + 1, -1);
        });

        pendingSounds.clear();

        extractionFailures.object2LongEntrySet().fastForEach(e -> {
            new Localized("mm.info.warning.could_not_find").sendChat(getRealPlayer());
            new MMTextBuilder("mm.info.warning.missing_resource")
                .addLocalized(e.getKey().getName())
                .addNumber(e.getLongValue())
                .toLocalized()
                .sendChat(getRealPlayer());
        });
    }
}
