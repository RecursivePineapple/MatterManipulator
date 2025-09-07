package com.recursive_pineapple.matter_manipulator.common.utils;

import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.AppliedEnergistics2;
import static com.recursive_pineapple.matter_manipulator.common.utils.Mods.GregTech;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.relauncher.ReflectionHelper;

import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.metatileentity.IConnectable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IHasInventory;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.common.blocks.BlockMachines;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;

import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.implementations.parts.IPartCable;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.parts.automation.UpgradeInventory;
import appeng.util.Platform;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.gtnewhorizon.structurelib.util.XSTR;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.RequiredItemAnalysis;
import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.IPseudoInventory;
import com.recursive_pineapple.matter_manipulator.common.building.ImmutableBlockSpec;
import com.recursive_pineapple.matter_manipulator.common.building.InteropConstants;
import com.recursive_pineapple.matter_manipulator.common.building.MMInventory;
import com.recursive_pineapple.matter_manipulator.common.building.PendingBlock;
import com.recursive_pineapple.matter_manipulator.common.building.PortableItemStack;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.Location;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import org.joml.Vector3i;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class MMUtils {

    /**
     * The ME network is online and the player is within range of an access point.
     */
    public static final int TOOLTIP_AE_WORKS = 0b1;
    /**
     * The uplink is online and active.
     */
    public static final int TOOLTIP_UPLINK_WORKS = 0b10;

    /**
     * Formats a number with group separator and at most 2 fraction digits.
     */
    private static final Map<Locale, DecimalFormat> decimalFormatters = new HashMap<>();

    public static final String BLACK = EnumChatFormatting.BLACK.toString();
    public static final String DARK_BLUE = EnumChatFormatting.DARK_BLUE.toString();
    public static final String DARK_GREEN = EnumChatFormatting.DARK_GREEN.toString();
    public static final String DARK_AQUA = EnumChatFormatting.DARK_AQUA.toString();
    public static final String DARK_RED = EnumChatFormatting.DARK_RED.toString();
    public static final String DARK_PURPLE = EnumChatFormatting.DARK_PURPLE.toString();
    public static final String GOLD = EnumChatFormatting.GOLD.toString();
    public static final String GRAY = EnumChatFormatting.GRAY.toString();
    public static final String DARK_GRAY = EnumChatFormatting.DARK_GRAY.toString();
    public static final String BLUE = EnumChatFormatting.BLUE.toString();
    public static final String GREEN = EnumChatFormatting.GREEN.toString();
    public static final String AQUA = EnumChatFormatting.AQUA.toString();
    public static final String RED = EnumChatFormatting.RED.toString();
    public static final String LIGHT_PURPLE = EnumChatFormatting.LIGHT_PURPLE.toString();
    public static final String YELLOW = EnumChatFormatting.YELLOW.toString();
    public static final String WHITE = EnumChatFormatting.WHITE.toString();
    public static final String OBFUSCATED = EnumChatFormatting.OBFUSCATED.toString();
    public static final String BOLD = EnumChatFormatting.BOLD.toString();
    public static final String STRIKETHROUGH = EnumChatFormatting.STRIKETHROUGH.toString();
    public static final String UNDERLINE = EnumChatFormatting.UNDERLINE.toString();
    public static final String ITALIC = EnumChatFormatting.ITALIC.toString();
    public static final String RESET = EnumChatFormatting.RESET.toString();

    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private MMUtils() {}

    public static int clamp(int val, int lo, int hi) {
        return MathHelper.clamp_int(val, lo, hi);
    }

    public static long clamp(long val, long lo, long hi) {
        return val < lo ? lo : val > hi ? hi : val;
    }

    public static int min(int first, int... rest) {
        for (int i = 0; i < rest.length; i++) {
            int l = rest[i];
            if (l < first) first = l;
        }
        return first;
    }

    public static long min(long first, long... rest) {
        for (int i = 0; i < rest.length; i++) {
            long l = rest[i];
            if (l < first) first = l;
        }
        return first;
    }

    public static int max(int first, int... rest) {
        for (int i = 0; i < rest.length; i++) {
            int l = rest[i];
            if (l > first) first = l;
        }
        return first;
    }

    public static long max(long first, long... rest) {
        for (int i = 0; i < rest.length; i++) {
            long l = rest[i];
            if (l > first) first = l;
        }
        return first;
    }

    public static int ceilDiv(int lhs, int rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int ceilDiv2(int lhs, int rhs) {
        int sign = signum(lhs) * signum(rhs);

        if (lhs == 0) return 0;
        if (rhs == 0) throw new ArithmeticException("/ by zero");

        lhs = Math.abs(lhs);
        rhs = Math.abs(rhs);

        int unsigned = 1 + ((lhs - 1) / rhs);

        return unsigned * sign;
    }

    public static long ceilDiv(long lhs, long rhs) {
        return (lhs + rhs - 1) / rhs;
    }

    public static int signum(int x) {
        return x < 0 ? -1 : x > 0 ? 1 : 0;
    }

    public static long signum(long x) {
        return x < 0 ? -1 : x > 0 ? 1 : 0;
    }

    public static Vector3i signum(Vector3i v) {
        v.x = signum(v.x);
        v.y = signum(v.y);
        v.z = signum(v.z);

        return v;
    }

    public static long ceilLong(double d) {
        long l = (long) d;
        return d > l ? l + 1 : l;
    }

    /**
     * Gets the standard vanilla hit result for a player.
     */
    public static MovingObjectPosition getHitResult(EntityPlayer player, boolean includeLiquids) {
        double reachDistance = player instanceof EntityPlayerMP mp ?
            mp.theItemInWorldManager.getBlockReachDistance() :
            Minecraft.getMinecraft().playerController.getBlockReachDistance();

        Vec3 posVec = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);

        Vec3 lookVec = player.getLook(1);

        Vec3 modifiedPosVec = posVec
            .addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

        MovingObjectPosition hit = player.worldObj.rayTraceBlocks(posVec, modifiedPosVec, includeLiquids);

        return hit != null && hit.typeOfHit != MovingObjectType.BLOCK ? null : hit;
    }

    /**
     * Gets the 'location' that the player is looking at.
     */
    public static Vector3i getLookingAtLocation(EntityPlayer player) {
        double dist = player instanceof EntityPlayerMP mp ?
            mp.theItemInWorldManager.getBlockReachDistance() :
            Minecraft.getMinecraft().playerController.getBlockReachDistance();

        Vec3 start = player.getPosition(0);
        Vec3 look = player.getLookVec();

        if (player instanceof EntityPlayerMP) {
            start.yCoord += player.getEyeHeight();
        }

        Vec3 end = Vec3.createVectorHelper(start.xCoord + look.xCoord * dist, start.yCoord + look.yCoord * dist, start.zCoord + look.zCoord * dist);

        MovingObjectPosition hit = player.worldObj.rayTraceBlocks(start, end);

        Vector3i target;

        if (hit != null && hit.typeOfHit == MovingObjectType.BLOCK) {
            target = new Vector3i(hit.blockX, hit.blockY, hit.blockZ);

            if (!player.isSneaking()) {
                ForgeDirection dir = ForgeDirection.getOrientation(hit.sideHit);
                target.add(dir.offsetX, dir.offsetY, dir.offsetZ);
            }
        } else {
            target = new Vector3i(
                MathHelper.floor_double(end.xCoord),
                MathHelper.floor_double(end.yCoord),
                MathHelper.floor_double(end.zCoord)
            );
        }

        return target;
    }

    /**
     * Calculates the delta x/y/z for the bounding box around a,b.
     * This is useful because a,a + deltas will always represent the same bounding box that's around a,b.
     */
    public static Vector3i getRegionDeltas(Location a, Location b) {
        if (a == null || b == null || a.worldId != b.worldId) return null;

        int x1 = a.x;
        int y1 = a.y;
        int z1 = a.z;
        int x2 = b.x;
        int y2 = b.y;
        int z2 = b.z;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        int dX = (maxX - minX) * (minX < x1 ? -1 : 1);
        int dY = (maxY - minY) * (minY < y1 ? -1 : 1);
        int dZ = (maxZ - minZ) * (minZ < z1 ? -1 : 1);

        return new Vector3i(dX, dY, dZ);
    }

    /**
     * {@link #getRegionDeltas(Location, Location)} but with three params.
     */
    public static Vector3i getRegionDeltas(Location a, Location b, Location c) {
        if (a == null || b == null || c == null || a.worldId != b.worldId || a.worldId != c.worldId) return null;

        Vector3i vA = a.toVec();
        Vector3i vB = b.toVec();
        Vector3i vC = c.toVec();

        Vector3i max = new Vector3i(vA).max(vB)
            .max(vC);
        Vector3i min = new Vector3i(vA).min(vB)
            .min(vC);

        int dX = (max.x - min.x) * (min.x < a.x ? -1 : 1);
        int dY = (max.y - min.y) * (min.y < a.y ? -1 : 1);
        int dZ = (max.z - min.z) * (min.z < a.z ? -1 : 1);

        return new Vector3i(dX, dY, dZ);
    }

    /**
     * Converts deltas to an AABB.
     */
    public static AxisAlignedBB getBoundingBox(Location l, Vector3i deltas) {
        int minX = Math.min(l.x, l.x + deltas.x);
        int minY = Math.min(l.y, l.y + deltas.y);
        int minZ = Math.min(l.z, l.z + deltas.z);
        int maxX = Math.max(l.x, l.x + deltas.x) + 1;
        int maxY = Math.max(l.y, l.y + deltas.y) + 1;
        int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Gets all blocks contained in a bounding box.
     * This can certainly be improved but I couldn't get the Iterator version to work properly and this doesn't seem to
     * be a big problem.
     */
    public static List<Vector3i> getBlocksInBB(Location l, Vector3i deltas) {
        int minX = Math.min(l.x, l.x + deltas.x);
        int minY = Math.min(l.y, l.y + deltas.y);
        int minZ = Math.min(l.z, l.z + deltas.z);
        int maxX = Math.max(l.x, l.x + deltas.x) + 1;
        int maxY = Math.max(l.y, l.y + deltas.y) + 1;
        int maxZ = Math.max(l.z, l.z + deltas.z) + 1;

        int dX = maxX - minX;
        int dY = maxY - minY;
        int dZ = maxZ - minZ;

        List<Vector3i> blocks = new ArrayList<>();

        for (int y = 0; y < dY; y++) {
            for (int z = 0; z < dZ; z++) {
                for (int x = 0; x < dX; x++) {
                    blocks.add(new Vector3i(minX + x, minY + y, minZ + z));
                }
            }
        }

        return blocks;
    }

    public static void sendErrorToPlayer(EntityPlayer aPlayer, String aChatMessage) {
        if (aPlayer instanceof EntityPlayerMP && aChatMessage != null) {
            aPlayer.addChatComponentMessage(new ChatComponentText(RED + aChatMessage));
        }
    }

    public static void sendWarningToPlayer(EntityPlayer aPlayer, String aChatMessage) {
        if (aPlayer instanceof EntityPlayerMP && aChatMessage != null) {
            aPlayer.addChatComponentMessage(new ChatComponentText(GOLD + aChatMessage));
        }
    }

    public static void sendInfoToPlayer(EntityPlayer aPlayer, String aChatMessage) {
        if (aPlayer instanceof EntityPlayerMP && aChatMessage != null) {
            aPlayer.addChatComponentMessage(new ChatComponentText(GRAY + aChatMessage));
        }
    }

    public static void sendChatToPlayer(EntityPlayer aPlayer, String aChatMessage) {
        if (aPlayer instanceof EntityPlayerMP && aChatMessage != null) {
            aPlayer.addChatComponentMessage(new ChatComponentText(aChatMessage));
        }
    }

    public static String stripFormat(String text) {
        return FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    private static DecimalFormat getDecimalFormat() {
        return decimalFormatters.computeIfAbsent(Locale.getDefault(Locale.Category.FORMAT), locale -> {
            DecimalFormat numberFormat = new DecimalFormat(); // uses the necessary locale inside anyway
            numberFormat.setGroupingUsed(true);
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setRoundingMode(RoundingMode.HALF_UP);
            DecimalFormatSymbols decimalFormatSymbols = numberFormat.getDecimalFormatSymbols();
            decimalFormatSymbols.setGroupingSeparator(','); // Use sensible separator for best clarity.
            numberFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            return numberFormat;
        });
    }

    public static String formatNumbers(BigInteger aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(long aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static String formatNumbers(double aNumber) {
        return getDecimalFormat().format(aNumber);
    }

    public static EntityPlayer getPlayerById(UUID playerId) {
        for (EntityPlayer player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (player.getGameProfile().getId().equals(playerId)) { return player; }
        }

        return null;
    }

    public static Object2LongOpenHashMap<ItemId> getItemStackHistogram(Iterable<ItemStack> stacks) {
        return getItemStackHistogram(stacks, true);
    }

    public static Object2LongOpenHashMap<ItemId> getItemStackHistogram(Iterable<ItemStack> stacks, boolean NBTSensitive) {
        Object2LongOpenHashMap<ItemId> histogram = new Object2LongOpenHashMap<>();

        if (stacks == null) return histogram;

        for (ItemStack stack : stacks) {
            if (stack == null || stack.getItem() == null) continue;
            histogram.addTo(ItemId.create(stack), stack.stackSize);
        }

        return histogram;
    }

    public static class StackMapDiff {

        public Object2LongOpenHashMap<ItemId> added = new Object2LongOpenHashMap<>();
        public Object2LongOpenHashMap<ItemId> removed = new Object2LongOpenHashMap<>();
    }

    public static StackMapDiff getStackMapDiff(Object2LongOpenHashMap<ItemId> before, Object2LongOpenHashMap<ItemId> after) {
        HashSet<ItemId> keys = new HashSet<>();
        keys.addAll(before.keySet());
        keys.addAll(after.keySet());

        StackMapDiff diff = new StackMapDiff();

        for (ItemId id : keys) {
            long beforeAmount = before.getLong(id);
            long afterAmount = after.getLong(id);

            if (afterAmount < beforeAmount) {
                diff.removed.addTo(id, beforeAmount - afterAmount);
            } else if (beforeAmount < afterAmount) {
                diff.added.addTo(id, afterAmount - beforeAmount);
            }
        }

        return diff;
    }

    public static List<ItemStack> getStacksOfSize(Object2LongOpenHashMap<ItemId> map, int maxStackSize) {
        ArrayList<ItemStack> list = new ArrayList<>();

        map.forEach((item, amount) -> {
            while (amount > 0) {
                int toRemove = Math
                    .min(amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : amount.intValue(), maxStackSize);

                list.add(item.getItemStack(toRemove));

                amount -= toRemove;
            }
        });

        return list;
    }

    public static List<ItemStack> getStacksOfSize(List<ItemStack> map, int maxStackSize) {
        ArrayList<ItemStack> list = new ArrayList<>();

        map.forEach(stack -> {
            while (stack.stackSize > 0) {
                int toRemove = Math.min(stack.stackSize, maxStackSize);

                ItemStack copy = stack.copy();
                copy.stackSize = toRemove;
                list.add(copy);

                stack.stackSize -= toRemove;
            }
        });

        return list;
    }

    public static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        return mergeStacks(stacks, false, false);
    }

    public static List<ItemStack> mergeStacks(List<ItemStack> stacks, boolean keepNBT, boolean NBTSensitive) {
        ArrayList<ItemStack> out = new ArrayList<>();
        HashMap<ItemId, ItemStack> map = new HashMap<>();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.getItem() == null) continue;

            ItemId id = NBTSensitive ? ItemId.create(stack) : ItemId.createWithoutNBT(stack);

            ItemStack match = map.get(id);

            if (match == null) {
                match = stack.copy();
                if (!keepNBT) match.setTagCompound(null);
                map.put(id, match);
                out.add(match);
            } else {
                match.stackSize += stack.stackSize;
            }
        }

        return out;
    }

    public static <S, T> List<T> mapToList(Collection<S> in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.size());

        for (S s : in)
            out.add(mapper.apply(s));

        return out;
    }

    public static <S, T> List<T> mapToList(S[] in, Function<S, T> mapper) {
        if (in == null) return null;

        List<T> out = new ArrayList<>(in.length);

        for (S s : in)
            out.add(mapper.apply(s));

        return out;
    }

    public static <S, T> T[] mapToArray(Collection<S> in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.size());

        Iterator<S> iter = in.iterator();
        for (int i = 0; i < out.length && iter.hasNext(); i++) {
            out[i] = mapper.apply(iter.next());
        }

        return out;
    }

    public static <S, T> T[] mapToArray(S[] in, IntFunction<T[]> ctor, Function<S, T> mapper) {
        if (in == null) return null;

        T[] out = ctor.apply(in.length);

        for (int i = 0; i < out.length; i++)
            out[i] = mapper.apply(in[i]);

        return out;
    }

    public static Stream<ItemStack> streamInventory(IInventory inv) {
        return IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot);
    }

    public static ItemStack[] inventoryToArray(IInventory inv) {
        return inventoryToArray(inv, true);
    }

    public static ItemStack[] inventoryToArray(IInventory inv, boolean copyStacks) {
        ItemStack[] array = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < array.length; i++) {
            array[i] = copyStacks ? ItemStack.copyItemStack(inv.getStackInSlot(i)) : inv.getStackInSlot(i);
        }

        return array;
    }

    public static ForgeDirection nullIfUnknown(ForgeDirection dir) {
        return dir == ForgeDirection.UNKNOWN ? null : dir;
    }

    public static <T> int indexOf(T[] array, T value) {
        int l = array.length;

        for (int i = 0; i < l; i++) {
            if (array[i] == value) { return i; }
        }

        return -1;
    }

    public static <T> T getIndexSafe(T[] array, int index) {
        return array == null || index < 0 || index >= array.length ? null : array[index];
    }

    public static <T> T getIndexSafe(List<T> list, int index) {
        return list == null || index < 0 || index >= list.size() ? null : list.get(index);
    }

    public static <T> T choose(List<T> list, Random rng) {
        if (list.isEmpty()) return null;
        if (list.size() == 1) return list.get(0);

        return list.get(rng.nextInt(list.size()));
    }

    /**
     * Empties all items in an inventory into a pseudo inventory.
     * Will reset/disassemble any items as necessary.
     */
    public static void emptyInventory(IPseudoInventory dest, IInventory src) {
        if (src == null) return;

        InventoryAdapter adapter = InventoryAdapter.findAdapter(src);

        if (adapter == null) return;

        int size = src.getSizeInventory();

        for (int slot = 0; slot < size; slot++) {
            if (!adapter.isValidSlot(src, slot)) continue;

            ItemStack stack = src.getStackInSlot(slot);

            if (stack == null || stack.getItem() == null || stack.stackSize == 0) continue;

            stack = adapter.extract(src, slot);

            if (stack != null && stack.getItem() != null) {
                dest.givePlayerItems(resetItem(dest, stack));
            }
        }

        src.markDirty();
    }

    public static ItemStack resetItem(IPseudoInventory dest, ItemStack stack) {
        if (AppliedEnergistics2.isModLoaded()) stack = resetAEItem(dest, stack);

        return stack;
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    private static ItemStack resetAEItem(IPseudoInventory dest, ItemStack stack) {
        if (stack.getItem() instanceof ICellWorkbenchItem cellWorkbenchItem && cellWorkbenchItem.isEditable(stack)) {
            emptyInventory(dest, cellWorkbenchItem.getUpgradesInventory(stack));
            clearInventory(cellWorkbenchItem.getConfigInventory(stack));

            NBTTagCompound tag = Platform.openNbtData(stack);
            tag.removeTag("FuzzyMode");
            tag.removeTag("upgrades");
            tag.removeTag("list");
            tag.removeTag("OreFilter");

            if (tag.hasNoTags()) tag = null;

            stack.setTagCompound(tag);
        }

        return stack;
    }

    public static boolean isSlotValid(IInventory inv, int slot) {
        if (GregTech.isModLoaded() && !isSlotValidGT(inv, slot)) return false;

        return true;
    }

    private static boolean isSlotValidGT(IInventory inv, int slot) {
        if (inv instanceof IHasInventory hasInv) {
            return hasInv.isValidSlot(slot);
        } else {
            return true;
        }
    }

    @Optional(Names.GREG_TECH_NH)
    public static boolean isStockingBus(IInventory inv) {
        if (inv instanceof BaseMetaTileEntity base && base.getMetaTileEntity() instanceof MTEHatchInputBusME) {
            return true;
        } else {
            return false;
        }
    }

    @Optional(Names.GREG_TECH_NH)
    public static boolean isStockingHatch(IFluidHandler tank) {
        if (tank instanceof BaseMetaTileEntity base && base.getMetaTileEntity() instanceof MTEHatchInputME) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all items in an inventory without returning them.
     */
    public static void clearInventory(IInventory inv) {
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            inv.setInventorySlotContents(i, null);
        }
        inv.markDirty();
    }

    /**
     * Merges stacks together and does not preserve order within the inventory.
     * Array will never contain null indices.
     */
    public static PortableItemStack[] fromInventory(IInventory inventory) {
        List<ItemStack> merged = mergeStacks(Arrays.asList(inventoryToArray(inventory, false)));
        ArrayList<PortableItemStack> out = new ArrayList<>();

        for (ItemStack stack : merged) {
            out.add(new PortableItemStack(stack));
        }

        return out.toArray(new PortableItemStack[out.size()]);
    }

    /**
     * Doesn't merge stacks and preserves the order of stacks.
     * Empty indices will be null.
     */
    public static PortableItemStack[] fromInventoryNoMerge(IInventory inventory) {
        PortableItemStack[] out = new PortableItemStack[inventory.getSizeInventory()];

        for (int i = 0; i < out.length; i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            out[i] = stack == null ? null : new PortableItemStack(stack);
        }

        return out;
    }

    /**
     * Installs upgrades into an AE UpgradeInventory.
     *
     * @param pupgrades The list of upgrades to install.
     * @param consume When true, items will be pulled from the pseudo inventory.
     * @param simulate When true, the upgrade inventory won't be touched at all.
     * @return True when successful.
     */
    public static boolean installUpgrades(
        IPseudoInventory src,
        UpgradeInventory dest,
        PortableItemStack[] pupgrades,
        boolean consume,
        boolean simulate
    ) {
        boolean success = true;

        List<ItemStack> stacks = mapToList(pupgrades, PortableItemStack::toStack);

        stacks.removeIf(i -> i == null || !(i.getItem() instanceof IUpgradeModule));

        for (ItemStack stack : stacks) {
            stack.stackSize = Math.min(stack.stackSize, dest.getMaxInstalled(((IUpgradeModule) stack.getItem()).getType(stack)));
        }

        Object2LongOpenHashMap<ItemId> actual = getItemStackHistogram(Arrays.asList(inventoryToArray(dest)));
        Object2LongOpenHashMap<ItemId> target = getItemStackHistogram(stacks);

        StackMapDiff diff = getStackMapDiff(actual, target);

        if (diff.removed.isEmpty() && diff.added.isEmpty()) return success;

        List<ItemStack> toInstall = getStacksOfSize(diff.added, dest.getInventoryStackLimit());

        long installable = dest.getSizeInventory() - actual.values().longStream().sum() + diff.removed.values().longStream().sum();

        List<BigItemStack> toInstallBig = toInstall.subList(0, Math.min(toInstall.size(), (int) installable))
            .stream()
            .map(BigItemStack::create)
            .collect(Collectors.toList());

        List<BigItemStack> extracted;

        if (consume) {
            var result = src.tryConsumeItems(toInstallBig, IPseudoInventory.CONSUME_PARTIAL);

            extracted = result.right();

            for (BigItemStack wanted : toInstallBig) {
                for (BigItemStack found : extracted) {
                    if (!found.isSameType(wanted)) continue;

                    wanted.stackSize -= found.stackSize;
                }
            }

            if (src instanceof IBlockApplyContext ctx) {
                for (BigItemStack wanted : toInstallBig) {
                    if (wanted.stackSize > 0) {
                        ctx.warn("Could not find upgrade: " + wanted.getItemStack().getDisplayName() + " x " + wanted.stackSize);
                        success = false;
                    }
                }
            }
        } else {
            extracted = mapToList(toInstallBig, BigItemStack::copy);
        }

        if (!simulate) {
            for (var e : diff.removed.object2LongEntrySet()) {
                long amount = e.getLongValue();

                for (int slot = 0; slot < dest.getSizeInventory(); slot++) {
                    if (amount <= 0) break;

                    ItemStack inSlot = dest.getStackInSlot(slot);

                    if (e.getKey().isSameAs(inSlot)) {
                        src.givePlayerItems(inSlot);
                        dest.setInventorySlotContents(slot, null);

                        amount--;
                    }
                }
            }

            int slot = 0;

            outer: for (BigItemStack stack : extracted) {
                for (ItemStack split : stack.toStacks(1)) {
                    while (dest.getStackInSlot(slot) != null) {
                        slot++;

                        if (slot >= dest.getSizeInventory()) {
                            MMMod.LOG.error(
                                "Tried to install too many upgrades: voiding the rest. Dest={}, upgrade={}, slot={}",
                                dest,
                                split,
                                slot,
                                new Exception()
                            );

                            if (src instanceof IBlockApplyContext ctx) {
                                ctx.error("Tried to install too many upgrades: voiding the rest (this is a bug, please report it)");
                            }
                            break outer;
                        }
                    }

                    dest.setInventorySlotContents(slot++, split);
                }
            }

            dest.markDirty();
        }

        return success;
    }

    public static NBTTagCompound copy(NBTTagCompound tag) {
        return tag == null ? null : (NBTTagCompound) tag;
    }

    public static ItemStack copyWithAmount(ItemStack stack, int amount) {
        if (stack == null) return null;
        stack = stack.copy();
        stack.stackSize = amount;
        return stack;
    }

    /**
     * Converts an nbt tag to json.
     * Does not preserve the specific types of the tags, but the returned data will be sane and generally correct.
     * Compatible with Gson.
     */
    @SuppressWarnings("unchecked")
    public static JsonElement toJsonObject(NBTBase nbt) {
        if (nbt == null) { return null; }

        if (nbt instanceof NBTTagCompound) {
            // NBTTagCompound
            final NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;
            final Map<String, NBTBase> tagMap = (Map<String, NBTBase>) nbtTagCompound.tagMap;

            JsonObject root = new JsonObject();

            for (Map.Entry<String, NBTBase> nbtEntry : tagMap.entrySet()) {
                root.add(nbtEntry.getKey(), toJsonObject(nbtEntry.getValue()));
            }

            return root;
        } else if (nbt instanceof NBTTagByte) {
            // Number (byte)
            return new JsonPrimitive(((NBTTagByte) nbt).func_150290_f());
        } else if (nbt instanceof NBTTagShort) {
            // Number (short)
            return new JsonPrimitive(((NBTTagShort) nbt).func_150289_e());
        } else if (nbt instanceof NBTTagInt) {
            // Number (int)
            return new JsonPrimitive(((NBTTagInt) nbt).func_150287_d());
        } else if (nbt instanceof NBTTagLong) {
            // Number (long)
            return new JsonPrimitive(((NBTTagLong) nbt).func_150291_c());
        } else if (nbt instanceof NBTTagFloat) {
            // Number (float)
            return new JsonPrimitive(((NBTTagFloat) nbt).func_150288_h());
        } else if (nbt instanceof NBTTagDouble) {
            // Number (double)
            return new JsonPrimitive(((NBTTagDouble) nbt).func_150286_g());
        } else if (nbt instanceof NBTBase.NBTPrimitive) {
            // Number
            return new JsonPrimitive(((NBTBase.NBTPrimitive) nbt).func_150286_g());
        } else if (nbt instanceof NBTTagString) {
            // String
            return new JsonPrimitive(((NBTTagString) nbt).func_150285_a_());
        } else if (nbt instanceof NBTTagList) {
            // Tag List
            final NBTTagList list = (NBTTagList) nbt;

            JsonArray arr = new JsonArray();
            list.tagList.forEach(c -> arr.add(toJsonObject((NBTBase) c)));
            return arr;
        } else if (nbt instanceof NBTTagIntArray) {
            // Int Array
            final NBTTagIntArray list = (NBTTagIntArray) nbt;

            JsonArray arr = new JsonArray();

            for (int i : list.func_150302_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return arr;
        } else if (nbt instanceof NBTTagByteArray) {
            // Byte Array
            final NBTTagByteArray list = (NBTTagByteArray) nbt;

            JsonArray arr = new JsonArray();

            for (byte i : list.func_150292_c()) {
                arr.add(new JsonPrimitive(i));
            }

            return arr;
        } else {
            throw new IllegalArgumentException("Unsupported NBT Tag: " + NBTBase.NBTTypes[nbt.getId()] + " - " + nbt);
        }
    }

    /**
     * The opposite of {@link #toJsonObject(NBTBase)}
     */
    public static NBTBase toNbt(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement == JsonNull.INSTANCE) { return null; }

        if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
            if (jsonPrimitive.isNumber()) {
                if (jsonPrimitive.getAsBigDecimal().remainder(BigDecimal.ONE).equals(BigDecimal.ZERO)) {
                    long lval = jsonPrimitive.getAsLong();

                    if (lval >= Byte.MIN_VALUE && lval <= Byte.MAX_VALUE) { return new NBTTagByte((byte) lval); }

                    if (lval >= Short.MIN_VALUE && lval <= Short.MAX_VALUE) { return new NBTTagShort((short) lval); }

                    if (lval >= Integer.MIN_VALUE && lval <= Integer.MAX_VALUE) { return new NBTTagInt((int) lval); }

                    return new NBTTagLong(lval);
                } else {
                    double dval = jsonPrimitive.getAsDouble();
                    float fval = (float) dval;

                    if (Math.abs(dval - fval) < 0.0001) { return new NBTTagFloat(fval); }

                    return new NBTTagDouble(dval);
                }
            } else {
                return new NBTTagString(jsonPrimitive.getAsString());
            }
        } else if (jsonElement instanceof JsonArray jsonArray) {
            final List<NBTBase> nbtList = new ArrayList<>();

            int type = -1;

            for (JsonElement element : jsonArray) {
                if (element == null || element == JsonNull.INSTANCE) continue;

                NBTBase tag = toNbt(element);

                if (tag == null) continue;

                if (type == -1) type = tag.getId();
                if (type != tag.getId()) throw new IllegalArgumentException("NBT lists cannot contain tags of varying types");

                nbtList.add(tag);
            }

            // spotless:off
            if (type == Constants.NBT.TAG_INT) {
                return new NBTTagIntArray(nbtList.stream().mapToInt(i -> ((NBTTagInt) i).func_150287_d()).toArray());
            } else if (type == Constants.NBT.TAG_BYTE) {
                final byte[] abyte = new byte[nbtList.size()];

                for (int i = 0; i < nbtList.size(); i++) {
                    abyte[i] = ((NBTTagByte) nbtList.get(i)).func_150290_f();
                }

                return new NBTTagByteArray(abyte);
            } else {
                NBTTagList nbtTagList = new NBTTagList();
                nbtList.forEach(nbtTagList::appendTag);

                return nbtTagList;
            }
            // spotless:on
        } else if (jsonElement instanceof JsonObject jsonObject) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            for (Map.Entry<String, JsonElement> jsonEntry : jsonObject.entrySet()) {
                if (jsonEntry.getValue() == JsonNull.INSTANCE) continue;

                nbtTagCompound.setTag(jsonEntry.getKey(), toNbt(jsonEntry.getValue()));
            }

            return nbtTagCompound;
        }

        throw new IllegalArgumentException("Unhandled element " + jsonElement);
    }

    /**
     * Converts an nbt tag to json.
     * Preserves types exactly. Not compatible with gson loading.
     */
    @SuppressWarnings("unchecked")
    public static JsonElement toJsonObjectExact(NBTBase nbt) {
        if (nbt == null) { return null; }

        if (nbt instanceof NBTTagCompound) {
            final NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;
            final Map<String, NBTBase> tagMap = (Map<String, NBTBase>) nbtTagCompound.tagMap;

            JsonObject root = new JsonObject();

            for (Map.Entry<String, NBTBase> nbtEntry : tagMap.entrySet()) {
                root.add(nbtEntry.getKey(), toJsonObjectExact(nbtEntry.getValue()));
            }

            return root;
        } else if (nbt instanceof NBTTagByte b) {
            return new JsonPrimitive("b" + b.func_150290_f());
        } else if (nbt instanceof NBTTagShort half) {
            return new JsonPrimitive("h" + half.func_150289_e());
        } else if (nbt instanceof NBTTagInt i) {
            return new JsonPrimitive("i" + Integer.toUnsignedString(i.func_150287_d(), 16));
        } else if (nbt instanceof NBTTagLong l) {
            return new JsonPrimitive("l" + Long.toUnsignedString(l.func_150291_c(), 16));
        } else if (nbt instanceof NBTTagFloat f) {
            return new JsonPrimitive("f" + Long.toUnsignedString(Float.floatToIntBits(f.func_150288_h()), 16));
        } else if (nbt instanceof NBTTagDouble d) {
            return new JsonPrimitive("d" + Long.toUnsignedString(Double.doubleToLongBits(d.func_150286_g()), 16));
        } else if (nbt instanceof NBTBase.NBTPrimitive other) {
            return new JsonPrimitive("d" + Long.toUnsignedString(Double.doubleToLongBits(other.func_150286_g()), 16));
        } else if (nbt instanceof NBTTagString s) {
            return new JsonPrimitive("s" + s.func_150285_a_());
        } else if (nbt instanceof NBTTagList l) {
            JsonArray arr = new JsonArray();

            l.tagList.forEach(c -> arr.add(toJsonObjectExact((NBTBase) c)));

            return arr;
        } else if (nbt instanceof NBTTagIntArray a) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            for (int i : a.func_150302_c()) {
                try {
                    dos.writeInt(i);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return new JsonPrimitive("1" + Base64.getEncoder().encodeToString(baos.toByteArray()));
        } else if (nbt instanceof NBTTagByteArray a) {
            return new JsonPrimitive("2" + Base64.getEncoder().encodeToString(a.func_150292_c()));
        } else {
            throw new IllegalArgumentException("Unsupported NBT Tag: " + NBTBase.NBTTypes[nbt.getId()] + " - " + nbt);
        }
    }

    /**
     * The opposite of {@link #toJsonObjectExact(NBTBase)}
     */
    public static NBTBase toNbtExact(JsonElement jsonElement) throws JsonParseException {
        if (jsonElement == null) { return null; }

        if (jsonElement instanceof JsonPrimitive primitive) {
            if (!primitive.isString()) throw new JsonParseException("expected json primitive to be string: '" + primitive + "'");

            String data = primitive.getAsString();

            if (data.length() < 2) throw new JsonParseException("illegal json primitive string: '" + data + "'");

            char prefix = data.charAt(0);
            data = data.substring(1);

            try {
                switch (prefix) {
                    case 'b' -> {
                        return new NBTTagByte(Byte.parseByte(data));
                    }
                    case 'h' -> {
                        return new NBTTagShort(Short.parseShort(data));
                    }
                    case 'i' -> {
                        return new NBTTagInt(Integer.parseUnsignedInt(data, 16));
                    }
                    case 'l' -> {
                        return new NBTTagLong(Long.parseUnsignedLong(data, 16));
                    }
                    case 'f' -> {
                        return new NBTTagFloat(Float.intBitsToFloat((int) Long.parseUnsignedLong(data, 16)));
                    }
                    case 'd' -> {
                        return new NBTTagDouble(Double.longBitsToDouble(Long.parseUnsignedLong(data, 16)));
                    }
                    case 's' -> {
                        return new NBTTagString(data);
                    }
                    case '1' -> {
                        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                        DataInputStream dis = new DataInputStream(bais);

                        int count = bais.available() / 4;

                        int[] array = new int[count];

                        for (int i = 0; i < count; i++) {
                            try {
                                array[i] = dis.readInt();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        return new NBTTagIntArray(array);
                    }
                    case '2' -> {
                        return new NBTTagByteArray(Base64.getDecoder().decode(data));
                    }
                }
            } catch (NumberFormatException e) {
                throw new JsonParseException("illegal number: " + primitive, e);
            }
        } else if (jsonElement instanceof JsonArray array) {
            NBTTagList list = new NBTTagList();

            for (JsonElement e : array) {
                list.appendTag(toNbtExact(e));
            }

            return list;
        } else if (jsonElement instanceof JsonObject obj) {
            NBTTagCompound tag = new NBTTagCompound();

            for (Map.Entry<String, JsonElement> jsonEntry : obj.entrySet()) {
                tag.setTag(jsonEntry.getKey(), toNbtExact(jsonEntry.getValue()));
            }

            return tag;
        }

        throw new IllegalArgumentException("Unhandled element " + jsonElement);
    }

    private static final Pattern INTEGER = Pattern.compile("\\d+");
    private static final Pattern FLOAT = Pattern.compile("\\d+\\.\\d+");

    /**
     * A helper for checking if an arbitrary JsonElement is truthy according to the standard JS rules, with some
     * modifications. This is useful for situations where you have an arbitrary deserialized JsonElement that's supposed
     * to have a boolean in it.
     */
    public static boolean isTruthy(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) element;

            if (primitive.isBoolean()) return primitive.getAsBoolean();

            if (primitive.isNumber()) return primitive.getAsNumber().doubleValue() != 0;

            String value = primitive.getAsString();

            if ("true".equals(value)) return true;
            if ("false".equals(value)) return false;

            if (INTEGER.matcher(value).matches()) return Long.parseLong(value) != 0;

            if (FLOAT.matcher(value).matches()) return Double.parseDouble(value) != 0;

            return !value.isEmpty();
        }

        if (element.isJsonArray()) return ((JsonArray) element).size() > 0;

        if (element.isJsonObject()) return !((JsonObject) element).entrySet().isEmpty();

        return false;
    }

    public static boolean areStacksBasicallyEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) { return a == null && b == null; }

        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
    }

    /**
     * Plans will have jobs automatically started (see
     * {@link #createPlanImpl(EntityPlayer, MMState, ItemMatterManipulator, int)}).
     */
    public static final int PLAN_AUTO_SUBMIT = 0b1;
    /**
     * Plans will ignore existing blocks (see {@link #createPlanImpl(EntityPlayer, MMState, ItemMatterManipulator, int)}).
     */
    public static final int PLAN_ALL = 0b10;

    /**
     * The logic for creating a plan.
     * This really belongs in {@link Messages}, but I put it here so that it's hotswappable.
     */
    public static void createPlanImpl(
        EntityPlayer player,
        MMState state,
        ItemMatterManipulator manipulator,
        int flags
    ) {
        state = state.clone();

        if (!Location.areCompatible(state.config.coordA, state.config.coordB)) {
            sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.must_have_copy_region"));
            return;
        }

        if ((flags & PLAN_ALL) != 0) {
            if (!Location.areCompatible(state.config.coordA, state.config.coordC)) {
                state.config.coordC = state.config.coordA.clone();
            }
        } else {
            if (!Location.areCompatible(state.config.coordA, state.config.coordC)) {
                sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.must_have_paste_region"));
                return;
            }
        }

        List<PendingBlock> blocks = state.getPendingBlocks(manipulator.tier, player.getEntityWorld());
        RequiredItemAnalysis itemAnalysis = BlockAnalyzer
            .getRequiredItemsForBuild(player, blocks, (flags & PLAN_ALL) != 0);

        List<BigItemStack> requiredItems = mapToList(
            itemAnalysis.requiredItems.entrySet(),
            e -> BigItemStack.create(e.getKey(), e.getValue())
        );

        MMInventory inv = new MMInventory(player, state, manipulator.tier);

        Pair<Boolean, List<BigItemStack>> extractResult = inv.tryConsumeItems(
            requiredItems,
            IPseudoInventory.CONSUME_SIMULATED | IPseudoInventory.CONSUME_PARTIAL | IPseudoInventory.CONSUME_IGNORE_CREATIVE
        );

        List<BigItemStack> availableItems = extractResult.right() == null ? new ArrayList<>() : extractResult.right();

        sendInfoToPlayer(player, StatCollector.translateToLocal("mm.info.required_items"));

        if (!requiredItems.isEmpty()) {
            requiredItems.stream()
                .map((BigItemStack stack) -> {
                    long available = availableItems.stream()
                        .filter(s -> s.isSameType(stack))
                        .mapToLong(s -> s.getStackSize())
                        .sum();

                    if (stack.getStackSize() - available > 0) {
                        return String.format(
                            "%s%s: %s%d%s (%s%d%s missing)",
                            stack.getItemStack()
                                .getDisplayName(),
                            GRAY,
                            GOLD,
                            stack.getStackSize(),
                            GRAY,
                            RED,
                            stack.getStackSize() - available,
                            GRAY
                        );
                    } else {
                        return String.format(
                            "%s%s: %s%d%s",
                            stack.getItemStack()
                                .getDisplayName(),
                            GRAY,
                            GOLD,
                            stack.getStackSize(),
                            GRAY
                        );
                    }
                })
                .sorted()
                .forEach(message -> { sendInfoToPlayer(player, message); });
        } else {
            sendInfoToPlayer(player, StatCollector.translateToLocal("mm.info.none"));
        }

        if (!requiredItems.isEmpty()) {
            if (state.connectToUplink()) {
                if ((flags & PLAN_ALL) == 0) {
                    requiredItems.forEach(stack -> {
                        long available = availableItems.stream()
                            .filter(s -> s.isSameType(stack))
                            .mapToLong(s -> s.getStackSize())
                            .sum();

                        stack.decStackSize(available);
                    });

                    Iterator<BigItemStack> iter = requiredItems.iterator();

                    while (iter.hasNext()) {
                        BigItemStack stack = iter.next();

                        if (stack.getStackSize() == 0) iter.remove();
                    }
                }

                if (!requiredItems.isEmpty()) {
                    state.uplink.submitPlan(
                        player,
                        state.config.coordA.toString(),
                        requiredItems,
                        (flags & PLAN_AUTO_SUBMIT) != 0
                    );
                } else {
                    sendInfoToPlayer(player, StatCollector.translateToLocal("mm.info.not_need_creating_pattern"));
                }
            } else {
                sendErrorToPlayer(player, StatCollector.translateToLocal("mm.info.error.not_connected"));
            }
        }
    }

    public static MethodHandle exposeFieldGetter(Class<?> clazz, String... names) {
        try {
            Field field = ReflectionHelper.findField(clazz, names);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field getter for " + clazz.getName() + ":" + names[0], e);
        }
    }

    public static MethodHandle exposeFieldSetter(Class<?> clazz, String... names) {
        try {
            Field field = ReflectionHelper.findField(clazz, names);
            field.setAccessible(true);
            return MethodHandles.lookup()
                .unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make field setter for " + clazz.getName() + ":" + names[0], e);
        }
    }

    public static <T, R> Function<T, R> exposeFieldGetterLambda(Class<? super T> clazz, String... names) {
        final MethodHandle method = exposeFieldGetter(clazz, names);

        return instance -> {
            try {
                return (R) method.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException("Could not get field " + clazz.getName() + ":" + names[0], e);
            }
        };
    }

    public static MethodHandle exposeMethod(Class<?> clazz, MethodType sig, String... names) {
        try {
            Method method = ReflectionHelper.findMethod(clazz, null, names, sig.parameterArray());
            method.setAccessible(true);
            return MethodHandles.lookup()
                .unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not make method handle for " + clazz.getName() + ":" + names[0], e);
        }
    }

    private static final XSTR RNG = new XSTR();

    public static Item getItemFromBlock(Block block, int metadata) {
        if (block == null) block = Blocks.air;

        Item item = Item.getItemFromBlock(block);

        if (item == null) {
            item = block.getItemDropped(metadata, RNG, 0);
        }

        return item;
    }

    public static Block getBlockFromItem(Item item, int metadata) {
        if (item == null) return Blocks.air;

        if (item == Items.redstone) {
            return Blocks.redstone_wire;
        } else if (item instanceof ItemReed specialPlacing) {
            return specialPlacing.field_150935_a;
        } else if (AppliedEnergistics2.isModLoaded() && isAECable(item, metadata)) {
            return InteropConstants.AE_BLOCK_CABLE.getBlock();
        } else {
            return Block.getBlockFromItem(item);
        }
    }

    @Optional(Names.GREG_TECH_NH)
    public static boolean isGTMachine(ImmutableBlockSpec spec) {
        if (spec.getBlock() instanceof BlockMachines) {
            if (getIndexSafe(GregTechAPI.METATILEENTITIES, spec.getItemMeta()) != null) { return true; }
        }

        return false;
    }

    @Optional(Names.GREG_TECH_NH)
    public static boolean isGTCable(ImmutableBlockSpec spec) {
        if (spec.getBlock() instanceof BlockMachines) {
            if (getIndexSafe(GregTechAPI.METATILEENTITIES, spec.getItemMeta()) instanceof IConnectable) { return true; }
        }

        return false;
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    public static boolean isAECable(ImmutableBlockSpec spec) {
        if (spec == null) return false;

        return isAECable(spec.getItem(), spec.getItemMeta());
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    public static boolean isAECable(Item item, int metadata) {
        if (item instanceof IPartItem partItem) {
            if (partItem.createPartFromItemStack(new ItemStack(item, 1, metadata)) instanceof IPartCable) { return true; }
        }

        return false;
    }

    @Optional(Names.APPLIED_ENERGISTICS2)
    public static boolean getAECable(BlockSpec spec, World world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof IPartHost partHost) {
            if (partHost.getPart(ForgeDirection.UNKNOWN) instanceof IPartCable cable) {
                spec.setObject(cable.getItemStack(PartItemStack.Break));
                return true;
            }
        }

        return false;
    }

    @Optional(Names.GREG_TECH_NH)
    public static boolean getGTCable(BlockSpec spec, World world, int x, int y, int z) {
        if (world.getTileEntity(x, y, z) instanceof IGregTechTileEntity igte && igte.getMetaTileEntity() instanceof IConnectable) {
            spec.setObject(Item.getItemFromBlock(world.getBlock(x, y, z)), igte.getMetaTileID());

            return true;
        }

        return false;
    }

    public static String getDirectionDisplayName(ForgeDirection dir) {
        return getDirectionDisplayName(dir, false);
    }

    public static String getDirectionDisplayName(ForgeDirection dir, boolean unknownIsCentre) {
        return switch (dir) {
            case DOWN -> "Down";
            case EAST -> "East";
            case NORTH -> "North";
            case SOUTH -> "South";
            case UNKNOWN -> unknownIsCentre ? "Center" : "Unknown";
            case UP -> "Up";
            case WEST -> "West";
        };
    }

    public static <K, V> boolean areMapsEqual(Map<K, V> left, Map<K, V> right) {
        if (left == null || right == null) return left == right;

        HashSet<K> keys = new HashSet<>(left.size() + right.size());

        keys.addAll(left.keySet());
        keys.addAll(right.keySet());

        for (K key : keys) {
            if (!Objects.equals(left.get(key), right.get(key))) return false;
        }

        return true;
    }
}
