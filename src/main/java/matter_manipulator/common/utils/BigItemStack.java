package matter_manipulator.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.Optional.Method;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

import com.recursive_pineapple.matter_manipulator.common.utils.Mods.Names;

public class BigItemStack {

    public Item item;
    public long stackSize;
    public int meta;
    public NBTTagCompound tag;

    public transient ItemId id;

    public BigItemStack() {}

    private BigItemStack(ItemStack stack) {
        this.item = stack.getItem();
        this.stackSize = stack.stackSize;
        this.meta = Items.feather.getDamage(stack);
        this.tag = stack.getTagCompound();
    }

    private BigItemStack(ItemId id, long amount) {
        this(id.getItemStack());
        setStackSize(amount);
    }

    public ItemId getId() {
        if (id == null) {
            id = ItemId.create(item, meta, tag);
        }

        return id;
    }

    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(item, stackSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) stackSize, this.meta);
        stack.setTagCompound(tag == null ? null : (NBTTagCompound) tag.copy());
        return stack;
    }

    @Method(modid = Names.APPLIED_ENERGISTICS2)
    public IAEItemStack getAEItemStack() {
        return Objects.requireNonNull(AEItemStack.create(getItemStack())).setStackSize(stackSize);
    }

    public static BigItemStack create(ItemStack stack) {
        if (stack == null) return null;

        return new BigItemStack(stack);
    }

    public static BigItemStack create(ItemId id, long amount) {
        if (id == null) return null;

        return new BigItemStack(id, amount);
    }

    @Method(modid = Names.APPLIED_ENERGISTICS2)
    public static BigItemStack create(IAEItemStack stack) {
        if (stack == null) return null;

        return create(stack.getItemStack()).setStackSize(stack.getStackSize());
    }

    public ItemStack remove(int stackSize) {
        if (this.stackSize < stackSize) {
            ItemStack stack = new ItemStack(item, (int) this.stackSize, this.meta);
            stack.setTagCompound(tag == null ? null : (NBTTagCompound) tag.copy());
            this.stackSize = 0;
            return stack;
        } else {
            ItemStack stack = new ItemStack(item, stackSize, this.meta);
            stack.setTagCompound(tag == null ? null : (NBTTagCompound) tag.copy());
            this.stackSize -= stackSize;
            return stack;
        }
    }

    public BigItemStack removeBig(int stackSize) {
        long toRemove = Math.min(this.stackSize, stackSize);

        BigItemStack stack = copy().setStackSize(toRemove);
        this.stackSize -= toRemove;

        return stack;
    }

    public BigItemStack incStackSize(long stackSize) {
        this.stackSize += stackSize;
        return this;
    }

    public BigItemStack decStackSize(long stackSize) {
        this.stackSize -= stackSize;
        return this;
    }

    public List<ItemStack> toStacks() {
        List<ItemStack> stack = new ArrayList<>();

        while (this.stackSize > 0) {
            stack.add(remove(Integer.MAX_VALUE));
        }

        return stack;
    }

    public List<ItemStack> toStacks(int stackSize) {
        List<ItemStack> stack = new ArrayList<>();

        while (this.stackSize > 0) {
            stack.add(remove(stackSize));
        }

        return stack;
    }

    public BigItemStack copy() {
        BigItemStack out = new BigItemStack();

        out.item = item;
        out.stackSize = stackSize;
        out.meta = meta;
        out.tag = tag == null ? null : (NBTTagCompound) tag.copy();

        return out;
    }

    public BigItemStack setStackSize(long stackSize) {
        this.stackSize = stackSize;
        return this;
    }

    public long getStackSize() {
        return stackSize;
    }

    public Item getItem() {
        return item;
    }

    public int getItemDamage() {
        return meta;
    }

    public boolean hasSubtypes() {
        return item.getHasSubtypes();
    }

    public boolean isSameType(ItemStack other) {
        if (other == null) return false;

        return item == other.getItem() && meta == Items.feather.getDamage(other) && Objects.equals(tag, other.getTagCompound());
    }

    public boolean isSameType(BigItemStack other) {
        if (other == null) return false;

        return item == other.getItem() && meta == other.meta && Objects.equals(tag, other.tag);
    }
}
