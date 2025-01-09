package com.recursive_pineapple.matter_manipulator.common.structure;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.recursive_pineapple.matter_manipulator.GlobalMMConfig;
import com.recursive_pineapple.matter_manipulator.MMMod;

import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntBinaryOperator;

/**
 * A wrapper for structure checking.
 * This should only be stored in the prototype MTE, then shared among the instance MTEs.
 */
public class StructureWrapper<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>> {

    public static final String STRUCTURE_PIECE_MAIN = "main";

    public final IStructureProvider<MTE> provider;

    public String[][] definitionText;

    public IStructureDefinition<MTE> structureDefinition;

    public Vector3i offset, size;
    public Char2ObjectArrayMap<CasingInfo<MTE>> casings;
    public Char2IntArrayMap casingCounts;

    public static class CasingInfo<MTE> {

        public int definitionCasingCount, maxHatches, dot;
        public ICasing casing;
        public IHatchElement<? super MTE>[] hatches;
    }

    public StructureWrapper(IStructureProvider<MTE> provider) {
        this.provider = provider;
    }

    public void loadStructure() {
        structureDefinition = null;

        try {
            definitionText = provider.getDefinition();
            casings = new Char2ObjectArrayMap<>();
            casingCounts = new Char2IntArrayMap();

            int width = 0;
            int height = 0;
            int length = definitionText.length;

            // find the controller offset and count the number of casings
            int z = 0;
            for (String[] a : definitionText) {
                int y = 0;
                height = Math.max(height, a.length);
                for (String b : a) {
                    width = Math.max(width, b.length());
                    for (int x = 0; x < b.length(); x++) {
                        char c = b.charAt(x);
                        if (c == ' ' || c == '-' || c == '+') continue;

                        casingCounts.mergeInt(c, 1, Integer::sum);

                        if (c == '~') {
                            offset = new Vector3i(x, y, z);
                        }
                    }
                    y++;
                }
                z++;
            }

            size = new Vector3i(width, height, length);

            if (offset == null) {
                throw new IllegalStateException(
                    "Structure definition for " + provider
                        + " did not contain a tilde! This is required so that the wrapper knows where the controller is.");
            }

            structureDefinition = provider.compile(definitionText);

            for (var e : casingCounts.char2IntEntrySet()) {
                CasingInfo<MTE> casing = casings.get(e.getCharKey());

                if (casing != null) casing.definitionCasingCount = e.getIntValue();
            }
        } catch (Throwable t) {
            MMMod.LOG.error("Could not compile structure", t);
        }
    }

    public boolean checkStructure(MTE instance) {
        loadStructure();

        if (!GlobalMMConfig.DEVENV) {
            return checkStructureImpl(instance);
        } else {
            try {
                return checkStructureImpl(instance);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                MMMod.LOG.info("Caught an exception that was probably caused by a hotswap.", ignored);

                loadStructure();

                return checkStructureImpl(instance);
            }
        }
    }

    private boolean checkStructureImpl(MTE instance) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        return structureDefinition.check(
            instance,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            !instance.mMachine);
    }

    public void construct(MTE instance, ItemStack trigger, boolean hintsOnly) {
        if (!GlobalMMConfig.DEVENV) {
            constructImpl(instance, trigger, hintsOnly);
        } else {
            try {
                constructImpl(instance, trigger, hintsOnly);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                MMMod.LOG.info("Caught an exception that was probably caused by a hotswap.", ignored);

                loadStructure();

                constructImpl(instance, trigger, hintsOnly);
            }
        }
    }

    private void constructImpl(MTE instance, ItemStack trigger, boolean hintsOnly) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        structureDefinition.buildOrHints(
            instance,
            trigger,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            hintsOnly);
    }

    public int survivalConstruct(MTE instance, ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (instance.mMachine) return -1;

        if (!GlobalMMConfig.DEVENV) {
            return survivalConstructImpl(instance, trigger, elementBudget, env);
        } else {
            try {
                return survivalConstructImpl(instance, trigger, elementBudget, env);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                MMMod.LOG.info("Caught an exception that was probably caused by a hotswap.", ignored);

                loadStructure();

                return survivalConstructImpl(instance, trigger, elementBudget, env);
            }
        }
    }

    private int survivalConstructImpl(MTE instance, ItemStack trigger, int elementBudget,
        ISurvivalBuildEnvironment env) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        int built = structureDefinition.survivalBuild(
            instance,
            trigger,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            elementBudget,
            env,
            false);

        if (built > 0) instance.checkStructure(true, tTile);

        return built;
    }

    public IStructureElement<MTE> getStructureElement(char c) {
        CasingInfo<MTE> casing = casings.get(c);

        if (casing.maxHatches > 0) {
            final IntBinaryOperator sum = Integer::sum;

            IStructureElement<MTE> adder = onElementPass(
                instance -> { instance.getWrapperInstanceInfo().actualCasingCounts.mergeInt(c, 1, sum); },
                casing.casing.asElement());

            return HatchElementBuilder.<MTE>builder()
                .atLeast(casing.hatches)
                .casingIndex(casing.casing.getTextureId())
                .dot(casing.dot)
                .buildAndChain(adder);
        } else {
            return casing.casing.asElement();
        }
    }

    public int getCasingMin(char c) {
        return getCasingMax(c) - casings.get(c).maxHatches;
    }

    public int getCasingMin(ICasing casing) {
        int sum = 0;

        for (var e : casings.char2ObjectEntrySet()) {
            if (e.getValue().casing == casing) {
                sum += getCasingMin(e.getCharKey());
            }
        }

        return sum;
    }

    public int getCasingMax(char c) {
        return casings.get(c).definitionCasingCount;
    }

    public int getCasingMax(ICasing casing) {
        int sum = 0;

        for (var e : casings.char2ObjectEntrySet()) {
            if (e.getValue().casing == casing) {
                sum += getCasingMax(e.getCharKey());
            }
        }

        return sum;
    }

    public StructureWrapper<MTE> addCasing(char c, ICasing casing) {
        Objects.requireNonNull(casing);

        CasingInfo<MTE> casingInfo = new CasingInfo<>();

        casingInfo.casing = casing;

        casings.put(c, casingInfo);
        return this;
    }

    @SuppressWarnings("unchecked")
    public StructureWrapper<MTE> addCasingWithHatches(char c, ICasing casing, int dot, int maxHatches,
        List<IHatchElement<? super MTE>> hatches) {
        Objects.requireNonNull(casing);
        Objects.requireNonNull(hatches);

        CasingInfo<MTE> casingInfo = new CasingInfo<>();

        casingInfo.casing = casing;
        casingInfo.dot = dot;
        casingInfo.maxHatches = maxHatches;
        casingInfo.hatches = hatches.toArray(new IHatchElement[0]);

        casings.put(c, casingInfo);
        return this;
    }

    public StructureDefinition.Builder<MTE> getStructureBuilder(List<Pair<String, String[][]>> pieces) {
        StructureDefinition.Builder<MTE> builder = StructureDefinition.builder();

        for (char casing : casings.keySet()) {
            builder.addElement(casing, getStructureElement(casing));
        }

        for (var piece : pieces) {
            builder.addShape(piece.left(), piece.right());
        }

        return builder;
    }

    public IStructureDefinition<MTE> buildStructure(String[][] definition) {
        return getStructureBuilder(Arrays.asList(Pair.of(StructureWrapper.STRUCTURE_PIECE_MAIN, definition))).build();
    }

    public StructureWrapper<MTE> addCasingInfoExact(MultiblockTooltipBuilder tt, ICasing casing) {
        tt.addCasingInfoExactly(casing.getLocalizedName(), getCasingMax(casing), false);
        return this;
    }

    public StructureWrapper<MTE> addCasingInfoRange(MultiblockTooltipBuilder tt, ICasing casing) {
        tt.addCasingInfoRange(casing.getLocalizedName(), getCasingMin(casing), getCasingMax(casing), false);
        return this;
    }

    public StructureWrapper<MTE> addCasingInfoAuto(MultiblockTooltipBuilder tt, ICasing casing) {
        if (getCasingMax(casing) != getCasingMin(casing)) {
            addCasingInfoRange(tt, casing);
        } else {
            addCasingInfoExact(tt, casing);
        }
        return this;
    }
}
