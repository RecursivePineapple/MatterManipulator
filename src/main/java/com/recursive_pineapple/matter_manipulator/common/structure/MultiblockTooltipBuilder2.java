package com.recursive_pineapple.matter_manipulator.common.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import net.minecraft.client.resources.I18n;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.recursive_pineapple.matter_manipulator.common.structure.StructureWrapper.CasingInfo;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import gregtech.api.enums.HatchElement;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.util.MultiblockTooltipBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharComparator;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import tectech.thing.metaTileEntity.multi.base.TTMultiblockBase;

public class MultiblockTooltipBuilder2<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>>
    extends MultiblockTooltipBuilder {

    public final StructureWrapper<MTE> structure;

    private final Object2ObjectArrayMap<IHatchElement<? super MTE>, String> hatchNameOverrides = new Object2ObjectArrayMap<>(),
        hatchInfoOverrides = new Object2ObjectArrayMap<>();
    private final List<IHatchElement<? super MTE>> hatchOrder = new ArrayList<>();
    private boolean hasMultiampHatches = false, printMultiampSupport = true;

    public MultiblockTooltipBuilder2(StructureWrapper<MTE> structure) {
        this.structure = structure;
    }

    public MultiblockTooltipBuilder2<MTE> beginStructureBlock(boolean hollow) {
        super.beginStructureBlock(structure.size.x, structure.size.y, structure.size.z, hollow);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> beginStructureBlock() {
        super.beginStructureBlock(structure.size.x, structure.size.y, structure.size.z, false);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addCasing(ICasing casing) {
        structure.addCasingInfoAuto(this, casing);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addHatchNameOverride(IHatchElement<? super MTE> hatch, String newName) {
        hatchNameOverrides.put(hatch, newName);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addHatchLocationOverride(IHatchElement<? super MTE> hatch,
        String newLocation) {
        hatchInfoOverrides.put(hatch, newLocation);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> disableMultiAmpHatchLine() {
        printMultiampSupport = false;
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> setHatchOrder(List<IHatchElement<? super MTE>> hatches) {
        hatchOrder.clear();
        hatchOrder.addAll(hatches);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addHatch(ICasing casing, IHatchElement<? super MTE> hatch, int... dots) {
        String override = hatchNameOverrides.get(hatch);

        String info = hatchInfoOverrides.get(hatch);

        if (info == null) info = I18n.format("mm.structure.hatch-info", casing.getLocalizedName());

        if (dots != null && dots.length > 0) {
            info += I18n.format(
                "mm.structure.hatch-dots",
                String.join(", ", MMUtils.mapToList(new IntArrayList(dots), i -> i.toString())));
        }

        if (override != null) {
            addOtherStructurePart(override, info, dots);
        } else {
            if (hatch instanceof HatchElement gtHatch) {
                switch (gtHatch) {
                    case Dynamo:
                        addDynamoHatch(info, dots);
                        break;
                    case Energy:
                        addEnergyHatch(info, dots);
                        break;
                    case ExoticEnergy:
                        addDynamoHatch(info, dots);
                        addEnergyHatch(info, dots);
                        hasMultiampHatches = true;
                        break;
                    case InputBus:
                        addInputBus(info, dots);
                        break;
                    case InputHatch:
                        addInputHatch(info, dots);
                        break;
                    case Maintenance:
                        addMaintenanceHatch(info, dots);
                        break;
                    case Muffler:
                        addMufflerHatch(info, dots);
                        break;
                    case OutputBus:
                        addOutputBus(info, dots);
                        break;
                    case OutputHatch:
                        addOutputHatch(info, dots);
                        break;
                    default:
                        break;
                }
            } else if (hatch instanceof TTMultiblockBase.HatchElement ttHatch) {
                switch (ttHatch) {
                    case DynamoMulti:
                        addDynamoHatch(info, dots);
                        hasMultiampHatches = true;
                        break;
                    case EnergyMulti:
                        addEnergyHatch(info, dots);
                        hasMultiampHatches = true;
                        break;
                    case InputData:
                        addOtherStructurePart(I18n.format("tt.keyword.Structure.DataConnector"), info, dots);
                        break;
                    case OutputData:
                        addOtherStructurePart(I18n.format("tt.keyword.Structure.DataConnector"), info, dots);
                        break;
                    case Param:
                        addOtherStructurePart(I18n.format("gt.blockmachines.hatch.param.tier.05.name"), info, dots);
                        break;
                    case Uncertainty:
                        addOtherStructurePart(I18n.format("gt.blockmachines.hatch.certain.tier.07.name"), info, dots);
                        break;
                    default:
                        break;
                }
            }
        }

        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addAllCasingInfo() {
        addAllCasingInfo(null);
        return this;
    }

    public MultiblockTooltipBuilder2<MTE> addAllCasingInfo(List<ICasing> casingOrder) {
        ObjectArraySet<ICasing> addedCasings = new ObjectArraySet<>();

        CharList casings = new CharArrayList(structure.casings.keySet());

        if (casingOrder != null && !casingOrder.isEmpty()) {
            CharComparator comparator = (char a, char b) -> {
                int i1 = casingOrder.indexOf(structure.casings.get(a).casing);
                int i2 = casingOrder.indexOf(structure.casings.get(b).casing);

                if (i1 == -1 || i2 == -1) {
                    return -Integer.compare(i1, i2);
                } else {
                    return Integer.compare(i1, i2);
                }
            };

            casings.sort(comparator);
        } else {
            casings.sort(null);
        }

        Multimap<Pair<ICasing, IHatchElement<? super MTE>>, Integer> hatches = ArrayListMultimap.create();

        for (char c : casings) {
            CasingInfo<MTE> casingInfo = structure.casings.get(c);

            if (addedCasings.add(casingInfo.casing)) {
                structure.addCasingInfoAuto(this, casingInfo.casing);
            }

            if (casingInfo.hatches != null) {
                for (var hatch : casingInfo.hatches) {
                    hatches.put(Pair.of(casingInfo.casing, hatch), casingInfo.dot);
                }
            }
        }

        List<Pair<ICasing, IHatchElement<? super MTE>>> hatchesSorted = new ArrayList<>(hatches.keys());

        if (!hatchOrder.isEmpty()) {
            hatchesSorted.sort((p1, p2) -> {
                int i1 = hatchOrder.indexOf(p1.right());
                int i2 = hatchOrder.indexOf(p2.right());

                if (i1 == -1 || i2 == -1) {
                    return -Integer.compare(i1, i2);
                } else {
                    return Integer.compare(i1, i2);
                }
            });
        } else {
            ToIntFunction<Pair<ICasing, IHatchElement<? super MTE>>> comparator = p -> {
                if (p.right() instanceof HatchElement gtHatch) {
                    return gtHatch.ordinal();
                } else if (p.right() instanceof TTMultiblockBase.HatchElement ttHatch) {
                    return ttHatch.ordinal() + 100;
                } else {
                    return 200;
                }
            };

            Comparator<Pair<ICasing, IHatchElement<? super MTE>>> customComparator = Comparator
                .nullsFirst(Comparator.comparing(p -> hatchNameOverrides.get(p.right())));

            hatchesSorted.sort(
                Comparator.comparingInt(comparator)
                    .thenComparing(customComparator));
        }

        for (var hatch : hatchesSorted) {
            IntArrayList dots = new IntArrayList(new IntArraySet(hatches.get(hatch)));
            dots.sort(null);

            addHatch(hatch.left(), hatch.right(), dots.toIntArray());
        }

        if (printMultiampSupport && hasMultiampHatches) {
            addTecTechHatchInfo();
        }

        return this;
    }
}
