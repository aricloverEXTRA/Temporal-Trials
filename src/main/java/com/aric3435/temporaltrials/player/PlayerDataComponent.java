package com.aric3435.temporaltrials.player;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

public class PlayerDataComponent {

    private DefaultedList<ItemStack> savedInventory =
            DefaultedList.ofSize(36, ItemStack.EMPTY);

    private long lastFluteUseTick = -1;

    public DefaultedList<ItemStack> getSavedInventory() {
        return savedInventory;
    }

    public void saveInventory(DefaultedList<ItemStack> inv) {
        for (int i = 0; i < inv.size(); i++) {
            savedInventory.set(i, inv.get(i).copy());
        }
    }

    public long getLastFluteUseTick() {
        return lastFluteUseTick;
    }

    public void setLastFluteUseTick(long tick) {
        this.lastFluteUseTick = tick;
    }

    // Disabled for 0.1.0-beta (NBT API changed in 1.21.4)
    public void writeToNbt(NbtCompound nbt) { }

    public void readFromNbt(NbtCompound nbt) { }
}