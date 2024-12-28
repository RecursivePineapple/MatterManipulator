package com.recursive_pineapple.matter_manipulator.common.building;


import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import org.joml.Vector3d;

import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.PendingBlock;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator.ManipulatorTier;
import com.recursive_pineapple.matter_manipulator.common.networking.SoundResource;
import com.recursive_pineapple.matter_manipulator.common.utils.BigFluidStack;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.ICustomNameObject;
import appeng.parts.AEBasePart;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IRedstoneEmitter;
import gregtech.common.blocks.BlockOres;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import gregtech.common.tileentities.machines.MTEHatchOutputME;
import gregtech.common.tileentities.storage.MTEDigitalChestBase;
import it.unimi.dsi.fastutil.Pair;

/**
 * Handles all generic manipulator building logic.
 */
public abstract class AbstractBuildable extends MMInventory implements IBuildable {

    public AbstractBuildable(EntityPlayer player, MMState state, ManipulatorTier tier) {
        super(player, state, tier);
    }

    protected static final double EU_PER_BLOCK = 128.0, TE_PENALTY = 16.0, EU_DISTANCE_EXP = 1.25;

    public boolean tryConsumePower(ItemStack stack, PendingBlock pendingBlock) {
        double euUsage = EU_PER_BLOCK * pendingBlock.getBlock()
            .getBlockHardness(pendingBlock.getWorld(), pendingBlock.x, pendingBlock.y, pendingBlock.z);

        try {
            Block block = pendingBlock.getBlock();
            if (block.isBlockContainer) {
                euUsage *= TE_PENALTY;
            }
        } catch (Throwable e) {
            MMMod.LOG.error("Could not get Block.isBlockContainer field", e);
        }

        return tryConsumePower(stack, pendingBlock.x, pendingBlock.y, pendingBlock.z, euUsage);
    }

    public boolean tryConsumePower(ItemStack stack, double x, double y, double z, double euUsage) {
        if (player.capabilities.isCreativeMode) {
            return true;
        }

        euUsage *= Math.pow(player.getDistance(x, y, z), EU_DISTANCE_EXP);

        return ((ItemMatterManipulator) stack.getItem()).use(stack, euUsage, player);
    }

    /**
     * Removes a block and stores its items in this object. Items & fluids must delivered by calling
     * {@link #actuallyGivePlayerStuff()} or they will be deleted.
     */
    protected void removeBlock(World world, int x, int y, int z, Block existing, int existingMeta) {

        PendingBlock block = PendingBlock.fromBlock(world, x, y, z, existing, existingMeta);

        if (block != null) {
            ItemStack stack = block.toStack();

            boolean isOre = false;

            if (GregTech.isModLoaded() && existing instanceof BlockOres) {
                isOre = true;
            } else {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (OreDictionary.getOreName(id).startsWith("ore")) {
                        isOre = true;
                        break;
                    }
                }
            }

            if (isOre) {
                world.setBlockToAir(x, y, z);
                return;
            }
        }

        TileEntity te = world.getTileEntity(x, y, z);

        boolean ae = Mods.AppliedEnergistics2.isModLoaded();
        boolean gt = Mods.GregTech.isModLoaded();
        boolean eio = Mods.EnderIO.isModLoaded();

        if (ae && gt) emptySuperchest(te);
        if (ae && gt) emptyMEOutput(te);
        emptyTileInventory(te);
        emptyTank(te);
        if (gt) removeCovers(te);
        if (ae) resetAEMachine(te);
        if (gt) resetGTMachine(te);
        if (eio) resetConduitBundle(te);

        if (existing instanceof IFluidBlock fluidBlock && fluidBlock.canDrain(world, x, y, z)) {
            givePlayerFluids(fluidBlock.drain(world, x, y, z, true));
        } else if (existing == Blocks.water || existing == Blocks.lava) {
            givePlayerFluids(new FluidStack(existing == Blocks.water ? FluidRegistry.WATER : FluidRegistry.LAVA, 1000));
        } else {
            givePlayerItems(
                existing.getDrops(world, x, y, z, existingMeta, 0)
                    .toArray(new ItemStack[0]));
        }

        world.setBlockToAir(x, y, z);
    }

    @Optional({ Names.GREG_TECH, Names.APPLIED_ENERGISTICS2 })
    protected void emptySuperchest(TileEntity te) {
        if (te instanceof IGregTechTileEntity igte && igte.getMetaTileEntity() instanceof MTEDigitalChestBase dchest) {
            for (IAEItemStack stack : dchest.getStorageList()) {
                stack = dchest.extractItems(stack, Actionable.MODULATE, null);

                while (stack.getStackSize() > 0) {
                    ItemStack is = stack.getItemStack();
                    stack.decStackSize(is.stackSize);
                    givePlayerItems(is);
                }
            }
        }
    }

    @Optional({ Names.GREG_TECH, Names.APPLIED_ENERGISTICS2 })
    private static Function<MTEHatchOutputBusME, IItemList<IAEItemStack>> GET_ITEM_STACK_LIST;

    @Optional({ Names.GREG_TECH, Names.APPLIED_ENERGISTICS2 })
    private static Function<MTEHatchOutputME, IItemList<IAEFluidStack>> GET_FLUID_STACK_LIST;

    static {
        if (GregTech.isModLoaded() && AppliedEnergistics2.isModLoaded()) {
            GET_ITEM_STACK_LIST = MMUtils.exposeFieldGetterLambda(MTEHatchOutputBusME.class, "itemCache");
            GET_FLUID_STACK_LIST = MMUtils.exposeFieldGetterLambda(MTEHatchOutputME.class, "fluidCache");
        }
    }

    @Optional({ Names.GREG_TECH, Names.APPLIED_ENERGISTICS2 })
    protected void emptyMEOutput(TileEntity te) {
        if (te instanceof IGregTechTileEntity igte) {
            if (igte.getMetaTileEntity() instanceof MTEHatchOutputBusME bus) {
                IItemList<IAEItemStack> items = GET_ITEM_STACK_LIST.apply(bus);

                for (IAEItemStack item : items) {
                    if (item.getStackSize() == 0) continue;

                    givePlayerItems(Arrays.asList(BigItemStack.create(item)));
                }
            }

            if (igte.getMetaTileEntity() instanceof MTEHatchOutputME hatch) {
                IItemList<IAEFluidStack> fluids = GET_FLUID_STACK_LIST.apply(hatch);

                for (IAEFluidStack fluid : fluids) {
                    if (fluid.getStackSize() == 0) continue;

                    givePlayerFluids(Arrays.asList(BigFluidStack.create(fluid)));
                }
            }
        }
    }

    protected void emptyTileInventory(TileEntity te) {
        if (te instanceof IInventory inv) {
            MMUtils.emptyInventory(this, inv);
        }
    }

    protected void emptyTank(TileEntity te) {
        if (te instanceof IFluidHandler handler) {
            if (GregTech.isModLoaded() && MMUtils.isStockingHatch(handler)) return;

            FluidStack fluid = null;
            while ((fluid = handler.drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, true)) != null
                && fluid.getFluid() != null
                && fluid.amount > 0) {
                givePlayerFluids(fluid);
            }
        }
    }

    @Optional(Names.GREG_TECH)
    protected void removeCovers(TileEntity te) {
        if (te instanceof ICoverable coverable) {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                if (coverable.getCoverIDAtSide(side) != 0) {
                    ItemStack cover = coverable.removeCoverAtSide(side, true);

                    if (cover != null && cover.getItem() != null) {
                        givePlayerItems(cover);
                    }
                }
            }
        }
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    protected void resetAEMachine(Object machine) {
        if (machine instanceof ISegmentedInventory segmentedInventory) {
            IInventory upgrades = segmentedInventory.getInventoryByName("upgrades");

            if (upgrades != null) {
                MMUtils.emptyInventory(this, upgrades);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");

            if (cells != null) {
                MMUtils.emptyInventory(this, cells);
            }

            IInventory patterns = segmentedInventory.getInventoryByName("patterns");

            if (patterns != null) {
                MMUtils.emptyInventory(this, patterns);
            }
        }

        if (machine instanceof ICustomNameObject customName) {
            if (customName.hasCustomName()) {
                try {
                    customName.setCustomName(null);
                } catch (IllegalArgumentException e) {
                    // hack because AEBasePart's default setCustomName impl throws an IAE when the name is null
                    if (machine instanceof AEBasePart basePart) {
                        NBTTagCompound tag = basePart.getItemStack()
                            .getTagCompound();

                        if (tag != null) {
                            tag.removeTag("display");

                            if (tag.hasNoTags()) {
                                basePart.getItemStack()
                                    .setTagCompound(null);
                            }
                        }
                    }
                }
            }
        }

        if (machine instanceof IPartHost host) {
            // intentionally includes UNKNOWN to remove any cables
            for (ForgeDirection dir : ForgeDirection.values()) {
                IPart part = host.getPart(dir);

                if (part != null) {
                    resetAEMachine(part);

                    host.removePart(dir, false);

                    givePlayerItems(part.getItemStack(PartItemStack.Break));

                    ArrayList<ItemStack> drops = new ArrayList<>();
                    part.getDrops(drops, false);

                    if (!drops.isEmpty()) {
                        givePlayerItems(drops.toArray(new ItemStack[drops.size()]));
                    }
                }
            }
        }
    }

    @Optional(Names.GREG_TECH)
    protected void resetGTMachine(TileEntity te) {
        if (te instanceof IRedstoneEmitter emitter) {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                emitter.setRedstoneOutputStrength(side, false);
            }
        }
    }

    @Optional(Names.ENDER_I_O)
    protected void resetConduitBundle(TileEntity te) {
        if (te instanceof IConduitBundle bundle) {
            for (IConduit conduit : bundle.getConduits()) {
                givePlayerItems(conduit.getDrops().toArray(new ItemStack[0]));
                bundle.removeConduit(conduit);
            }
        }
    }

    private static class SoundInfo {

        private int eventCount;
        private double sumX, sumY, sumZ;
    }

    private final HashMap<Pair<SoundResource, World>, SoundInfo> pendingSounds = new HashMap<>();

    private boolean printedProtectedBlockWarning = false;

    /**
     * Queues a sound to be played at a specific spot.
     * This doesn't actually play anything, {@link #playSounds()} must be called to play all queued sounds.
     * This mechanism finds the centre point for all played sounds of the same type and makes a single sound event so
     * that several aren't played in the same tick.
     */
    protected void playSound(World world, int x, int y, int z, SoundResource sound) {
        Pair<SoundResource, World> pair = Pair.of(sound, world);

        SoundInfo info = pendingSounds.computeIfAbsent(pair, ignored -> new SoundInfo());

        info.eventCount++;
        info.sumX += x;
        info.sumY += y;
        info.sumZ += z;
    }

    protected void playSounds() {
        pendingSounds.forEach((pair, info) -> {
            int avgX = (int) (info.sumX / info.eventCount);
            int avgY = (int) (info.sumY / info.eventCount);
            int avgZ = (int) (info.sumZ / info.eventCount);

            float distance = (float) new Vector3d(player.posX - avgX, player.posY - avgY, player.posZ - avgZ).length();

            pair.left().sendPlayToAll(new Location(pair.right(), avgX, avgY, avgZ), (distance / 16f) + 1, -1);
        });
        pendingSounds.clear();
    }

    /**
     * Checks if a block can be edited.
     */
    protected boolean isEditable(World world, int x, int y, int z) {
        // if this block is protected, ignore it completely and print a warning
        // spotless:off
        if (!world.canMineBlock(player, x, y, z) || MinecraftServer.getServer().isBlockProtected(world, x, y, z, player)) {
            // spotless:on
            if (!printedProtectedBlockWarning) {
                MMUtils.sendWarningToPlayer(
                    player,
                    "Tried to break/place a block in a protected area!");
                printedProtectedBlockWarning = true;
            }

            return false;
        } else {
            return true;
        }
    }
}
