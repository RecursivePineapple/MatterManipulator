package matter_manipulator.core.analysis;

import java.lang.reflect.Type;
import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.persist.NBTPersist;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemResourceProvider;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class InventoryAnalysis {

    public Int2ObjectOpenHashMap<ItemStack> slots = new Int2ObjectOpenHashMap<>();

    public EnumSet<ApplyResult> apply(BlockPlacingContext context, InventoryAdapter<IntItemResourceStack> target, ApplyMode applyMode) {
        EnumSet<ApplyResult> result = EnumSet.noneOf(ApplyResult.class);

        validateInventory(context, target, result);

        if (result.contains(ApplyResult.NotApplicable)) {
            return result;
        }

        IntIterator iter = target.getSlots().iterator();

        ItemResourceProvider provider = context.resource(ItemResource.ITEMS);

        while (iter.hasNext()) {
            int i = iter.nextInt();

            ItemStackWrapper desired = new ItemStackWrapper(this.slots.get(i));
            IntItemResourceStack actual = target.getStackInSlot(i);

            if (!actual.matches(desired) || desired.getAmountInt() != actual.getAmountInt()) {
                if (!target.canExtract(i)) {
                    context.warn(new Localized("mm.chat.inventory-no-extract", actual.toStack(), i));
                    result.add(ApplyResult.NotApplicable);
                    continue;
                }

                if (!target.canInsert(i, desired)) {
                    context.warn(new Localized("mm.chat.inventory-no-insert", desired.toStack(), i));
                    result.add(ApplyResult.NotApplicable);
                    continue;
                }

                if (applyMode.doIO() && !provider.canExtract(desired)) {
                    context.extractionFailure(desired);
                    result.add(ApplyResult.Retry);
                    continue;
                }

                IntItemResourceStack existing;

                if (!applyMode.isSimulate() && applyMode.clearExisting()) {
                    existing = target.extract(i);
                } else {
                    existing = actual.copy();
                }

                if (applyMode.doIO() && existing != null) {
                    provider.insert(existing);
                }

                IntItemResourceStack extracted = !applyMode.doIO() ? desired.copy() : provider.tryExtract(desired);

                if (extracted == null) {
                    context.extractionFailure(desired);
                    result.add(ApplyResult.Retry);
                    continue;
                } else if (!extracted.isEmpty()) {
                    if (!applyMode.isSimulate()) {
                        extracted = target.insert(i, extracted);
                    } else {
                        extracted = IntItemResourceStack.EMPTY;
                    }
                }

                if (applyMode.doIO()) {
                    if (extracted != null && !extracted.isEmpty()) {
                        provider.insert(extracted);
                    }
                }
            }
        }

        return result;
    }

    private void validateInventory(BlockPlacingContext context,
        InventoryAdapter<IntItemResourceStack> target, EnumSet<ApplyResult> result) {
        if (this.slots.size() != target.getSlots().size()) {
            result.add(ApplyResult.NotApplicable);
            context.warn(new Localized("mm.chat.wrong-slot-count", this.slots.size(), target.getSlots().size()));
            return;
        }

        IntOpenHashSet slotsUnioned = new IntOpenHashSet();

        slotsUnioned.addAll(this.slots.keySet());

        IntOpenHashSet targetSlots = new IntOpenHashSet(target.getSlots());
        slotsUnioned.addAll(targetSlots);

        slotsUnioned.forEach(slot -> {
            boolean inInv = targetSlots.contains(slot);
            boolean inAnalysis = this.slots.containsKey(slot);

            if (!inInv && inAnalysis) {
                context.warn(new Localized("mm.chat.missing-slot-inventory", slot, this.slots.get(slot).copy()));
                result.add(ApplyResult.NotApplicable);
            } else if (inInv && !inAnalysis) {
                context.warn(new Localized("mm.chat.missing-slot-analysis", slot, target.getStackInSlot(slot).toStack()));
                result.add(ApplyResult.NotApplicable);
            }
        });
    }

    public JsonObject save() {
        JsonObject obj = new JsonObject();

        Int2ObjectMaps.fastForEach(this.slots, e -> {
            obj.add(Integer.toString(e.getIntKey()), NBTPersist.GSON.toJsonTree(e.getValue()));
        });

        return obj;
    }

    public void load(JsonObject obj) throws JsonParseException {
        this.slots.clear();
        this.slots.ensureCapacity(obj.size());

        obj.entrySet().forEach(e -> {
            try {
                int i = Integer.parseInt(e.getKey());

                this.slots.put(i, NBTPersist.GSON.fromJson(e.getValue(), ItemStack.class));
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Invalid slot index: " + e.getKey(), ex);
            }
        });
    }

    public static InventoryAnalysis fromInventory(InventoryAdapter<? extends IntItemResourceStack> inv) {
        InventoryAnalysis analysis = new InventoryAnalysis();

        inv.getSlots().forEach(i -> {
            analysis.slots.put(i, inv.getStackInSlot(i).toStack());
        });

        return analysis;
    }

    public static InventoryAnalysis fromInventory(IItemHandler inv) {
        InventoryAnalysis analysis = new InventoryAnalysis();

        final int count = inv.getSlots();

        for (int i = 0; i < count; i++) {
            analysis.slots.put(i, inv.getStackInSlot(i));
        }

        return analysis;
    }

    public static class InventoryAnalysisJsonAdapter implements JsonSerializer<InventoryAnalysis>, JsonDeserializer<InventoryAnalysis> {

        @Override
        public InventoryAnalysis deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!(json instanceof JsonObject obj)) throw new JsonParseException("Expected object: " + json);

            InventoryAnalysis analysis = new InventoryAnalysis();

            analysis.load(obj);

            return analysis;
        }

        @Override
        public JsonElement serialize(InventoryAnalysis src, Type typeOfSrc, JsonSerializationContext context) {
            return src.save();
        }
    }
}
