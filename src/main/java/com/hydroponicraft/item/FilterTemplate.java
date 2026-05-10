package com.hydroponicraft.item;

import com.hydroponicraft.blockentity.GatheringChestBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class FilterTemplate extends Item {

    private static final int FILTER_SLOTS = 9;

    public FilterTemplate(Properties props) {
        super(props);
    }

    // ── NBT helpers ───────────────────────────────────────────────────────────

    private static boolean hasFilters(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        ListTag list = data.getUnsafe().getList("Filters", 8); // 8 = StringTag
        for (int i = 0; i < list.size(); i++) {
            if (!list.getString(i).isEmpty()) return true;
        }
        return false;
    }

    private static void saveFilters(ItemStack stack, ItemStackHandler handler) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < FILTER_SLOTS; i++) {
            ItemStack slot = handler.getStackInSlot(i);
            String id = slot.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(slot.getItem()).toString();
            list.add(StringTag.valueOf(id));
        }
        tag.put("Filters", list);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void applyFilters(ItemStack stack, ItemStackHandler handler) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return;
        ListTag list = data.getUnsafe().getList("Filters", 8);
        for (int i = 0; i < FILTER_SLOTS; i++) {
            String id = i < list.size() ? list.getString(i) : "";
            if (id.isEmpty()) {
                handler.setStackInSlot(i, ItemStack.EMPTY);
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                Item item = rl != null
                        ? BuiltInRegistries.ITEM.getOptional(rl).orElse(Items.AIR)
                        : Items.AIR;
                handler.setStackInSlot(i, item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item));
            }
        }
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        if (!hasFilters(stack)) {
            tooltip.add(Component.literal("Empty").withStyle(ChatFormatting.GRAY));
            return;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        ListTag list = data.getUnsafe().getList("Filters", 8);
        for (int i = 0; i < list.size(); i++) {
            String id = list.getString(i);
            if (!id.isEmpty()) {
                ResourceLocation rl = ResourceLocation.tryParse(id);
                Item item = rl != null
                        ? BuiltInRegistries.ITEM.getOptional(rl).orElse(Items.AIR)
                        : Items.AIR;
                if (item != Items.AIR) {
                    tooltip.add(new ItemStack(item).getHoverName().copy()
                            .withStyle(ChatFormatting.YELLOW));
                }
            }
        }
    }

    // ── Right-click on a Gathering Chest ──────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos pos = ctx.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof GatheringChestBlockEntity be)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = ctx.getItemInHand();
        var player = ctx.getPlayer();

        if (hasFilters(stack)) {
            applyFilters(stack, be.filterHandler);
            be.setChanged();
            if (player != null) {
                player.displayClientMessage(Component.literal("Filter applied"), true);
            }
        } else {
            boolean chestHasFilters = false;
            for (int i = 0; i < FILTER_SLOTS; i++) {
                if (!be.filterHandler.getStackInSlot(i).isEmpty()) {
                    chestHasFilters = true;
                    break;
                }
            }
            if (!chestHasFilters) return InteractionResult.PASS;

            saveFilters(stack, be.filterHandler);
            if (player != null) {
                player.displayClientMessage(Component.literal("Filter saved"), true);
            }
        }
        return InteractionResult.CONSUME;
    }
}
