package com.recursive_pineapple.matter_manipulator.common.data;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.ImmutableBlockSpec;

import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;

public class WeightedSpecList {

    public ArrayList<ObjectIntMutablePair<BlockSpec>> specs = new ArrayList<>();

    public WeightedSpecList(BlockSpec... values) {
        for (BlockSpec spec : values) {
            add(spec);
        }
    }

    public void add(BlockSpec spec) {
        for (var p : specs) {
            if (Objects.equals(p.first(), spec)) {
                p.right(p.rightInt() + 1);
                return;
            }
        }

        specs.add(ObjectIntMutablePair.of(spec, 1));
    }

    public ImmutableBlockSpec get(Random rng) {
        if (specs.isEmpty()) return BlockSpec.AIR;

        int sum = 0;

        for (var p : specs) sum += p.rightInt();

        if (sum == 0) return BlockSpec.AIR;

        int selector = rng.nextInt(sum);

        for (var p : specs) {
            if (selector < p.rightInt()) return p.left();

            selector -= p.rightInt();
        }

        return BlockSpec.AIR;
    }

    public boolean contains(BlockSpec spec) {
        for (var p : specs) {
            if (Objects.equals(p.first(), spec)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (var p : specs) {
            if (sb.length() > 0) sb.append(", ");

            sb.append(
                p.left()
                    .getDisplayName());

            if (p.rightInt() > 1) sb.append(" x ")
                .append(p.rightInt());
        }

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((specs == null) ? 0 : specs.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        WeightedSpecList other = (WeightedSpecList) obj;
        if (specs == null) {
            if (other.specs != null) return false;
        } else if (!specs.equals(other.specs)) return false;
        return true;
    }
}
