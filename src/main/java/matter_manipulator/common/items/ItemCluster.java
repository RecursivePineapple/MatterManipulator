package matter_manipulator.common.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import matter_manipulator.Tags;

public class ItemCluster extends Item {

    private static ItemCluster instance;

    public ItemCluster() {
        instance = this;

        setCreativeTab(CreativeTabs.TOOLS);
        setRegistryName(Tags.MODID, "cluster");
        setTranslationKey("cluster");
        setMaxStackSize(1);
    }

    public static ItemStack getContents(ItemStack cluster) {
        if (!(cluster.getItem() instanceof ItemCluster)) return ItemStack.EMPTY;

        NBTTagCompound tag = cluster.getTagCompound();

        if (tag == null || tag.isEmpty()) return ItemStack.EMPTY;

        return new ItemStack(tag);
    }

    public static ItemStack makeCluster(ItemStack contents) {
        ItemStack stack = new ItemStack(instance, 1);

        stack.setTagCompound(contents.serializeNBT());

        return stack;
    }
}
