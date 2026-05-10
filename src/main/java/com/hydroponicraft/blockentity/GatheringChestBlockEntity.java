package com.hydroponicraft.blockentity;

import com.hydroponicraft.GatheringChestManager;
import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.menu.GatheringChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.UUID;

public class GatheringChestBlockEntity extends BlockEntity implements MenuProvider, Container {

    private static final int SLOTS = 108;
    private static final int FILTER_SLOTS = 9;

    public final ItemStackHandler itemHandler = new ItemStackHandler(SLOTS);
    public final ItemStackHandler filterHandler = new ItemStackHandler(FILTER_SLOTS);

    @Nullable private UUID ownerUUID;
    @Nullable private DyeColor linkedColor;

    public GatheringChestBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.GATHERING_CHEST_BE.get(), pos, state);
    }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(@Nullable UUID uuid) { this.ownerUUID = uuid; }

    @Nullable public DyeColor getLinkedColor() { return linkedColor; }
    public void setLinkedColor(@Nullable DyeColor color) { this.linkedColor = color; }

    /**
     * Chest collects drops if:
     * - Owner matches
     * - Unlinked chest: catches all C4 from owner
     * - Linked chest (color X): catches uncolored C4 OR matching-color C4
     */
    public boolean matchesDetonation(@Nullable UUID detonationOwner, @Nullable DyeColor detonationColor) {
        if (ownerUUID == null || detonationOwner == null) return false;
        if (!ownerUUID.equals(detonationOwner)) return false;
        if (linkedColor == null) return true;           // unlinked: catch all
        if (detonationColor == null) return true;       // uncolored C4: any chest catches it
        return linkedColor == detonationColor;
    }

    /** Returns true if this item type is in the void filter. */
    public boolean isVoided(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int i = 0; i < FILTER_SLOTS; i++) {
            ItemStack filter = filterHandler.getStackInSlot(i);
            if (!filter.isEmpty() && filter.getItem() == stack.getItem()) return true;
        }
        return false;
    }

    /** Inserts a stack into the inventory, returns leftovers. */
    public ItemStack insertItem(ItemStack stack) {
        for (int i = 0; i < SLOTS && !stack.isEmpty(); i++) {
            stack = itemHandler.insertItem(i, stack, false);
        }
        return stack;
    }

    // ── Tracking ──────────────────────────────────────────────────────────────

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            GatheringChestManager.trackChest(level, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            GatheringChestManager.untrackChest(level, worldPosition);
        }
        super.setRemoved();
    }

    // ── Container ─────────────────────────────────────────────────────────────

    // GUI shows a standard 6-row chest (54 slots); all 108 slots are accessible via IItemHandler.
    @Override public int getContainerSize() { return 54; }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < SLOTS; i++) if (!itemHandler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { return itemHandler.getStackInSlot(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return itemHandler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = itemHandler.getStackInSlot(slot).copy();
        itemHandler.setStackInSlot(slot, ItemStack.EMPTY);
        return s;
    }

    @Override public void setItem(int slot, ItemStack stack) { itemHandler.setStackInSlot(slot, stack); }
    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void clearContent() {
        for (int i = 0; i < SLOTS; i++) itemHandler.setStackInSlot(i, ItemStack.EMPTY);
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hydroponicraft.gathering_chest");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GatheringChestMenu(containerId, playerInventory, this);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.put("FilterInventory", filterHandler.serializeNBT(registries));
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (linkedColor != null) tag.putString("LinkedColor", linkedColor.getName());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        if (tag.contains("FilterInventory")) {
            filterHandler.deserializeNBT(registries, tag.getCompound("FilterInventory"));
        }
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        linkedColor = EnderC4BlockEntity.colorFromName(tag.getString("LinkedColor"));
    }
}
