package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.MMMod;
import matter_manipulator.common.utils.DataUtils;

public class BlockStateJsonAdapter implements JsonSerializer<IBlockState>, JsonDeserializer<IBlockState> {

    @Override
    public IBlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String[] halves = json.getAsString().split("\\{", 2);

        Block block = Block.getBlockFromName(halves[0]);

        if (block == null) {
            //noinspection DataFlowIssue
            return Blocks.AIR.getDefaultState();
        }

        IBlockState state = block.getDefaultState();

        if (halves.length > 1) {
            String props = halves[1].substring(0, halves[1].length() - 1);

            for (String entry : props.split(",")) {
                String[] kv = entry.split("=");

                IProperty<?> prop = DataUtils.find(state.getPropertyKeys(), p2 -> p2.getName().equals(kv[0]));

                if (prop == null) {
                    MMMod.LOG.warn("Tried to set invalid property {} ({}) on block {}", kv[0], kv[1], block);
                    continue;
                }

                state = mutate(state, prop, kv[1]);
            }
        }

        return state;
    }

    private <T extends Comparable<T>, V extends T> IBlockState mutate(IBlockState state, IProperty<T> prop, String value) {
        Optional<@NotNull T> parsed = prop.parseValue(value);

        if (!parsed.isPresent()) {
            MMMod.LOG.warn("Tried to set invalid property {} ({}) on block {}", prop.getName(), value, state.getBlock());
            return state;
        }

        return state.withProperty(prop, parsed.get());
    }

    @Override
    public JsonElement serialize(IBlockState src, Type typeOfSrc, JsonSerializationContext context) {
        StringBuilder sb = new StringBuilder();

        sb.append(src.getBlock().getRegistryName());

        if (!src.getPropertyKeys().isEmpty()) {
            sb.append("{");

            boolean first = true;

            //noinspection rawtypes
            for (IProperty key : src.getPropertyKeys()) {
                if (!first) {
                    sb.append(",");
                }

                first = false;

                sb.append(key.getName());
                sb.append("=");
                //noinspection unchecked
                sb.append(key.getName(src.getValue(key)));
            }

            sb.append("}");
        }

        return new JsonPrimitive(sb.toString());
    }
}
