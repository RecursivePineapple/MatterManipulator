package matter_manipulator.common.utils.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Optional.Method;

import com.cleanroommc.modularui.utils.fakeworld.DummyWorld;
import dev.redstudio.alfheim.lighting.LightingEngine;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import matter_manipulator.common.utils.Mods;
import matter_manipulator.mixin.mixins.minecraft.AccessorWorld;

@SuppressWarnings("NullableProblems")
public class ProxiedWorld extends DummyWorld {

    private final World base;

    public final Long2ObjectOpenHashMap<IBlockState> overrides = new Long2ObjectOpenHashMap<>();

    public ProxiedWorld(World base) {
        super();

        this.base = base;

        // De-allocate alfheim lighting engine
        if (Mods.Alfheim.isModLoaded()) {
            ObfuscationReflectionHelper.setPrivateValue(World.class, this, null,
                "alfheim$lightingEngine");
        }
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return ((AccessorWorld) base).mm$isChunkLoaded(x, z, allowEmpty);
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        IBlockState override = overrides.get(pos.toLong());

        if (override != null) return override;

        return base.getBlockState(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        overrides.put(pos.toLong(), newState);

        return true;
    }

    @Override
    @Method(modid = Mods.Names.ALFHEIM)
    public World init() {
        return this;
    }

    @Override
    @Method(modid = Mods.Names.ALFHEIM)
    public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
        return 15;
    }

    @Method(modid = Mods.Names.ALFHEIM)
    public int alfheim$getLight(BlockPos pos, boolean checkNeighbors) {
        return 15;
    }

    @Method(modid = Mods.Names.ALFHEIM)
    public LightingEngine getAlfheim$lightingEngine() {
        return null;
    }
}
