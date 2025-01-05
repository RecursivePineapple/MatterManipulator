package com.recursive_pineapple.matter_manipulator.common.utils;

import net.minecraft.block.Block;

public class BlockMeta implements ImmutableBlockMeta {

    public Block block;
    public int meta;

    public BlockMeta() {}

    public BlockMeta(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public int getMeta() {
        return meta;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((block == null) ? 0 : block.hashCode());
        result = prime * result + meta;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        BlockMeta other = (BlockMeta) obj;
        if (block == null) {
            if (other.block != null) return false;
        } else if (!block.equals(other.block)) return false;
        if (meta != other.meta) return false;
        return true;
    }

    @Override
    public String toString() {
        return "BlockMeta [block=" + block + ", meta=" + meta + "]";
    }
}
