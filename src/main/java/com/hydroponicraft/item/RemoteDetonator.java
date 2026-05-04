package com.hydroponicraft.item;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.block.C4Block;
import com.hydroponicraft.blockentity.C4BlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoteDetonator extends Item {

    public RemoteDetonator(Properties props) {
        super(props);
    }

    // ── Color storage (DataComponents.CUSTOM_DATA) ────────────────────────────
    // Stored as a String in a CompoundTag so it survives inventory put-away/pickup.

    public static DyeColor getColor(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return DyeColor.WHITE;
        String name = data.getUnsafe().getString("Color");
        for (DyeColor c : DyeColor.values()) {
            if (c.getName().equals(name)) return c;
        }
        return DyeColor.WHITE;
    }

    private static void setColor(ItemStack stack, DyeColor color) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = (existing != null) ? existing.copyTag() : new CompoundTag();
        tag.putString("Color", color.getName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Color: ")
                .append(Component.literal(formatColor(getColor(stack)))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("Shift+Right-click to cycle color")
                .withStyle(ChatFormatting.GRAY));
    }

    // ── Scan for matching C4 blocks ───────────────────────────────────────────

    private static List<BlockPos> findTargets(Level level, Player player, DyeColor color) {
        Block baseC4 = HydroponiCraftRegistry.C4_BLOCK.get();
        var coloredHolder = HydroponiCraftRegistry.COLORED_C4_BLOCKS.get(color);
        Block coloredC4 = (coloredHolder != null) ? coloredHolder.get() : null;
        UUID playerUUID = player.getUUID();

        List<BlockPos> result = new ArrayList<>();
        // Snapshot to avoid ConcurrentModificationException if detonation removes entries.
        for (BlockPos pos : C4BlockEntity.getActivePositions(level)) {
            Block b = level.getBlockState(pos).getBlock();
            // Base C4 detonates regardless of color selection; colored must match.
            if (b != baseC4 && (coloredC4 == null || b != coloredC4)) continue;
            // Ownership check — only detonate blocks placed by this player.
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof C4BlockEntity c4be)) continue;
            UUID owner = c4be.getOwner();
            if (owner == null || !owner.equals(playerUUID)) continue;
            result.add(pos);
        }
        return result;
    }

    // ── Shared logic ──────────────────────────────────────────────────────────

    private static void cycleColor(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) return;
        DyeColor[] colors = DyeColor.values();
        DyeColor next = colors[(getColor(stack).ordinal() + 1) % colors.length];
        setColor(stack, next);
        player.displayClientMessage(
                Component.literal("[Remote Detonator] Color: " + formatColor(next)), true);
    }

    private static void detonateAll(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) return;
        DyeColor color = getColor(stack);
        List<BlockPos> targets = findTargets(level, player, color);
        if (targets.isEmpty()) {
            player.displayClientMessage(
                    Component.literal("[Remote Detonator] No matching charges in range."), true);
        } else {
            player.displayClientMessage(
                    Component.literal("[Remote Detonator] Detonating " + targets.size() + " charges..."), true);
            for (BlockPos pos : targets) {
                C4Block.detonate(level, pos);
            }
        }
    }

    // ── Right-click on a block ────────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = ctx.getItemInHand();
        if (player.isShiftKeyDown()) {
            cycleColor(level, player, stack);
        } else {
            detonateAll(level, player, stack);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // ── Right-click in air ────────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            cycleColor(level, player, stack);
        } else {
            detonateAll(level, player, stack);
        }
        return InteractionResultHolder.success(stack);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // "light_blue" → "Light Blue"
    private static String formatColor(DyeColor color) {
        String[] parts = color.getName().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
