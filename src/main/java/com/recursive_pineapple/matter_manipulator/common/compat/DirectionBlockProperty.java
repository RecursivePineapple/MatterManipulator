package com.recursive_pineapple.matter_manipulator.common.compat;

import static net.minecraftforge.common.util.ForgeDirection.*;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

public interface DirectionBlockProperty extends BlockProperty<ForgeDirection> {

    default DirectionBlockProperty setName(String name) {
        return this;
    }

    @Override
    default ForgeDirection parse(String text) throws Exception {
        return ForgeDirection.valueOf(text.toUpperCase());
    }

    @Override
    default String stringify(ForgeDirection value) {
        return value.name()
            .toLowerCase();
    }

    public static abstract class AbstractDirectionBlockProperty implements DirectionBlockProperty {

        private String name = "facing";

        @Override
        public DirectionBlockProperty setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String getName() {
            return name;
        }

        public AbstractDirectionBlockProperty(String name) {
            this.name = name;
        }
    }

    public static DirectionBlockProperty facing() {
        return new AbstractDirectionBlockProperty("facing") {

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                return MMUtils.getIndexSafe(ForgeDirection.VALID_DIRECTIONS, world.getBlockMetadata(x, y, z));
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                world.setBlockMetadataWithNotify(x, y, z, value.ordinal(), 2);
            }
        };
    }

    public static DirectionBlockProperty facingVanilla(int mask) {
        return facing(mask, dir -> switch (dir) {
            case NORTH -> 3;
            case SOUTH -> 4;
            case WEST -> 1;
            case EAST -> 2;
            case UP -> 0;
            case DOWN -> 5;
            default -> 3;
        }, meta -> switch (meta) {
            case 3 -> NORTH;
            case 4 -> SOUTH;
            case 1 -> WEST;
            case 2 -> EAST;
            case 0 -> UP;
            case 5 -> DOWN;
            default -> NORTH;
        });
    }

    public static interface D2M {

        int getMeta(ForgeDirection dir);
    }

    public static interface M2D {

        ForgeDirection getDir(int meta);
    }

    public static DirectionBlockProperty facing(int mask, D2M toMeta, M2D toDir) {
        return new AbstractDirectionBlockProperty("facing") {

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                return toDir.getDir(world.getBlockMetadata(x, y, z) & mask);
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                int meta = 0;
                if (mask != -1) {
                    meta = world.getBlockMetadata(x, y, z) & ~mask;
                }

                world.setBlockMetadataWithNotify(x, y, z, toMeta.getMeta(value) | meta, 2);
            }
        };
    }

    public static interface D2M2 {

        int getMeta(ForgeDirection dir, int existing);
    }

    public static DirectionBlockProperty facing(D2M2 toMeta, M2D toDir) {
        return new AbstractDirectionBlockProperty("facing") {

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                return toDir.getDir(world.getBlockMetadata(x, y, z));
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                int meta = world.getBlockMetadata(x, y, z);

                world.setBlockMetadataWithNotify(x, y, z, toMeta.getMeta(value, meta), 2);
            }
        };
    }

    public static DirectionBlockProperty facing(int mask, int north, int south, int west, int east, int up, int down) {
        return facing(mask, dir -> switch (dir) {
            case NORTH -> north == -1 ? 0 : north;
            case SOUTH -> south == -1 ? 0 : south;
            case WEST -> west == -1 ? 0 : west;
            case EAST -> east == -1 ? 0 : east;
            case UP -> up == -1 ? 0 : up;
            case DOWN -> down == -1 ? 0 : down;
            case UNKNOWN -> 0;
        }, meta -> {
            if (meta == north) return NORTH;
            if (meta == south) return SOUTH;
            if (meta == west) return WEST;
            if (meta == east) return EAST;
            if (meta == up) return UP;
            if (meta == down) return DOWN;

            return UNKNOWN;
        });
    }
}
