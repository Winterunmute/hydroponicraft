package com.hydroponicraft.item;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.block.C4Block;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RemoteDetonator extends Item {

    private static final int RANGE = 50;

    public RemoteDetonator(Properties props) {
        super(props);
    }

    // ── Color storage (DataComponents.CUSTOM_DATA / CompoundTag) ──────────────

    private static DyeColor getColor(ItemStack stack) {
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

    // ── Scan for matching C4 blocks ───────────────────────────────────────────

    private static List<BlockPos> findTargets(Level level, Player player, DyeColor color) {
        Block baseC4 = HydroponiCraftRegistry.C4_BLOCK.get();
        var coloredHolder = HydroponiCraftRegistry.COLORED_C4_BLOCKS.get(color);
        Block coloredC4 = (coloredHolder != null) ? coloredHolder.get() : null;

        int cx = player.blockPosition().getX();
        int cy = player.blockPosition().getY();
        int cz = player.blockPosition().getZ();

        List<BlockPos> result = new ArrayList<>();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dy = -RANGE; dy <= RANGE; dy++) {
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    mutable.set(cx + dx, cy + dy, cz + dz);
                    Block b = level.getBlockState(mutable).getBlock();
                    if (b == baseC4 || (coloredC4 != null && b == coloredC4)) {
                        result.add(mutable.immutable());
                    }
                }
            }
        }
        return result;
    }

    // ── Right-click on a block → detonate matching C4 within range ────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            DyeColor color = getColor(ctx.getItemInHand());
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
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // ── Right-click in air → cycle selected color ─────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            DyeColor current = getColor(stack);
            DyeColor[] colors = DyeColor.values();
            DyeColor next = colors[(current.ordinal() + 1) % colors.length];
            setColor(stack, next);
            player.displayClientMessage(
                    Component.literal("[Remote Detonator] Color: " + formatColor(next)), true);
        }

        return InteractionResultHolder.success(stack);
    }

    // Converts "light_blue" → "Light Blue" for display
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
