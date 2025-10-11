package com.recursive_pineapple.matter_manipulator.common.entities;

import java.util.Iterator;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

public class EntityItemLarge extends EntityItem {

    public EntityItemLarge(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);

        motionX = 0;
        motionY = 0;
        motionZ = 0;
    }

    public static void registerCommon() {
        EntityRegistry.registerModEntity(
            EntityItemLarge.class,
            "EntityItemLarge",
            EntityID.LargeItem.ID,
            Mods.MatterManipulator.ID,
            64,
            3,
            true
        );
    }

    @SideOnly(Side.CLIENT)
    public static void registerClient() {
        RenderingRegistry.registerEntityRenderingHandler(EntityItemLarge.class, new RenderItem());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);

        ItemStack item = this.getEntityItem();
        if (item != null) {
            tagCompound.getCompoundTag("Item")
                .setInteger("Count", item.stackSize);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);

        isDead = false;

        NBTTagCompound itemTag = tagCompound.getCompoundTag("Item");
        ItemStack item = ItemStack.loadItemStackFromNBT(itemTag);
        item.stackSize = itemTag.getInteger("Count");

        this.setEntityItemStack(item);

        item = getDataWatcher().getWatchableObjectItemStack(10);

        if (item == null || item.stackSize <= 0) {
            this.setDead();
        }
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (!this.worldObj.isRemote) {
            if (this.delayBeforeCanPickup > 0) { return; }

            EntityItemPickupEvent event = new EntityItemPickupEvent(player, this);

            if (MinecraftForge.EVENT_BUS.post(event)) { return; }

            player.openContainer.detectAndSendChanges();
            player.inventoryContainer.detectAndSendChanges();

            ItemStack itemstack = this.getEntityItem();
            int i = itemstack.stackSize;

            if (
                this.delayBeforeCanPickup <= 0 &&
                    (func_145798_i() == null || lifespan - this.age <= 200 || func_145798_i().equals(player.getCommandSenderName()))
            ) {
                player.inventory.addItemStackToInventory(itemstack);

                if (i == itemstack.stackSize) {
                    // couldn't store any items
                    return;
                }

                setEntityItemStack(itemstack);

                FMLCommonHandler.instance()
                    .firePlayerItemPickupEvent(player, this);

                this.worldObj.playSoundAtEntity(
                    player,
                    "random.pop",
                    0.2F,
                    ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F
                );

                // this just barely doesn't work, but it fixes the desync mostly so it's good enough
                player.openContainer.detectAndSendChanges();
                player.inventoryContainer.detectAndSendChanges();

                if (itemstack.stackSize <= 0) {
                    player.onItemPickup(this, i - itemstack.stackSize);
                    this.setDead();
                }
            }
        }
    }

    @Override
    public boolean combineItems(EntityItem other) {
        if (other == this) return false;
        if (!other.isEntityAlive() || !this.isEntityAlive()) return false;
        if (!(other instanceof EntityItemLarge)) return false;

        ItemStack ours = this.getEntityItem();
        ItemStack theirs = other.getEntityItem();

        if (!MMUtils.areStacksBasicallyEqual(ours, theirs)) { return false; }

        if (theirs.stackSize < ours.stackSize) { return other.combineItems(this); }

        theirs.stackSize += ours.stackSize;
        other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, this.delayBeforeCanPickup);
        other.age = Math.min(other.age, this.age);
        other.setEntityItemStack(theirs);
        this.setDead();

        return true;
    }

    /**
     * Looks for other itemstacks nearby and tries to stack them together
     */
    public void searchForOtherItemsNearbyCustom() {
        Iterator<EntityItemLarge> iterator = this.worldObj.getEntitiesWithinAABB(EntityItemLarge.class, this.boundingBox.expand(2D, 0.0D, 2D)).iterator();

        while (iterator.hasNext()) {
            this.combineItems(iterator.next());
        }

        Iterator<EntityPlayer> iterator2 = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, this.boundingBox.expand(2D, 0.0D, 2D)).iterator();

        while (iterator2.hasNext()) {
            this.onCollideWithPlayer(iterator2.next());
        }
    }
}
