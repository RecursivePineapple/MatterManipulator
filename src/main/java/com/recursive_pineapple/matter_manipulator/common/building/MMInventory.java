package com.recursive_pineapple.matter_manipulator.common.building;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
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
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * Handles all manipulator-related item sourcing and sinking.
 */
public class MMInventory implements IPseudoInventory {

    public EntityPlayer player;
    public MMState state;
    public ManipulatorTier tier;

    public final Object2LongOpenHashMap<ItemId> pendingItems = new Object2LongOpenHashMap<>();
    public final Object2LongOpenHashMap<FluidId> pendingFluids = new Object2LongOpenHashMap<>();

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

            if ((flags & CONSUME_SIMULATED) != 0) {
                return Pair.of(true, extracted);
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
    public void givePlayerItems(List<BigItemStack> items) {
        if (player.capabilities.isCreativeMode) {
            return;
        }

        for (BigItemStack item : items) {
            if (item != null && item.getItem() != null) {
                pendingItems.addTo(item.getId(), item.stackSize);
            }
        }
    }

    @Override
    public void givePlayerFluids(List<BigFluidStack> fluids) {
        if (player.capabilities.isCreativeMode) {
            return;
        }

        for (BigFluidStack fluid : fluids) {
            if (fluid != null) {
                pendingFluids.addTo(fluid.getId(), fluid.amount);
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

        outer: for (var entry : pendingItems.object2LongEntrySet()) {
            ItemId item = entry.getKey();

            BigItemStack stack = new BigItemStack(item.getItemStack()).setStackSize(entry.getLongValue());

            if (hasME) {
                injectItemsIntoAE(stack);

                if (stack.getStackSize() == 0) continue;
            }

            if (uplink != null) {
                UplinkStatus status = uplink.tryGivePlayerItems(Arrays.asList(stack));

                if (status != UplinkStatus.OK && !printedUplinkWarning) {
                    printedUplinkWarning = true;
                    MMUtils.sendErrorToPlayer(player, "Could not push items to uplink: " + status.toString());
                }

                if (stack.getStackSize() == 0) continue;
            }

            while (stack.stackSize > 0) {
                ItemStack smallStack = stack.remove(item.getItemStack().getMaxStackSize());

                int toInsert = smallStack.stackSize;

                player.inventory.addItemStackToInventory(smallStack);

                stack.stackSize += smallStack.stackSize;

                if (smallStack.stackSize == toInsert) continue outer;
            }

            AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(player.posX - 2.5d, player.posY - 1d, player.posX - 2.5d, player.posY + 2.5d, player.getEyeHeight() + 1d, player.posZ + 2.5d);

            for(ItemStack smallStack : stack.toStacks()) {
                var onGround = player.worldObj.getEntitiesWithinAABB(EntityItemLarge.class, aabb);

                for (EntityItemLarge e : onGround) {
                    if (smallStack.stackSize <= 0) break;

                    ItemStack droppedStack = e.getEntityItem();
                    if (MMUtils.areStacksBasicallyEqual(droppedStack, smallStack)) {
                        int toAdd = (int) (Math.min((long) Integer.MAX_VALUE, (long) droppedStack.stackSize + (long) smallStack.stackSize) - droppedStack.stackSize);

                        droppedStack = droppedStack.copy();

                        droppedStack.stackSize += toAdd;
                        smallStack.stackSize -= toAdd;

                        e.setEntityItemStack(droppedStack);
                    }
                }

                if (smallStack.stackSize > 0) {
                    player.worldObj.spawnEntityInWorld(
                        new EntityItemLarge(
                            player.worldObj,
                            player.posX,
                            player.posY,
                            player.posZ,
                            smallStack));
                }
            }
        }

        pendingItems.clear();

        outer: for (var entry : pendingFluids.object2LongEntrySet()) {
            FluidId id = entry.getKey();
            long amount = entry.getLongValue();

            BigFluidStack stack = new BigFluidStack(id.getFluidStack()).setStackSize(amount);

            if (hasME) {
                injectFluidsIntoAE(stack);

                if (stack.getStackSize() == 0) continue outer;
            }

            if (uplink != null) {
                UplinkStatus status = uplink.tryGivePlayerFluids(Arrays.asList(stack));

                if (status != UplinkStatus.OK && !printedUplinkWarning) {
                    printedUplinkWarning = true;
                    MMUtils.sendErrorToPlayer(player, "Could not push fluids to uplink: " + status.toString());
                }

                if (stack.getStackSize() == 0) continue outer;
            }

            // this is final because of the lambdas, but its amount field is updated several times
            final FluidStack fluid = id
                .getFluidStack(amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount);

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
                continue outer;
            }

            fluid.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;

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
                fluid.amount = amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
                amount -= ((IFluidContainerItem) cell.getItem()).fill(idealCell, fluid.copy(), true);

                if (amount <= 0) {
                    continue outer;
                }
            }

            if (amount > 0 && !player.capabilities.isCreativeMode) {
                MMUtils.sendWarningToPlayer(player, "Could not find a container for fluid (it was voided): "
                            + amount
                            + "L of "
                            + fluid.getLocalizedName());
            }
        }

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

                long amtInPending = pendingItems.getLong(id);

                if (amtInPending == 0) {
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
                        pendingItems.removeLong(id);
                    } else {
                        pendingItems.put(id, amtInPending);
                    }
                }
            } else {
                var iter = pendingItems.object2LongEntrySet()
                    .iterator();

                while (iter.hasNext()) {
                    var e = iter.next();

                    if (e.getLongValue() == 0) {
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

                    long amtInPending = e.getLongValue();
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

                if (req.getItem() != slot.getItem()) continue;

                ItemStack reqStack = req.getItemStack();

                if (fuzzy) {
                    if (reqStack.getHasSubtypes()) {
                        if (req.meta != Items.feather.getDamage(slot)) continue;
                    }
                } else {
                    if (!MMUtils.areStacksBasicallyEqual(reqStack, slot)) continue;
                }

                if (slot.stackSize == 111) {
                    extractedItems.add(new BigItemStack(slot).setStackSize(req.getStackSize()));
                    req.setStackSize(0);
                    continue;
                }

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
