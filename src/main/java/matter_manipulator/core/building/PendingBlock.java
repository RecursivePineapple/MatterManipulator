package matter_manipulator.core.building;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import matter_manipulator.common.networking.MMPacketBuffer;
import matter_manipulator.common.utils.math.Location;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.persist.NBTPersist;

/**
 * This represents a block in the world.
 * It stores everything the building algorithm needs to know to place a block.
 */
@EqualsAndHashCode(callSuper = true)
public class PendingBlock extends Location {

    public IBlockSpec spec;

    public PendingBlock() {

    }

    public PendingBlock(World world, int x, int y, int z, @NotNull IBlockSpec spec) {
        super(world.provider.getDimension(), x, y, z);
        this.spec = spec;
    }

    public PendingBlock(int worldId, int x, int y, int z, @NotNull IBlockSpec spec) {
        super(worldId, x, y, z);
        this.spec = spec;
    }

    public PendingBlock(World world, Vector3i voxel, IBlockSpec spec) {
        this(world.provider.getDimension(), voxel.x, voxel.y, voxel.z, spec);
    }

    public IBlockState getBlockState() {
        return spec.getBlockState();
    }

    public Block getBlock() {
        return spec.getBlockState().getBlock();
    }

    public PendingBlock clone() {
        return new PendingBlock(worldId, x, y, z, spec.clone());
    }

    public void encode(MMPacketBuffer buffer) {
        buffer.writeInt(this.worldId);
        buffer.writeBlockPos(this.toPos());
        buffer.writeBSON((JsonObject) NBTPersist.GSON.toJsonTree(this.spec));
    }

    public void decode(MMPacketBuffer buffer) {
        this.worldId = buffer.readInt();

        BlockPos pos = buffer.readBlockPos();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();

        this.spec = NBTPersist.GSON.fromJson(buffer.readBSON(), IBlockSpec.class);
    }

    public static PendingBlock decodeNew(MMPacketBuffer buffer) {
        PendingBlock block = new PendingBlock();
        block.decode(buffer);
        return block;
    }

//    public static Comparator<IBlockSpec> getBlockSpecComparator() {
//        return Comparator.comparing(IBlockSpec::getStack, (a, b) -> {
//            if (a.isEmpty() && !b.isEmpty()) return -1;
//            if (!a.isEmpty() && b.isEmpty()) return 1;
//            if (a.isEmpty() && b.isEmpty()) return 0;
//
//            int result;
//
//            result = String.CASE_INSENSITIVE_ORDER.compare(a.getItem().delegate.name().toString(), b.getItem().delegate.name().toString());
//            if (result != 0) return result;
//
//            result = Integer.compare(Items.FEATHER.getDamage(a), Items.FEATHER.getDamage(b));
//            if (result != 0) return result;
//
//            NBTTagCompound ta = a.getTagCompound();
//            NBTTagCompound tb = b.getTagCompound();
//
//            if (ta == null && tb != null) return -1;
//            if (ta != null && tb == null) return 1;
//            if (ta == null && tb == null) return 0;
//
//            return Integer.compare(ta.hashCode(), tb.hashCode());
//        });
//    }

    /// A comparator that makes the building process look cool, at the expense of being slow.
//    public static Comparator<PendingBlock> getCoolComparator() {
//        return Comparator.<PendingBlock, IBlockSpec>comparing(b -> b.spec, getBlockSpecComparator())
//            .thenComparingInt(Location::getChunkX)
//            .thenComparingInt(Location::getChunkZ)
//            .thenComparingInt(Location::x)
//            .thenComparingInt(Location::y)
//            .thenComparingInt(Location::z);
//    }

    /// A comparator that makes the building process faster, but looks less cool.
//    public static Comparator<PendingBlock> getFastComparator() {
//        return Comparator.<PendingBlock>comparingInt(Location::getChunkX)
//            .thenComparingInt(Location::getChunkZ)
//            .thenComparingInt(Location::x)
//            .thenComparingInt(Location::y)
//            .thenComparingInt(Location::z);
//    }
}
