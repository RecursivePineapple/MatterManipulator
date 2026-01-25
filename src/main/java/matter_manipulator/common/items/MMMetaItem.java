package matter_manipulator.common.items;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import matter_manipulator.Tags;

@ParametersAreNonnullByDefault
public class MMMetaItem extends Item {

    public final String name;

    public final Int2ObjectMap<ModelResourceLocation> models = new Int2ObjectOpenHashMap<>();
    public final Int2ObjectMap<IDMetaItem> metaItems = new Int2ObjectOpenHashMap<>();

    public MMMetaItem(String name) {
        this.name = name;

        setHasSubtypes(true);
        setMaxDamage(0);
        setTranslationKey("mm.metaitem");
        setRegistryName(Tags.MODID, "metaitem");

        for (IDMetaItem id : IDMetaItem.values()) {
            metaItems.put(id.ID, id);
            id.container.set(this, id.ID);
        }
    }

    @Override
    public @NotNull String getTranslationKey(ItemStack stack) {
        return "item." + name + "." + getDamage(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        int meta = getDamage(stack);

        String descKey = "item." + name + "." + meta + ".desc";

        if (I18n.hasKey(descKey)) {
            tooltip.add(I18n.format(descKey));
        }

        MMUpgrades upgrade = MMUpgrades.UPGRADES_BY_META.get(meta);

        if (upgrade != null) {
            tooltip.add(I18n.format("mm.upgrade.hint"));

            for (ManipulatorTier tier : ManipulatorTier.values()) {
                if (tier.allowedUpgrades.contains(upgrade)) {
                    tooltip.add("- " + tier.container.stack.getDisplayName());
                }
            }
        }
    }

    protected static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing",
        "inventory");

    @SideOnly(Side.CLIENT)
    public void configureModels() {
        for (IDMetaItem id : IDMetaItem.values()) {
            ModelResourceLocation loc = new ModelResourceLocation(Tags.MODID + ":metaitem/" + id.ID, "inventory");

            models.put(id.ID, loc);

            ModelBakery.registerItemVariants(this, loc);
        }

        models.defaultReturnValue(MISSING_LOCATION);

        ModelLoader.setCustomMeshDefinition(this, itemStack -> models.get(itemStack.getItemDamage()));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (IDMetaItem id : IDMetaItem.values()) {
            items.add(new ItemStack(this, 1, id.ID));
        }
    }
}
