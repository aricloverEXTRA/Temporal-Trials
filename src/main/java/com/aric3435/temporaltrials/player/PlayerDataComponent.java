package com.aric3435.temporaltrials.player;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

/**
 * PlayerDataComponent: Stores persistent player data across cycles.
 * 
 * Currently stores:
 * - savedInventory: Items player had when they last used Flute of Time
 * - lastFluteUseTick: When they last used the flute (for debugging)
 * 
 * In future versions:
 * - NBT serialization for permanent storage
 * - Difficulty-specific behavior
 */
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
        System.out.println("[TemporalTrials] Saved inventory with " + 
                         savedInventory.stream().filter(stack -> !stack.isEmpty()).count() + 
                         " item stacks");
    }

    public long getLastFluteUseTick() {
        return lastFluteUseTick;
    }

    public void setLastFluteUseTick(long tick) {
        this.lastFluteUseTick = tick;
    }

    // Disabled for 0.1.0-beta (NBT API changed in 1.21.4)
    // Re-enable in future versions for persistent storage
    public void writeToNbt(NbtCompound nbt) { 
        // TODO: Serialize savedInventory to NBT
    }

    public void readFromNbt(NbtCompound nbt) { 
        // TODO: Deserialize savedInventory from NBT
    }
}