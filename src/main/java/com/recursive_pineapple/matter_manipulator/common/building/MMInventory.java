package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.gtnhlib.util.map.ItemStackMap;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.entities.EntityItemLarge;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.uplink.IUplinkMulti;
import com.recursive_pineapple.matter_manipulator.common.uplink.UplinkStatus;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.FluidId;
import com.recursive_pineapple.matter_manipulator.common.utils.ItemId;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import it.unimi.dsi.fastutil.Pair;

/**
 * Handles all manipulator-related item sourcing and sinking.
 */
public class MMInventory implements IPseudoInventory {

    public EntityPlayer player;
    public MMState state;
    public ManipulatorTier tier;

    public final HashMap<ItemId, Long> pendingItems = new HashMap<>();
    public final HashMap<FluidId, Long> pendingFluids = new HashMap<>();

    private boolean printedUplinkWarning = false;

    public MMInventory(EntityPlayer player, MMState state, ManipulatorTier tier) {
        this.player = player;
        this.state = state;
        this.tier = tier;
    }

    @Override
    public Pair<Boolean, List<BigItemStack>> tryConsumeItems(List<BigItemStack> items, int flags) {
        if ((flags & CONSUME_IGNORE_CREATIVE) == 0 && player.capabilities.isCreativeMode) {
            return Pair.of(true, items);
        } else {
            List<BigItemStack> simulated = MMUtils.mapToList(items, BigItemStack::copy);
            List<BigItemStack> extracted = new ArrayList<>();

            // the first pass is simulated to make sure the requested items can be provided
            consumeItemsFromPending(simulated, extracted, flags | CONSUME_SIMULATED);
            consumeItemsFromPlayer(simulated, extracted, flags | CONSUME_SIMULATED);
            if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_AE) && Mods.AppliedEnergistics2.isModLoaded()) {
                consumeItemsFromAE(simulated, extracted, flags | CONSUME_SIMULATED);
            }
            if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_UPLINK) && Mods.GregTech.isModLoaded()) {
                consumeItemsFromUplink(simulated, extracted, flags | CONSUME_SIMULATED);
            }

            // if we aren't allowed to partially consume items, make sure everything was consumed
            if ((flags & CONSUME_PARTIAL) == 0) {
                if (simulated.stream()
                    .anyMatch(s -> s.getStackSize() > 0)) {
                    return Pair.of(false, null);
                }
            }

            simulated = MMUtils.mapToList(items, BigItemStack::copy);
            extracted.clear();

            consumeItemsFromPending(simulated, extracted, flags);
            consumeItemsFromPlayer(simulated, extracted, flags);
            if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_AE) && Mods.AppliedEnergistics2.isModLoaded()) {
                consumeItemsFromAE(simulated, extracted, flags);
            }
            if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_UPLINK) && Mods.GregTech.isModLoaded()) {
                consumeItemsFromUplink(simulated, extracted, flags);
            }

            ItemStackMap<BigItemStack> out = new ItemStackMap<>(true);

            for (BigItemStack ex : extracted) {
                ItemStack stack = ex.getItemStack();

                BigItemStack merged = out.get(stack);

                if (merged == null) {
                    merged = ex.copy()
                        .setStackSize(0);
                    out.put(stack, merged);
                }

                merged.incStackSize(ex.getStackSize());
            }

            return Pair.of(true, new ArrayList<>(out.values()));
        }
    }

    @Override
    public void givePlayerItems(ItemStack... items) {
        if (player.capabilities.isCreativeMode) {
            return;
        }

        for (ItemStack item : items) {
            if (item != null && item.getItem() != null) {
                pendingItems.merge(ItemId.create(item), (long) item.stackSize, (Long a, Long b) -> a + b);
            }
        }
    }

    @Override
    public void givePlayerFluids(FluidStack... fluids) {
        if (player.capabilities.isCreativeMode) {
            return;
        }

        for (FluidStack fluid : fluids) {
            if (fluid != null) {
                pendingFluids.merge(FluidId.create(fluid), (long) fluid.amount, (Long a, Long b) -> a + b);
            }
        }
    }

    /**
     * Actually delivers stuff stored in pendingItems/pendingFluids so that the inserts are batched.
     * First tries to insert into AE, then the uplink, then the player's inventory.
     * If items can't be inserted, they're dropped on the ground as an EntityItemLarge.
     * If fluids can't be inserted, they're voided.
     */
    public void actuallyGivePlayerStuff() {
        if (player.capabilities.isCreativeMode) {
            pendingItems.clear();
            pendingFluids.clear();
            return;
        }
        
        boolean hasME;

        if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_AE) && AppliedEnergistics2.isModLoaded()) {
            if (state.encKey != null && !state.hasMEConnection()) {
                state.connectToMESystem();
            }
            hasME = state.hasMEConnection() && state.canInteractWithAE(player);
        } else {
            hasME = false;
        }

        if (tier.hasCap(ItemMatterManipulator.CONNECTS_TO_UPLINK) && GregTech.isModLoaded()) {
            if (state.uplinkAddress != null && !state.hasUplinkConnection()) {
                state.connectToUplink();
            }
        }

        IUplinkMulti uplink = state.uplink;

        pendingItems.forEach((item, amount) -> {
            BigItemStack stack = new BigItemStack(item.getItemStack()).setStackSize(amount);

            if (hasME) {
                injectItemsIntoAE(stack);

                if (stack.getStackSize() == 0) return;
            }

            if (uplink != null) {
                UplinkStatus status = uplink.tryGivePlayerItems(Arrays.asList(stack));

                if (status != UplinkStatus.OK && !printedUplinkWarning) {
                    printedUplinkWarning = true;
                    MMUtils.sendErrorToPlayer(player, "Could not push items to uplink: " + status.toString());
                }

                if (stack.getStackSize() == 0) return;
            }

            while (amount > 0) {
                ItemStack smallStack = stack.remove(item.getItemStack().getMaxStackSize());

                int removed = smallStack.stackSize;

                if (!player.inventory.addItemStackToInventory(smallStack)) {
                    amount += removed;
                    break;
                } else {
                    amount += stack.stackSize;
                }
            }

            for(ItemStack smallStack : stack.toStacks()) {
                player.worldObj.spawnEntityInWorld(
                    new EntityItemLarge(
                        player.worldObj,
                        player.posX,
                        player.posY,
                        player.posZ,
                        smallStack));
            }
        });

        pendingItems.clear();

        pendingFluids.forEach((id, amount) -> {
            BigFluidStack stack = new BigFluidStack(id.getFluidStack()).setStackSize(amount);

            if (hasME) {
                injectFluidsIntoAE(stack);

                if (stack.getStackSize() == 0) return;
            }

            if (uplink != null) {
                UplinkStatus status = uplink.tryGivePlayerFluids(Arrays.asList(stack));

                if (status != UplinkStatus.OK && !printedUplinkWarning) {
                    printedUplinkWarning = true;
                    MMUtils.sendErrorToPlayer(player, "Could not push fluids to uplink: " + status.toString());
                }

                if (stack.getStackSize() == 0) return;
            }

            // this is final because of the lambdas, but its amount field is updated several times
            final FluidStack fluid = id
                .getFluidStack(amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : amount.intValue());

            // spotless:off
            ItemStack idealCell = MMUtils.streamInventory(player.inventory)
                .sorted(Comparator.comparingInt((ItemStack x) -> (
                    x != null && x.getItem() instanceof IFluidContainerItem container ? container.getCapacity(x) : 0
                )))
                .filter(x -> (
                    x != null &&
                    x.getItem() instanceof IFluidContainerItem container &&
                    container.fill(x, fluid, false) == fluid.amount
                ))
                .findFirst()
                .orElse(null);
            // spotless:on

            if (idealCell != null) {
                amount -= ((IFluidContainerItem) idealCell.getItem()).fill(idealCell, fluid.copy(), true);
            }

            if (amount <= 0) {
                return;
            }

            fluid.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : amount.intValue();

            // spotless:off
            List<ItemStack> validCells = MMUtils.streamInventory(player.inventory)
                .filter(x -> (
                    x != null &&
                    x.getItem() instanceof IFluidContainerItem container &&
                    container.fill(x, fluid, false) > 0
                ))
                .collect(Collectors.toList());
            // spotless:on

            for (ItemStack cell : validCells) {
                fluid.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : amount.intValue();
                amount -= ((IFluidContainerItem) cell.getItem()).fill(idealCell, fluid.copy(), true);

                if (amount <= 0) {
                    return;
                }
            }

            if (amount > 0 && !player.capabilities.isCreativeMode) {
                MMUtils.sendWarningToPlayer(player, "Could not find a container for fluid (it was voided): "
                            + amount
                            + "L of "
                            + fluid.getLocalizedName());
            }
        });

        pendingFluids.clear();
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void injectItemsIntoAE(BigItemStack stack) {
        IAEItemStack result = state.storageGrid.getItemInventory()
            .injectItems(stack.getAEItemStack(), Actionable.MODULATE, new PlayerSource(player, state.securityTerminal));

        stack.setStackSize(result != null ? result.getStackSize() : 0);
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void injectFluidsIntoAE(BigFluidStack stack) {
        IAEFluidStack result = state.storageGrid.getFluidInventory()
            .injectItems(stack.getAEFluidStack(), Actionable.MODULATE, new PlayerSource(player, state.securityTerminal));

        stack.setStackSize(result != null ? result.getStackSize() : 0);
    }

    private void consumeItemsFromPending(List<BigItemStack> requestedItems, List<BigItemStack> extractedItems,
        int flags) {
        boolean simulate = (flags & CONSUME_SIMULATED) != 0;
        boolean fuzzy = (flags & CONSUME_FUZZY) != 0;

        for (BigItemStack req : requestedItems) {
            if (req.getStackSize() == 0) {
                continue;
            }

            if (!fuzzy) {
                ItemId id = req.getId();

                Long amtInPending = pendingItems.get(id);

                if (amtInPending == null || amtInPending == 0) {
                    continue;
                }

                long toRemove = Math.min(amtInPending, req.getStackSize());

                extractedItems.add(
                    req.copy()
                        .setStackSize(toRemove));
                amtInPending -= toRemove;
                req.decStackSize(toRemove);

                if (!simulate) {
                    if (amtInPending == 0) {
                        pendingItems.remove(id);
                    } else {
                        pendingItems.put(id, amtInPending);
                    }
                }
            } else {
                var iter = pendingItems.entrySet()
                    .iterator();

                while (iter.hasNext()) {
                    var e = iter.next();

                    if (e.getValue() == null || e.getValue() == 0) {
                        continue;
                    }

                    ItemStack stack = e.getKey()
                        .getItemStack();

                    if (stack.getItem() != req.getItem()) {
                        continue;
                    }

                    if (stack.getHasSubtypes() && Items.feather.getDamage(stack) != req.getItemDamage()) {
                        continue;
                    }

                    long amtInPending = e.getValue();
                    long toRemove = Math.min(amtInPending, req.getStackSize());

                    extractedItems.add(
                        req.copy()
                            .setStackSize(toRemove));
                    amtInPending -= toRemove;
                    req.decStackSize(toRemove);

                    if (!simulate) {
                        if (amtInPending == 0) {
                            iter.remove();
                        } else {
                            e.setValue(amtInPending);
                        }
                    }
                }
            }
        }
    }

    private void consumeItemsFromPlayer(List<BigItemStack> requestedItems, List<BigItemStack> extractedItems,
        int flags) {
        boolean simulate = (flags & CONSUME_SIMULATED) != 0;
        boolean fuzzy = (flags & CONSUME_FUZZY) != 0;

        ItemStack[] inv = player.inventory.mainInventory;

        for (BigItemStack req : requestedItems) {
            if (req.getStackSize() == 0) {
                continue;
            }

            for (int i = 0; i < inv.length; i++) {
                ItemStack slot = inv[i];

                if (req.getStackSize() == 0) {
                    break;
                }

                if (slot == null || slot.getItem() == null || slot.stackSize == 0) {
                    continue;
                }

                ItemStack reqStack = req.getItemStack();

                if (fuzzy ? slot.isItemEqual(reqStack) : MMUtils.areStacksBasicallyEqual(slot, reqStack)) {
                    if (slot.stackSize == 111) {
                        extractedItems.add(new BigItemStack(slot).setStackSize(req.getStackSize()));
                        req.setStackSize(0);
                    } else {
                        int toRemove = Math.min(slot.stackSize, reqStack.stackSize);

                        req.decStackSize(toRemove);
                        extractedItems.add(new BigItemStack(slot).setStackSize(toRemove));

                        if (!simulate) {
                            slot.stackSize -= toRemove;
                            if (slot.stackSize == 0) {
                                inv[i] = null;
                            }
                            player.inventory.markDirty();
                        }
                    }
                }
            }
        }
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private void consumeItemsFromAE(List<BigItemStack> requestedItems, List<BigItemStack> extractedItems, int flags) {
        boolean simulate = (flags & CONSUME_SIMULATED) != 0;
        boolean fuzzy = (flags & CONSUME_FUZZY) != 0;

        if (state.encKey == null) {
            return;
        }

        if (!state.hasMEConnection()) {
            if (!state.connectToMESystem()) {
                return;
            }
        }

        if (!state.canInteractWithAE(player)) {
            return;
        }

        for (BigItemStack req : requestedItems) {
            if (req.getStackSize() == 0) {
                continue;
            }

            IAEItemStack aeReq = req.getAEItemStack();

            // spotless:off
            List<IAEItemStack> matches = fuzzy ?
                ImmutableList.copyOf(state.itemStorage.getStorageList().findFuzzy(aeReq, FuzzyMode.IGNORE_ALL)) :
                Arrays.asList(state.itemStorage.getStorageList().findPrecise(aeReq));
            // spotless:on

            for (IAEItemStack match : matches) {
                if (req.getStackSize() == 0) {
                    break;
                }

                if (match == null) {
                    continue;
                }

                match = match.copy()
                    .setStackSize(req.getStackSize());

                    IAEItemStack result = state.itemStorage.extractItems(
                    match,
                    simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                    new PlayerSource(player, state.securityTerminal));

                if (result != null) {
                    extractedItems.add(BigItemStack.create(result));
                    req.decStackSize(result.getStackSize());
                }
            }
        }
    }

    @Optional(Names.GREG_TECH)
    private void consumeItemsFromUplink(List<BigItemStack> requestedItems, List<BigItemStack> extractedItems,
        int flags) {
        boolean simulate = (flags & CONSUME_SIMULATED) != 0;
        boolean fuzzy = (flags & CONSUME_FUZZY) != 0;

        if (state.uplinkAddress == null) return;

        state.connectToUplink();

        if (!state.hasUplinkConnection()) return;

        IUplinkMulti uplink = state.uplink;

        var result = uplink.tryConsumeItems(requestedItems, simulate, fuzzy);

        if (result.left() != UplinkStatus.OK && !printedUplinkWarning) {
            printedUplinkWarning = true;
            MMUtils.sendErrorToPlayer(
                player,
                "Could not request items from uplink: " + result.left()
                    .toString());
        }

        if (result.right() != null) extractedItems.addAll(result.right());
    }

}
