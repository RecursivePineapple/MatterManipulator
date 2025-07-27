package com.recursive_pineapple.matter_manipulator.common.compat;

import static net.minecraftforge.common.util.ForgeDirection.*;
import static net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.blocks.ItemMachines;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import gregtech.common.tileentities.machines.MTEHatchOutputME;

import appeng.util.ReadableNumberConverter;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.recursive_pineapple.matter_manipulator.asm.Optional;
import com.recursive_pineapple.matter_manipulator.common.building.InteropConstants;
import com.recursive_pineapple.matter_manipulator.common.compat.BooleanProperty.FlagBooleanProperty;
import com.recursive_pineapple.matter_manipulator.common.compat.DirectionBlockProperty.AbstractDirectionBlockProperty;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

import net.bdew.ae2stuff.machines.wireless.TileWireless;

import bartworks.common.tileentities.multis.MTECircuitAssemblyLine;
import codechicken.enderstorage.common.TileFrequencyOwner;
import codechicken.enderstorage.storage.item.TileEnderChest;
import codechicken.enderstorage.storage.liquid.TileEnderTank;
import de.keridos.floodlights.tileentity.TileEntityMetaFloodlight;
import de.keridos.floodlights.tileentity.TileEntitySmallFloodlight;
import gcewing.architecture.common.tile.TileArchitecture;
import ic2.api.tile.IWrenchable;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import lombok.SneakyThrows;

public class BlockPropertyRegistry {

    public static final Map<String, BlockProperty<?>> EMPTY_O2O_MAP = Collections.emptyMap();

    private BlockPropertyRegistry() {}

    public static final Object2ObjectOpenHashMap<Block, Map<String, BlockProperty<?>>> SPECIFIC_BLOCK_PROPERTIES = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<Class<?>, Map<String, BlockProperty<?>>> BLOCK_IFACE_PROPERTIES = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<Class<?>, Map<String, BlockProperty<?>>> TILE_IFACE_PROPERTIES = new Object2ObjectOpenHashMap<>();

    public static final Object2ObjectOpenHashMap<Class<? extends Block>, Map<String, BlockProperty<?>>> CACHED_BLOCK_PROPS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<Class<? extends TileEntity>, Map<String, BlockProperty<?>>> CACHED_TILE_PROPS = new Object2ObjectOpenHashMap<>();

    public static void registerProperty(Block block, BlockProperty<?> property) {
        SPECIFIC_BLOCK_PROPERTIES.computeIfAbsent(block, x -> new Object2ObjectArrayMap<>()).put(property.getName(), property);
    }

    public static void registerProperty(Collection<Block> blocks, BlockProperty<?> property) {
        for (Block block : blocks) {
            registerProperty(block, property);
        }
    }

    public static void registerBlockInterfaceProperty(Class<?> iface, BlockProperty<?> property) {
        BLOCK_IFACE_PROPERTIES.computeIfAbsent(iface, x -> new Object2ObjectArrayMap<>()).put(property.getName(), property);
    }

    public static void registerTileEntityInterfaceProperty(Class<?> iface, BlockProperty<?> property) {
        TILE_IFACE_PROPERTIES.computeIfAbsent(iface, x -> new Object2ObjectArrayMap<>()).put(property.getName(), property);
    }

    private static Map<String, BlockProperty<?>> getUnfilteredBlockProperties(Class<? extends Block> clazz) {

        Map<String, BlockProperty<?>> props = CACHED_BLOCK_PROPS.get(clazz);

        if (props != null) return props;

        Map<String, BlockProperty<?>> cache = new Object2ObjectArrayMap<>();

        for (var e : BLOCK_IFACE_PROPERTIES.object2ObjectEntrySet()) {
            if (e.getKey().isAssignableFrom(clazz)) {
                cache.putAll(e.getValue());
            }
        }

        cache = cache.isEmpty() ? EMPTY_O2O_MAP : Collections.unmodifiableMap(cache);

        CACHED_BLOCK_PROPS.put(clazz, cache);

        return cache;
    }

    private static Map<String, BlockProperty<?>> getUnfilteredTileProperties(Class<? extends TileEntity> clazz) {

        Map<String, BlockProperty<?>> props = CACHED_TILE_PROPS.get(clazz);

        if (props != null) return props;

        Map<String, BlockProperty<?>> cache = new Object2ObjectArrayMap<>();

        for (var e : TILE_IFACE_PROPERTIES.object2ObjectEntrySet()) {
            if (e.getKey().isAssignableFrom(clazz)) {
                cache.putAll(e.getValue());
            }
        }

        cache = cache.isEmpty() ? EMPTY_O2O_MAP : Collections.unmodifiableMap(cache);

        CACHED_TILE_PROPS.put(clazz, cache);

        return cache;
    }

    public static void getProperties(World world, int x, int y, int z, Map<String, BlockProperty<?>> properties) {
        properties.clear();

        Block block = world.getBlock(x, y, z);

        Map<String, BlockProperty<?>> props = SPECIFIC_BLOCK_PROPERTIES.get(block);
        if (props != null) properties.putAll(props);

        addPropertiesFiltered(properties, getUnfilteredBlockProperties(block.getClass()), block);

        if (block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile != null) {
                addPropertiesFiltered(properties, getUnfilteredTileProperties(tile.getClass()), tile);
            }
        }
    }

    private static void addPropertiesFiltered(Map<String, BlockProperty<?>> dest, Map<String, BlockProperty<?>> src, Object filter) {
        if (src == null) return;

        for (var e : src.entrySet()) {
            if (filter == null || e.getValue().appliesTo(filter)) {
                dest.put(e.getKey(), e.getValue());
            }
        }
    }

    public static final Map<Block, List<IntrinsicProperty>> INTRINSIC_PROPERTIES = new Reference2ObjectArrayMap<>();

    public static void registerIntrinsicProperty(Block block, IntrinsicProperty property) {
        List<IntrinsicProperty> m = INTRINSIC_PROPERTIES.computeIfAbsent(block, x -> new ObjectArrayList<>());

        m.add(property);
    }

    public static void getIntrinsicProperties(IBlockAccess world, int x, int y, int z, Collection<IntrinsicProperty> props) {
        props.clear();

        List<IntrinsicProperty> unfiltered = INTRINSIC_PROPERTIES.get(world.getBlock(x, y, z));

        if (unfiltered != null) {
            for (IntrinsicProperty prop : unfiltered) {
                if (prop.hasValue(world, x, y, z)) props.add(prop);
            }
        }
    }

    public static void getIntrinsicProperties(IBlockAccess world, int x, int y, int z, Map<String, IntrinsicProperty> props) {
        props.clear();

        List<IntrinsicProperty> unfiltered = INTRINSIC_PROPERTIES.get(world.getBlock(x, y, z));

        if (unfiltered != null) {
            for (IntrinsicProperty prop : unfiltered) {
                if (prop.hasValue(world, x, y, z)) props.put(prop.getName(), prop);
            }
        }
    }

    public static void getIntrinsicProperties(ItemStack stack, Collection<IntrinsicProperty> props) {
        props.clear();

        if (stack == null) return;

        Block block = MMUtils.getBlockFromItem(stack.getItem(), stack.itemDamage);

        if (block == null) return;

        List<IntrinsicProperty> unfiltered = INTRINSIC_PROPERTIES.get(block);

        if (unfiltered != null) {
            for (IntrinsicProperty prop : unfiltered) {
                if (prop.hasValue(stack)) props.add(prop);
            }
        }
    }

    public static void getIntrinsicProperties(ItemStack stack, Map<String, IntrinsicProperty> props) {
        props.clear();

        if (stack == null) return;

        Block block = MMUtils.getBlockFromItem(stack.getItem(), stack.itemDamage);

        if (block == null) return;

        List<IntrinsicProperty> unfiltered = INTRINSIC_PROPERTIES.get(block);

        if (unfiltered != null) {
            for (IntrinsicProperty prop : unfiltered) {
                if (prop.hasValue(stack)) props.put(prop.getName(), prop);
            }
        }
    }

    public static void init() {
        initVanilla();

        if (Mods.StorageDrawers.isModLoaded()) initStorageDrawers();
        if (Mods.IndustrialCraft2.isModLoaded()) initIC2();
        if (Mods.ArchitectureCraft.isModLoaded()) initArch();
        if (Mods.FloodLights.isModLoaded()) initFloodLights();
        if (Mods.GregTech.isModLoaded()) initGT5u();
        if (Mods.AE2Stuff.isModLoaded()) initAE2Stuff();
        if (Mods.EnderStorage.isModLoaded()) initEnderStorage();
    }

    // #region Vanilla

    private static void initVanilla() {
        registerBlockInterfaceProperty(
            BlockLog.class,
            DirectionBlockProperty.facing(
                0b1100,
                dir -> switch (dir) {
                    case UP, DOWN -> 0;
                    case NORTH, SOUTH -> 0b1000;
                    case EAST, WEST -> 0b100;
                    default -> 0;
                },
                meta -> switch (meta) {
                    case 0b100 -> EAST;
                    case 0b1000 -> NORTH;
                    default -> UP;
                }
            )
        );

        registerProperty(
            Blocks.rail,
            new MetaBlockProperty<RailMode>() {

                @Override
                public String getName() {
                    return "mode";
                }

                @Override
                public RailMode getValue(int meta) {
                    return getRailMode(meta, true);
                }

                public int getMeta(RailMode value, int existing) {
                    return getRailMeta(value, getRailDirection(existing, true), true, false);
                }

                @Override
                public RailMode parse(String text) throws Exception {
                    return RailMode.parse(text);
                }
            }
        );

        registerProperty(
            Blocks.rail,
            DirectionBlockProperty.facing(
                (dir, existing) -> getRailMeta(getRailMode(existing, true), dir, true, false),
                meta -> getRailDirection(meta, true)
            )
        );

        registerProperty(
            Arrays.asList(Blocks.golden_rail, Blocks.detector_rail, Blocks.activator_rail),
            new MetaBlockProperty<RailMode>() {

                @Override
                public String getName() {
                    return "mode";
                }

                @Override
                public RailMode getValue(int meta) {
                    return getRailMode(meta, false);
                }

                public int getMeta(RailMode value, int existing) {
                    return getRailMeta(value, getRailDirection(existing, false), false, isRailPowered(existing));
                }

                @Override
                public RailMode parse(String text) throws Exception {
                    return RailMode.parse(text);
                }
            }
        );

        registerProperty(
            Arrays.asList(Blocks.golden_rail, Blocks.detector_rail, Blocks.activator_rail),
            DirectionBlockProperty.facing(
                (dir, existing) -> getRailMeta(getRailMode(existing, false), dir, false, isRailPowered(existing)),
                meta -> getRailDirection(meta, false)
            )
        );

        BooleanProperty powered = BooleanProperty.flag("powered", 0b1000);

        registerProperty(
            Arrays.asList(Blocks.golden_rail, Blocks.detector_rail, Blocks.activator_rail),
            powered
        );

        registerBlockInterfaceProperty(BlockButton.class, powered);
        registerBlockInterfaceProperty(BlockButton.class, DirectionBlockProperty.facing(0b111, 3, 4, 1, 2, 0, 5));

        registerBlockInterfaceProperty(BlockTorch.class, DirectionBlockProperty.facing(-1, 3, 4, 1, 2, 0, 5));
        registerProperty(Blocks.redstone_torch, BooleanProperty.blocks("powered", Blocks.unlit_redstone_torch, Blocks.redstone_torch));
        registerProperty(Blocks.unlit_redstone_torch, BooleanProperty.blocks("powered", Blocks.unlit_redstone_torch, Blocks.redstone_torch));

        registerProperty(Blocks.lever, powered);
        registerProperty(Blocks.lever, new DirectionBlockProperty() {

            @Override
            public String getName() {
                return "forward";
            }

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                int meta = world.getBlockMetadata(x, y, z) & 0b111;
                return switch (meta) {
                    case 5 -> NORTH;
                    case 6 -> EAST;
                    case 0 -> EAST;
                    case 7 -> NORTH;
                    default -> UNKNOWN;
                };
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                int meta = world.getBlockMetadata(x, y, z);
                int power = meta & 0b1000;
                meta &= 0b111;

                if (meta == 0 || meta == 7) meta = value == EAST || value == WEST ? 0 : 7;
                if (meta == 5 || meta == 6) meta = value == EAST || value == WEST ? 6 : 5;

                world.setBlockMetadataWithNotify(x, y, z, meta + power, 2);
            }
        });

        registerProperty(
            Blocks.lever,
            DirectionBlockProperty.facing(
                (dir, existing) -> (existing & 0b1000) + switch (dir) {
                    case NORTH -> 3;
                    case SOUTH -> 4;
                    case WEST -> 1;
                    case EAST -> 2;
                    case UP -> (existing % 8 == 7) ? 7 : 0;
                    case DOWN -> (existing % 8 == 6) ? 6 : 5;
                    default -> 0;
                },
                meta -> switch (meta & 0b111) {
                    case 3 -> NORTH;
                    case 4 -> SOUTH;
                    case 1 -> WEST;
                    case 2 -> EAST;
                    case 0, 7 -> UP;
                    case 5, 6 -> DOWN;
                    default -> UNKNOWN;
                }
            )
        );

        registerBlockInterfaceProperty(BlockPistonBase.class, powered);
        registerBlockInterfaceProperty(BlockPistonBase.class, DirectionBlockProperty.facing(0b111, 3, 4, 1, 2, 0, 5));
        registerBlockInterfaceProperty(BlockPistonExtension.class, DirectionBlockProperty.facing(0b111, 3, 4, 1, 2, 0, 5));

        registerBlockInterfaceProperty(
            BlockSlab.class,
            new FlagBooleanProperty("top", 0b1000) {

                @Override
                public boolean appliesTo(Object obj) {
                    return (obj instanceof BlockSlab slab) && !slab.field_150004_a;
                }
            }
        );

        registerBlockInterfaceProperty(
            BlockStairs.class,
            DirectionBlockProperty.facing(
                0b11,
                dir -> switch (dir) {
                    case EAST -> 0;
                    case WEST -> 1;
                    case SOUTH -> 2;
                    case NORTH -> 3;
                    default -> 0;
                },
                meta -> switch (meta) {
                    case 0 -> EAST;
                    case 1 -> WEST;
                    case 2 -> SOUTH;
                    case 3 -> NORTH;
                    default -> NORTH;
                }
            )
        );

        registerBlockInterfaceProperty(
            BlockStairs.class,
            DirectionBlockProperty.facing(
                0b100,
                dir -> switch (dir) {
                    case UP -> 0;
                    case DOWN -> 0b100;
                    default -> 0;
                },
                meta -> switch (meta) {
                    case 0 -> UP;
                    case 0b100 -> DOWN;
                    default -> UP;
                }
            )
                .setName("up")
        );

        registerBlockInterfaceProperty(BlockChest.class, DirectionBlockProperty.facing(0b111, 2, 3, 4, 5, -1, -1));

        registerBlockInterfaceProperty(
            BlockAnvil.class,
            DirectionBlockProperty.facing(
                0b11,
                dir -> switch (dir) {
                    case WEST -> 0;
                    case NORTH -> 1;
                    case EAST -> 2;
                    case SOUTH -> 3;
                    default -> 0;
                },
                meta -> switch (meta) {
                    case 0 -> WEST;
                    case 1 -> NORTH;
                    case 2 -> EAST;
                    case 3 -> SOUTH;
                    default -> NORTH;
                }
            )
        );

        registerBlockInterfaceProperty(
            BlockAnvil.class,
            IntegerProperty.meta("damage", 0b1100, 2)
                .map(Arrays.asList("undamaged", "slightly_damaged", "very_damaged", "broken"))
        );

        registerProperty(Blocks.redstone_wire, IntegerProperty.meta("power", 0b1111, 0));

        registerBlockInterfaceProperty(BlockFurnace.class, DirectionBlockProperty.facing());
        registerBlockInterfaceProperty(BlockFurnace.class, BooleanProperty.blocks("lit", Blocks.furnace, Blocks.lit_furnace));

        registerProperty(Blocks.wall_sign, DirectionBlockProperty.facing());
        registerProperty(
            Blocks.standing_sign,
            new FloatProperty() {

                @Override
                public String getName() {
                    return "rotation";
                }

                @Override
                public float getFloat(World world, int x, int y, int z) {
                    return world.getBlockMetadata(x, y, z) * 360f / 16f;
                }

                @Override
                public void setFloat(World world, int x, int y, int z, float value) {
                    int meta = (Math.round(value * 16f / 360f) % 16 + 16) % 16;

                    world.setBlockMetadataWithNotify(x, y, z, meta, 2);
                }
            }
        );
        registerTileEntityInterfaceProperty(
            TileEntitySign.class,
            new BlockProperty<String>() {

                @Override
                public String getName() {
                    return "text";
                }

                @Override
                public String getValue(World world, int x, int y, int z) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileEntitySign sign)) return "";

                    return String.join("\n", sign.signText);
                }

                @Override
                public void setValue(World world, int x, int y, int z, String value) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileEntitySign sign)) return;

                    String[] text = value.split("\n");
                    sign.signText = new String[4];

                    for (int i = 0; i < 4; i++) {
                        String line = MMUtils.getIndexSafe(text, i);

                        if (line == null) line = "";
                        if (line.length() > 15) line = line.substring(0, 15);

                        sign.signText[i] = line;
                    }

                    sign.markDirty();
                    world.markBlockForUpdate(x, y, z);
                }

                @Override
                public String parse(String text) throws Exception {
                    return text;
                }
            }
        );

        registerBlockInterfaceProperty(
            BlockDoor.class,
            new DirectionBlockProperty() {

                @Override
                public String getName() {
                    return "facing";
                }

                @Override
                public ForgeDirection getValue(World world, int x, int y, int z) {
                    int meta = world.getBlockMetadata(x, y, z);
                    if (meta == 8) {
                        y--;
                        if (!(world.getBlock(x, y, z) instanceof BlockDoor)) return ForgeDirection.UNKNOWN;
                        meta = world.getBlockMetadata(x, y, z);
                    }
                    return switch (meta & 0b11) {
                        case 0 -> WEST;
                        case 1 -> NORTH;
                        case 2 -> EAST;
                        case 3 -> SOUTH;
                        default -> NORTH;
                    };
                }

                @Override
                public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                    int meta = world.getBlockMetadata(x, y, z);
                    if (meta == 8) {
                        y--;
                        if (!(world.getBlock(x, y, z) instanceof BlockDoor)) return;
                        meta = world.getBlockMetadata(x, y, z);
                    }

                    meta &= ~0b11;
                    meta |= switch (value) {
                        case WEST -> 0;
                        case NORTH -> 1;
                        case EAST -> 2;
                        case SOUTH -> 3;
                        default -> 1;
                    };

                    world.setBlockMetadataWithNotify(x, y, z, meta, 2);
                }
            }
        );
        registerBlockInterfaceProperty(
            BlockDoor.class,
            new BooleanProperty() {

                @Override
                public String getName() {
                    return "open";
                }

                @Override
                public boolean getBoolean(World world, int x, int y, int z) {
                    int meta = world.getBlockMetadata(x, y, z);
                    if (meta == 8) {
                        y--;
                        if (!(world.getBlock(x, y, z) instanceof BlockDoor)) return false;
                        meta = world.getBlockMetadata(x, y, z);
                    }
                    return meta >= 4;
                }

                @Override
                public void setBoolean(World world, int x, int y, int z, boolean value) {
                    int meta = world.getBlockMetadata(x, y, z);
                    if (meta == 8) {
                        y--;
                        if (!(world.getBlock(x, y, z) instanceof BlockDoor)) return;
                        meta = world.getBlockMetadata(x, y, z);
                    }

                    meta &= ~0b100;
                    if (value) meta |= 0b100;

                    world.setBlockMetadataWithNotify(x, y, z, meta, 2);
                }
            }
        );

        registerBlockInterfaceProperty(BlockLadder.class, DirectionBlockProperty.facing());

        registerBlockInterfaceProperty(BlockBasePressurePlate.class, BooleanProperty.flag("powered", 0b1));

        registerBlockInterfaceProperty(BlockPumpkin.class, DirectionBlockProperty.facing(0b11, 2, 0, 1, 3, -1, -1));

        registerProperty(
            Arrays.asList(Blocks.unpowered_repeater, Blocks.powered_repeater),
            BooleanProperty.blocks("powered", Blocks.unpowered_repeater, Blocks.powered_repeater)
        );
        registerProperty(
            Arrays.asList(Blocks.unpowered_repeater, Blocks.powered_repeater),
            DirectionBlockProperty.facing(0b11, 0, 2, 3, 1, -1, -1)
        );
        registerProperty(
            Arrays.asList(Blocks.unpowered_repeater, Blocks.powered_repeater),
            IntegerProperty.meta("delay", 0b1100, 2)
        );

        registerBlockInterfaceProperty(BlockTrapDoor.class, DirectionBlockProperty.facing(0b11, 0, 1, 2, 3, -1, -1));
        registerBlockInterfaceProperty(BlockTrapDoor.class, BooleanProperty.flag("open", 0b100));
        registerBlockInterfaceProperty(BlockTrapDoor.class, DirectionBlockProperty.facing(0b1000, -1, -1, -1, -1, 0b1000, 0).setName("up"));

        registerBlockInterfaceProperty(BlockRedstoneLight.class, BooleanProperty.blocks("powered", Blocks.redstone_lamp, Blocks.lit_redstone_lamp));

        registerProperty(Blocks.tripwire_hook, DirectionBlockProperty.facing(0b11, 0, 2, 3, 1, -1, -1));
        registerProperty(Blocks.tripwire_hook, powered);
        registerProperty(Blocks.tripwire_hook, BooleanProperty.flag("connected", 0b100));

        registerTileEntityInterfaceProperty(
            TileEntitySkull.class,
            new IntegerProperty() {

                @Override
                public String getName() {
                    return "rotation";
                }

                @Override
                public int getInt(World world, int x, int y, int z) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileEntitySkull skull)) return 0;

                    return skull.field_145910_i * 360 / 16;
                }

                @Override
                public void setInt(World world, int x, int y, int z, int value) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileEntitySkull skull)) return;

                    skull.field_145910_i = value * 16 / 360;

                    skull.markDirty();
                    world.markBlockForUpdate(x, y, z);
                }
            }
        );

        registerBlockInterfaceProperty(BlockDispenser.class, DirectionBlockProperty.facing());

        registerProperty(
            Arrays.asList(Blocks.unpowered_comparator, Blocks.powered_comparator),
            BooleanProperty.flag("powered", 0b1000)
        );
        registerProperty(
            Arrays.asList(Blocks.unpowered_comparator, Blocks.powered_comparator),
            DirectionBlockProperty.facing(0b11, 0, 2, 3, 1, -1, -1)
        );
        registerProperty(
            Arrays.asList(Blocks.unpowered_comparator, Blocks.powered_comparator),
            IntegerProperty.meta("mode", 0b100, 2)
                .map(Arrays.asList("comparator", "subtractor"))
        );

        registerBlockInterfaceProperty(BlockHopper.class, DirectionBlockProperty.facing());

        registerBlockInterfaceProperty(
            BlockFenceGate.class,
            DirectionBlockProperty.facing(0b11, 2, 0, 1, 3, -1, -1)
        );
        registerBlockInterfaceProperty(BlockFenceGate.class, BooleanProperty.flag("open", 0b100));
    }

    public static enum RailMode {

        NONE,
        ASCENDING,
        TURNED;

        public String toString() {
            return switch (this) {
                case NONE -> "none";
                case ASCENDING -> "ascending";
                case TURNED -> "turned";
            };
        }

        public static RailMode parse(String name) throws Exception {
            if ("turned".equals(name)) return TURNED;
            if ("ascending".equals(name)) return ASCENDING;
            if ("none".equals(name)) return NONE;
            throw new Exception("illegal rail mode: '" + name + "'");
        }
    }

    private static RailMode getRailMode(int meta, boolean canTurn) {
        return canTurn && meta >= 6 ? RailMode.TURNED : meta >= 2 ? RailMode.ASCENDING : RailMode.NONE;
    }

    private static ForgeDirection getRailDirection(int meta, boolean canTurn) {
        if (canTurn) {
            return switch (meta) {
                case 0, 4, 6 -> NORTH;
                case 1, 3, 9 -> WEST;
                case 2, 7 -> EAST;
                case 5, 8 -> SOUTH;
                default -> NORTH;
            };
        } else {
            return switch (meta) {
                case 0, 4 -> NORTH;
                case 1, 3 -> WEST;
                case 2 -> EAST;
                case 5 -> SOUTH;
                default -> NORTH;
            };
        }
    }

    private static boolean isRailPowered(int meta) {
        return meta >= 8;
    }

    private static int getRailMeta(RailMode mode, ForgeDirection dir, boolean canTurn, boolean powered) {
        if (canTurn && mode == RailMode.TURNED) {
            return switch (dir) {
                case NORTH -> 6;
                case WEST -> 9;
                case EAST -> 7;
                case SOUTH -> 8;
                default -> 0;
            };
        }

        if (mode == RailMode.ASCENDING) {
            return switch (dir) {
                case NORTH -> 4;
                case WEST -> 3;
                case EAST -> 2;
                case SOUTH -> 5;
                default -> 0;
            };
        }

        return switch (dir) {
            case NORTH, SOUTH -> 0;
            case EAST, WEST -> 1;
            default -> 0;
        };
    }

    // #endregion

    // #region Storage Drawers

    @Optional(Names.STORAGE_DRAWERS)
    private static void initStorageDrawers() {

        Class<?> clazz = null;

        try {
            clazz = Class.forName("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawers");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        registerTileEntityInterfaceProperty(clazz, methodIntDirectionTile(clazz, "getDirection", "setDirection"));
    }

    // #endregion

    // #region IC2

    @Optional(Names.INDUSTRIAL_CRAFT2)
    private static void initIC2() {
        registerTileEntityInterfaceProperty(
            ic2.api.tile.IWrenchable.class,
            new AbstractDirectionBlockProperty("facing") {

                @Override
                public ForgeDirection getValue(World world, int x, int y, int z) {
                    if (!(world.getTileEntity(x, y, z) instanceof IWrenchable wrenchable)) return ForgeDirection.UNKNOWN;

                    return ForgeDirection.getOrientation(wrenchable.getFacing());
                }

                @Override
                public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                    if (!(world.getTileEntity(x, y, z) instanceof IWrenchable wrenchable)) return;

                    wrenchable.setFacing((short) value.ordinal());
                }
            }
        );
    }

    // #endregion

    // #region Architecturecraft

    private static void initArch() {

        // spotless:off
        final ForgeDirection[][] FORWARDS = {
            {SOUTH, EAST, NORTH, WEST}, // down = DOWN
            {NORTH, EAST, SOUTH, WEST}, // down = UP
            {DOWN, EAST, UP, WEST}, // down = NORTH
            {DOWN, WEST, UP, EAST}, // down = SOUTH
            {DOWN, NORTH, UP, SOUTH}, // down = WEST
            {DOWN, SOUTH, UP, NORTH}, // down = EAST
        };
        // spotless:on

        registerTileEntityInterfaceProperty(
            TileArchitecture.class,
            new OrientationBlockProperty() {

                @Override
                public String getName() {
                    return "orientation";
                }

                @Override
                public Orientation getValue(World world, int x, int y, int z) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileArchitecture tile)) return Orientation.NONE;

                    return Orientation.getOrientation(
                        ForgeDirection.getOrientation(tile.side),
                        MMUtils.getIndexSafe(MMUtils.getIndexSafe(FORWARDS, tile.side), tile.turn)
                    );
                }

                @Override
                public void setValue(World world, int x, int y, int z, Orientation value) {
                    if (!(world.getTileEntity(x, y, z) instanceof TileArchitecture tile)) return;

                    if (value == null || value == Orientation.NONE || value.a == value.b || value.a.getOpposite() == value.b) value = Orientation.DOWN_NORTH;

                    int index = MMUtils.indexOf(MMUtils.getIndexSafe(FORWARDS, value.a.ordinal()), value.b);

                    if (index != -1) {
                        tile.turn = (byte) index;
                        tile.side = (byte) value.a.ordinal();
                    } else {
                        for (int side = 0; side < FORWARDS.length; side++) {
                            index = MMUtils.indexOf(FORWARDS[side], value);

                            if (index != -1) {
                                tile.side = (byte) side;
                                tile.turn = (byte) index;
                                break;
                            }
                        }
                    }

                    tile.markDirty();
                    world.markBlockForUpdate(x, y, z);
                }
            }
        );
    }

    // #endregion

    // #region FloodLights

    private static void initFloodLights() {
        registerTileEntityInterfaceProperty(TileEntityMetaFloodlight.class, new AbstractDirectionBlockProperty("facing") {

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return UNKNOWN;

                return floodlight.getOrientation();
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection forgeDirection) {
                // Do the same thing FloodLights do in `onBlockPlacedBy`
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return;

                floodlight.setOrientation(forgeDirection);

                if (!(floodlight instanceof TileEntitySmallFloodlight)) {
                    // copy rotation info into metadata because FloodLights does it too
                    world.setBlockMetadataWithNotify(x, y, z, forgeDirection.ordinal(), 2);
                } else {
                    // NB: small electric light does not use metadata for rotation
                    // instead, it uses it to discern normal/small floodlights
                    // so don't modify metadata, just update it
                    world.markBlockForUpdate(x, y, z);
                }
            }
        });

        registerTileEntityInterfaceProperty(TileEntityMetaFloodlight.class, new BooleanProperty() {

            @Override
            public String getName() {
                return "inverted";
            }

            @Override
            public boolean getBoolean(World world, int x, int y, int z) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return false;

                return floodlight.getInverted();
            }

            @Override
            public void setBoolean(World world, int x, int y, int z, boolean value) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return;

                if (floodlight.getInverted() != value) {
                    floodlight.toggleInverted();
                }
            }
        });

        registerTileEntityInterfaceProperty(TileEntityMetaFloodlight.class, new IntegerProperty() {

            @Override
            public String getName() {
                return "mode";
            }

            @Override
            public int getInt(World world, int x, int y, int z) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return 0;

                return floodlight.getMode();
            }

            @Override
            public void setInt(World world, int x, int y, int z, int value) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return;

                floodlight.setMode(value);
            }
        });

        registerTileEntityInterfaceProperty(TileEntityMetaFloodlight.class, new IntegerProperty() {

            @Override
            public String getName() {
                return "color";
            }

            @Override
            public int getInt(World world, int x, int y, int z) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return 0;

                return floodlight.getColor();
            }

            @Override
            public void setInt(World world, int x, int y, int z, int value) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntityMetaFloodlight floodlight)) return;

                floodlight.setColor(value);
            }
        });

        registerTileEntityInterfaceProperty(TileEntitySmallFloodlight.class, new BooleanProperty() {

            @Override
            public String getName() {
                return "rotation_state";
            }

            @Override
            public boolean getBoolean(World world, int x, int y, int z) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntitySmallFloodlight floodlight)) return false;

                return floodlight.getRotationState();
            }

            @Override
            public void setBoolean(World world, int x, int y, int z, boolean value) {
                if (!(world.getTileEntity(x, y, z) instanceof TileEntitySmallFloodlight floodlight)) return;

                floodlight.setRotationState(value);
                world.markBlockForUpdate(x, y, z);
            }
        });
    }

    // #endregion

    // #region GT5u

    @Optional(Names.GREG_TECH)
    private static void initGT5u() {
        registerIntrinsicProperty(GregTechAPI.sBlockMachines, new MEHatchCapacityProperty<>(MTEHatchOutputBusME.class));
        registerIntrinsicProperty(GregTechAPI.sBlockMachines, new MEHatchCapacityProperty<>(MTEHatchOutputME.class));
        registerIntrinsicProperty(GregTechAPI.sBlockMachines, new CALImprintProperty());
    }

    // #endregion

    // #region AE2 Stuff

    @Optional(Names.AE2STUFF)
    private static void initAE2Stuff() {
        registerIntrinsicProperty(InteropConstants.WIRELESS_CONNECTOR.get().getBlock(), new WirelessHubProperty());
    }

    // #endregion

    // #region Ender Storage

    @Optional(Names.ENDER_STORAGE)
    private static void initEnderStorage() {
        registerIntrinsicProperty(InteropConstants.ENDER_STORAGE.getBlock(), new EnderStorageTypeProperty());
        registerIntrinsicProperty(InteropConstants.ENDER_STORAGE.getBlock(), new EnderStorageFrequencyProperty());
        registerIntrinsicProperty(InteropConstants.ENDER_STORAGE.getBlock(), new EnderStoragePrivateProperty());

        registerProperty(
            InteropConstants.ENDER_STORAGE.getBlock(),
            new DirectionBlockProperty() {

                private static final ForgeDirection[] VALUES = {
                    SOUTH, WEST, NORTH, EAST
                };

                @Override
                public String getName() {
                    return "facing";
                }

                @Override
                public ForgeDirection getValue(World world, int x, int y, int z) {
                    TileEntity te = world.getTileEntity(x, y, z);

                    if (te instanceof TileEnderTank tank) return VALUES[tank.rotation];

                    if (te instanceof TileEnderChest chest) return VALUES[chest.rotation];

                    return UNKNOWN;
                }

                @Override
                public void setValue(World world, int x, int y, int z, ForgeDirection forgeDirection) {
                    int index = MMUtils.indexOf(VALUES, forgeDirection);

                    if (index != -1) {
                        TileEntity te = world.getTileEntity(x, y, z);

                        if (te instanceof TileEnderTank tank) {
                            tank.rotation = index;
                            tank.markDirty();
                            world.markBlockForUpdate(x, y, z);
                        }

                        if (te instanceof TileEnderChest chest) {
                            chest.rotation = index;
                            chest.markDirty();
                            world.markBlockForUpdate(x, y, z);
                        }
                    }
                }
            }
        );
    }

    // #endregion

    @SneakyThrows
    public static DirectionBlockProperty methodIntDirectionTile(Class<?> clazz, String getterName, String setterName) {
        Method getter = clazz.getDeclaredMethod(getterName);
        Method setter = clazz.getDeclaredMethod(setterName, int.class);

        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle getterHandle = lookup.unreflect(getter);
        MethodHandle setterHandle = lookup.unreflect(setter);

        interface Getter {

            int get(Object tile);
        }

        interface Setter {

            void set(Object tile, int side);
        }

        Getter getterFn = (Getter) LambdaMetafactory
            .metafactory(
                lookup,
                "get",
                MethodType.methodType(Getter.class),
                MethodType.methodType(int.class, Object.class),
                getterHandle,
                getterHandle.type()
            )
            .getTarget()
            .invokeExact();
        Setter setterFn = (Setter) LambdaMetafactory
            .metafactory(
                lookup,
                "set",
                MethodType.methodType(Setter.class),
                MethodType.methodType(void.class, Object.class, int.class),
                setterHandle,
                setterHandle.type()
            )
            .getTarget()
            .invokeExact();

        return new AbstractDirectionBlockProperty("facing") {

            @Override
            public ForgeDirection getValue(World world, int x, int y, int z) {
                TileEntity tile = world.getTileEntity(x, y, z);

                return ForgeDirection.getOrientation(getterFn.get(tile));
            }

            @Override
            public void setValue(World world, int x, int y, int z, ForgeDirection value) {
                TileEntity tile = world.getTileEntity(x, y, z);

                setterFn.set(tile, value.ordinal());

                tile.markDirty();
                world.markBlockForUpdate(x, y, z);
            }
        };
    }

    public static abstract class IntrinsicMTEProperty<MTE extends IMetaTileEntity> implements IntrinsicProperty {

        public final Class<MTE> clazz;

        public IntrinsicMTEProperty(Class<MTE> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean hasValue(ItemStack stack) {
            if (stack == null) return false;
            if (!(stack.getItem() instanceof ItemMachines machines)) return false;

            var mte = MMUtils.getIndexSafe(GregTechAPI.METATILEENTITIES, machines.getDamage(stack));

            if (mte == null) return false;

            return clazz.isAssignableFrom(mte.getClass());
        }

        @Override
        public boolean hasValue(IBlockAccess world, int x, int y, int z) {
            if (!(world.getTileEntity(x, y, z) instanceof IGregTechTileEntity igte)) return false;

            IMetaTileEntity mte = igte.getMetaTileEntity();

            if (mte == null || igte.isDead()) return false;

            return clazz.isAssignableFrom(mte.getClass());
        }

        @Override
        public JsonElement getValue(IBlockAccess world, int x, int y, int z) {
            if (!(world.getTileEntity(x, y, z) instanceof IGregTechTileEntity igte)) return null;

            IMetaTileEntity mte = igte.getMetaTileEntity();

            if (mte == null || igte.isDead()) return null;
            if (!clazz.isAssignableFrom(mte.getClass())) return null;

            MTE casted = clazz.cast(mte);

            return getValue(casted);
        }

        @Override
        public void setValue(IBlockAccess world, int x, int y, int z, JsonElement value) {
            if (!(world.getTileEntity(x, y, z) instanceof IGregTechTileEntity igte)) return;

            IMetaTileEntity mte = igte.getMetaTileEntity();

            if (mte == null || igte.isDead()) return;
            if (!clazz.isAssignableFrom(mte.getClass())) return;

            MTE casted = clazz.cast(mte);

            setValue(casted, value);
        }

        public abstract JsonElement getValue(MTE mte);

        public abstract void setValue(MTE mte, JsonElement value);

        @Override
        public JsonElement getValue(ItemStack stack) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag == null) return null;

            return getValue(tag);
        }

        @Override
        public void setValue(ItemStack stack, JsonElement value) {
            setValue(stack.getTagCompound(), value);
        }

        public abstract JsonElement getValue(NBTTagCompound itemTag);

        public abstract void setValue(NBTTagCompound itemTag, JsonElement value);
    }

    private static class MEHatchCapacityProperty<MTE extends IMetaTileEntity> extends IntrinsicMTEProperty<MTE> {

        public static final String KEY = "baseCapacity";
        public static final int BASE_CAPACITY = 1600;

        public MEHatchCapacityProperty(Class<MTE> clazz) {
            super(clazz);
        }

        @Override
        public String getName() {
            return "capacity";
        }

        @Override
        public JsonElement getValue(MTE mte) {
            NBTTagCompound tag = new NBTTagCompound();
            mte.setItemNBT(tag);
            long capacity = tag.hasKey(KEY) ? tag.getLong(KEY) : BASE_CAPACITY;
            return new JsonPrimitive(capacity);
        }

        @Override
        public void setValue(MTE mte, JsonElement value) {
            throw new UnsupportedOperationException("bus capacity is fixed and cannot be changed");
        }

        @Override
        public JsonElement getValue(NBTTagCompound itemTag) {
            long capacity = itemTag.hasKey(KEY) ? itemTag.getLong(KEY) : BASE_CAPACITY;
            return new JsonPrimitive(capacity);
        }

        @Override
        public void setValue(NBTTagCompound itemTag, JsonElement value) {
            if (value.getAsLong() == BASE_CAPACITY) {
                itemTag.removeTag(KEY);
            } else {
                itemTag.setLong(KEY, value.getAsLong());
            }
        }

        @Override
        public void getItemDetails(List<String> details, JsonElement value) {
            ReadableNumberConverter nc = ReadableNumberConverter.INSTANCE;
            details.add(String.format("Cache Capacity: %s", nc.toWideReadableForm(value.getAsLong())));
        }
    }

    private static class WirelessHubProperty implements IntrinsicProperty {

        @Override
        public String getName() {
            return "isHub";
        }

        @Override
        public boolean hasValue(ItemStack stack) {
            return InteropConstants.WIRELESS_CONNECTOR.matches(MMUtils.getBlockFromItem(stack.getItem(), stack.itemDamage), WILDCARD_VALUE);
        }

        @Override
        public boolean hasValue(IBlockAccess world, int x, int y, int z) {
            return world.getTileEntity(x, y, z) instanceof TileWireless;
        }

        @Override
        public JsonElement getValue(ItemStack stack) {
            return new JsonPrimitive(stack.itemDamage >= 17);
        }

        @Override
        public JsonElement getValue(IBlockAccess world, int x, int y, int z) {
            TileWireless te = (TileWireless) world.getTileEntity(x, y, z);

            return new JsonPrimitive(te.isHub());
        }

        @Override
        public void setValue(ItemStack stack, JsonElement value) {
            stack.itemDamage = (stack.itemDamage % 17) + (value.getAsBoolean() ? 17 : 0);
        }

        @Override
        public void setValue(IBlockAccess world, int x, int y, int z, JsonElement value) {
            throw new UnsupportedOperationException("hub status is fixed and cannot be changed");
        }
    }

    private static class CALImprintProperty extends IntrinsicMTEProperty<MTECircuitAssemblyLine> {

        private static final MethodHandle GET_CAL_TYPE = MMUtils.exposeFieldGetter(MTECircuitAssemblyLine.class, "type");

        @SneakyThrows
        public static NBTTagCompound getCALType(MTECircuitAssemblyLine cal) {
            return (NBTTagCompound) GET_CAL_TYPE.invokeExact(cal);
        }

        public CALImprintProperty() {
            super(MTECircuitAssemblyLine.class);
        }

        @Override
        public String getName() {
            return "imprint";
        }

        @Override
        public JsonElement getValue(MTECircuitAssemblyLine mte) {
            NBTTagCompound imprint = getCALType(mte);

            if (imprint == null) return null;

            return MMUtils.toJsonObjectExact(imprint);
        }

        @Override
        public void setValue(MTECircuitAssemblyLine mte, JsonElement value) {
            throw new UnsupportedOperationException("imprint is fixed and cannot be changed");
        }

        @Override
        public JsonElement getValue(NBTTagCompound itemTag) {
            if (itemTag == null || !itemTag.hasKey(MTECircuitAssemblyLine.IMPRINT_KEY)) return null;

            return MMUtils.toJsonObjectExact(itemTag.getCompoundTag(MTECircuitAssemblyLine.IMPRINT_KEY));
        }

        @Override
        public void setValue(NBTTagCompound itemTag, JsonElement value) {
            if (value == null) {
                itemTag.removeTag(MTECircuitAssemblyLine.IMPRINT_KEY);
            } else {
                itemTag.setTag(MTECircuitAssemblyLine.IMPRINT_KEY, MMUtils.toNbtExact(value));
            }
        }

        @Override
        public void getItemDetails(List<String> details, JsonElement value) {
            ItemStack stack = null;

            if (value != null) {
                stack = ItemStack.loadItemStackFromNBT((NBTTagCompound) MMUtils.toNbtExact(value));
            }

            details.add(String.format("Imprint: %s", stack == null ? "None" : stack.getDisplayName()));
        }
    }

    private static class EnderStorageTypeProperty implements IntrinsicProperty {

        @Override
        public String getName() {
            return "isTank";
        }

        @Override
        public boolean hasValue(ItemStack stack) {
            return InteropConstants.ENDER_STORAGE.matches(stack);
        }

        @Override
        public boolean hasValue(IBlockAccess world, int x, int y, int z) {
            return InteropConstants.ENDER_STORAGE.getBlock() == world.getBlock(x, y, z);
        }

        @Override
        public JsonElement getValue(ItemStack stack) {
            int meta = stack.getItemDamage();

            return new JsonPrimitive(meta >= 4096);
        }

        @Override
        public JsonElement getValue(IBlockAccess world, int x, int y, int z) {
            return new JsonPrimitive(world.getTileEntity(x, y, z) instanceof TileEnderTank);
        }

        @Override
        public void setValue(ItemStack stack, JsonElement value) {
            int meta = stack.getItemDamage();

            meta %= 4096;

            if (value.getAsBoolean()) meta += 4096;

            stack.setItemDamage(meta);
        }

        @Override
        public void setValue(IBlockAccess world, int x, int y, int z, JsonElement value) {
            throw new UnsupportedOperationException("Tank status is immutable for in-world blocks");
        }
    }

    private static class EnderStorageFrequencyProperty implements IntrinsicProperty {

        @Override
        public String getName() {
            return "freq";
        }

        @Override
        public boolean hasValue(ItemStack stack) {
            return InteropConstants.ENDER_STORAGE.matches(stack);
        }

        @Override
        public boolean hasValue(IBlockAccess world, int x, int y, int z) {
            return InteropConstants.ENDER_STORAGE.getBlock() == world.getBlock(x, y, z);
        }

        @Override
        public JsonElement getValue(ItemStack stack) {
            return new JsonPrimitive(stack.getItemDamage() % 4096);
        }

        @Override
        public JsonElement getValue(IBlockAccess world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof TileFrequencyOwner tfo) {
                return new JsonPrimitive(tfo.freq);
            } else {
                throw new IllegalStateException("expected " + te + " to be a TileFrequencyOwner");
            }
        }

        @Override
        public void setValue(ItemStack stack, JsonElement value) {
            int meta = stack.getItemDamage();

            meta &= 4096;
            meta |= value.getAsInt() & 4095;

            stack.setItemDamage(meta);
        }

        @Override
        public void setValue(IBlockAccess world, int x, int y, int z, JsonElement value) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof TileFrequencyOwner tfo) {
                tfo.freq = value.getAsInt();
                tfo.reloadStorage();
                tfo.markDirty();

                if (world instanceof World w) w.markBlockForUpdate(x, y, z);
            } else {
                throw new IllegalStateException("expected " + te + " to be a TileFrequencyOwner");
            }
        }
    }

    private static class EnderStoragePrivateProperty implements IntrinsicProperty {

        @Override
        public String getName() {
            return "owner";
        }

        @Override
        public boolean hasValue(ItemStack stack) {
            return InteropConstants.ENDER_STORAGE.matches(stack);
        }

        @Override
        public boolean hasValue(IBlockAccess world, int x, int y, int z) {
            return world.getTileEntity(x, y, z) instanceof TileFrequencyOwner;
        }

        @Override
        public JsonElement getValue(ItemStack stack) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag == null || !tag.hasKey("owner", Constants.NBT.TAG_STRING)) return JsonNull.INSTANCE;

            return new JsonPrimitive(tag.getString("owner"));
        }

        @Override
        public JsonElement getValue(IBlockAccess world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(x, y, z);

            if (te instanceof TileFrequencyOwner tfo) {
                return "global".equals(tfo.owner) ? JsonNull.INSTANCE : new JsonPrimitive(tfo.owner);
            } else {
                throw new IllegalStateException("expected " + te + " to be a TileFrequencyOwner");
            }
        }

        @Override
        public void setValue(ItemStack stack, JsonElement value) {
            String owner = value == null || value.isJsonNull() ? "global" : value.getAsString();

            if (!owner.equals("global")) {
                if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

                stack.getTagCompound().setString("owner", owner);
            } else {
                if (stack.hasTagCompound()) {
                    stack.getTagCompound().removeTag("owner");
                }
            }
        }

        @Override
        public void setValue(IBlockAccess world, int x, int y, int z, JsonElement value) {
            throw new UnsupportedOperationException("Owner is immutable for in-world blocks");
        }
    }
}
