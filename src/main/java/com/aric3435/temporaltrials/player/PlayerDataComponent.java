package com.aric3435.temporaltrials.player;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

/**
 * PlayerDataComponent: Stores persistent player data across cycles.
 * Stores savedInventory and remainingLives for the current cycle.
 */
public class PlayerDataComponent {

    private DefaultedList<ItemStack> savedInventory =
            DefaultedList.ofSize(36, ItemStack.EMPTY);

    private long lastFluteUseTick = -1;

    // NEW: per-player remaining lives for the current cycle
    private int remainingLives = 1;

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

    // NEW: Remaining lives accessors
    public int getRemainingLives() {
        return remainingLives;
    }

    public void setRemainingLives(int lives) {
        this.remainingLives = Math.max(0, lives);
    }

    // NBT serialization stubs (implement if you want persistent storage)
    public void writeToNbt(NbtCompound nbt) {
        // TODO: Serialize saved inventory and remainingLives
    }

    public void readFromNbt(NbtCompound nbt) {
        // TODO: Deserialize saved inventory and remainingLives
    }
}