package com.hydroponicraft.menu;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.blockentity.GatheringChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GatheringChestMenu extends AbstractContainerMenu {

    private final GatheringChestBlockEntity blockEntity;
    public final BlockPos blockPos;

    // Server-side constructor
    public GatheringChestMenu(int containerId, Inventory playerInventory, GatheringChestBlockEntity be) {
        super(HydroponiCraftRegistry.GATHERING_CHEST_MENU.get(), containerId);
        this.blockEntity = be;
        this.blockPos = be.getBlockPos();

        // Storage slots 0-53 (6 rows x 9 cols)
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(be.itemHandler, row * 9 + col,
                        8 + col * 18, 18 + row * 18));
            }
        }

        // Filter slots 54-62 (1 row x 9 cols)
        for (int col = 0; col < 9; col++) {
            addSlot(new SlotItemHandler(be.filterHandler, col, 8 + col * 18, 140));
        }

        // Player main inventory 63-89
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 172 + row * 18));
            }
        }

        // Hotbar 90-98
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 230));
        }
    }

    // Client-side constructor — reads block pos from network buffer to look up BE
    public GatheringChestMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, buf));
    }

    private static GatheringChestBlockEntity getBlockEntity(Inventory inv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = inv.player.level().getBlockEntity(pos);
        if (be instanceof GatheringChestBlockEntity gcbe) return gcbe;
        throw new IllegalStateException("No GatheringChestBlockEntity at " + pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < 54) {
            // Storage → player inventory
            if (!moveItemStackTo(stack, 63, 99, true)) return ItemStack.EMPTY;
        } else if (index < 63) {
            // Filter → player inventory
            if (!moveItemStackTo(stack, 63, 99, true)) return ItemStack.EMPTY;
        } else {
            // Player inventory → storage
            if (!moveItemStackTo(stack, 0, 54, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }
}
