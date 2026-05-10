package com.hydroponicraft.entity;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.block.C4Block;
import com.hydroponicraft.block.EnderC4Block;
import com.hydroponicraft.blockentity.EnderC4BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class EnderPearlLauncherCart extends AbstractMinecart implements Container, MenuProvider {

    private static final int SLOTS = 27;

    private int depth = 10;
    @Nullable private UUID ownerUUID;
    @Nullable private BlockPos lastFiredRailPos;

    public final ItemStackHandler inventory = new ItemStackHandler(SLOTS);

    public EnderPearlLauncherCart(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }

    @Override
    protected net.minecraft.world.item.Item getDropItem() {
        return HydroponiCraftRegistry.ENDER_PEARL_LAUNCHER_CART_ITEM.get();
    }

    // ── MenuProvider ──────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.literal("Launcher Cart (depth: " + depth + ")");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return ChestMenu.threeRows(syncId, playerInventory, this);
    }

    // ── Container ─────────────────────────────────────────────────────────────

    @Override public int getContainerSize() { return SLOTS; }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < SLOTS; i++) if (!inventory.getStackInSlot(i).isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = inventory.getStackInSlot(slot).copy();
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return s;
    }

    @Override public void setItem(int slot, ItemStack stack) { inventory.setStackInSlot(slot, stack); }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public void setChanged() { /* entity auto-saves */ }
    @Override public void clearContent() {
        for (int i = 0; i < SLOTS; i++) inventory.setStackInSlot(i, ItemStack.EMPTY);
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        ownerUUID = player.getUUID();
        if (player.isShiftKeyDown()) {
            depth = depth > 1 ? depth - 1 : 50;
            player.displayClientMessage(
                    Component.literal("Launcher Cart depth: " + depth), true);
        } else {
            player.openMenu(this);
        }
        return InteractionResult.CONSUME;
    }

    // ── Tick / detector rail ─────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        BlockPos railPos = this.blockPosition();
        BlockState railState = level().getBlockState(railPos);
        if (railState.getBlock() instanceof DetectorRailBlock) {
            if (!railPos.equals(lastFiredRailPos)) {
                lastFiredRailPos = railPos;
                handleDetectorRail(railPos);
            }
        } else {
            lastFiredRailPos = null;
        }
    }

    private void handleDetectorRail(BlockPos railPos) {
        // Find an Ender C4 item in the cart's own inventory
        int foundSlot = -1;
        ItemStack foundStack = ItemStack.EMPTY;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack s = inventory.getStackInSlot(i);
            if (isEnderC4Item(s)) {
                foundSlot = i;
                foundStack = s.copy();
                break;
            }
        }
        if (foundSlot < 0 || foundStack.isEmpty()) return;

        // Take 1 item
        inventory.extractItem(foundSlot, 1, false);

        // Determine which EnderC4Block to place
        Block enderBlock = getEnderC4Block(foundStack);
        if (enderBlock == null) return;

        // Place at depth blocks below rail
        BlockPos placePos = railPos.below(depth);
        BlockState stateToPlace = enderBlock.defaultBlockState()
                .setValue(C4Block.FACING, Direction.UP);
        level().setBlock(placePos, stateToPlace, 3);

        // Set owner + color in block entity
        if (level().getBlockEntity(placePos) instanceof EnderC4BlockEntity ebe) {
            ebe.setOwner(ownerUUID);
            if (enderBlock instanceof EnderC4Block ec4b) {
                ebe.setColor(ec4b.getDyeColor());
            }
        }

        // Sound
        level().playSound(null, railPos, SoundEvents.ENDER_PEARL_THROW,
                SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private boolean isEnderC4Item(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == HydroponiCraftRegistry.ENDER_C4_ITEM.get()) return true;
        for (var h : HydroponiCraftRegistry.COLORED_ENDER_C4_ITEMS.values()) {
            if (item == h.get()) return true;
        }
        return false;
    }

    @Nullable
    private Block getEnderC4Block(ItemStack stack) {
        Item item = stack.getItem();
        if (item == HydroponiCraftRegistry.ENDER_C4_ITEM.get()) {
            return HydroponiCraftRegistry.ENDER_C4_BLOCK.get();
        }
        for (Map.Entry<DyeColor, ?> entry : HydroponiCraftRegistry.COLORED_ENDER_C4_ITEMS.entrySet()) {
            @SuppressWarnings("unchecked")
            var holder = (net.neoforged.neoforge.registries.DeferredHolder<Item, BlockItem>)
                    HydroponiCraftRegistry.COLORED_ENDER_C4_ITEMS.get(entry.getKey());
            if (holder != null && item == holder.get()) {
                var blockHolder = HydroponiCraftRegistry.COLORED_ENDER_C4_BLOCKS.get(entry.getKey());
                return blockHolder != null ? blockHolder.get() : null;
            }
        }
        return null;
    }

    // ── Drop inventory on death ───────────────────────────────────────────────

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide() && (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED)) {
            for (int i = 0; i < SLOTS; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) spawnAtLocation(stack);
            }
        }
        super.remove(reason);
    }

    // ── Synced data ───────────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        depth = tag.contains("Depth") ? Math.clamp(tag.getInt("Depth"), 1, 50) : 10;
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(level().registryAccess(), tag.getCompound("Inventory"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Depth", depth);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.put("Inventory", inventory.serializeNBT(level().registryAccess()));
    }
}
