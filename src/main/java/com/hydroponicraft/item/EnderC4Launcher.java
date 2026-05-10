package com.hydroponicraft.item;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.block.C4Block;
import com.hydroponicraft.block.EnderC4Block;
import com.hydroponicraft.blockentity.EnderC4BlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class EnderC4Launcher extends Item {

    private static final int DEFAULT_DEPTH = 10;
    private static final int STEP = 5;
    private static final int MIN_DEPTH = 5;
    private static final int MAX_DEPTH = 50;
    private static final int MAX_RANGE = 100;   // raycast search range
    private static final int COOLDOWN_TICKS = 20;

    public EnderC4Launcher(Properties props) {
        super(props);
    }

    // ── Distance NBT ──────────────────────────────────────────────────────────

    private static int getDepth(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return DEFAULT_DEPTH;
        CompoundTag tag = data.getUnsafe();
        return tag.contains("Distance") ? tag.getInt("Distance") : DEFAULT_DEPTH;
    }

    private static void setDepth(ItemStack stack, int depth) {
        CustomData existing = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = existing != null ? existing.copyTag() : new CompoundTag();
        tag.putInt("Distance", depth);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    // ── Tooltip ───────────────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Boring depth: " + getDepth(stack) + " blocks")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Shift + Right-click to adjust boring depth")
                .withStyle(ChatFormatting.GRAY));
    }

    // ── Right-click on a block ────────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = ctx.getItemInHand();
        handleUse(ctx.getLevel(), player, stack, ctx.getHand());
        return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide());
    }

    // ── Right-click in air ────────────────────────────────────────────────────

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        handleUse(level, player, stack, hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    // ── Shared logic ──────────────────────────────────────────────────────────

    private void handleUse(Level level, Player player, ItemStack stack, InteractionHand hand) {
        if (level.isClientSide()) return;

        if (player.isShiftKeyDown()) {
            cycleDistance(player, stack);
        } else {
            doFire(level, player, stack);
        }
    }

    private static void cycleDistance(Player player, ItemStack stack) {
        int depth = getDepth(stack) + STEP;
        if (depth > MAX_DEPTH) depth = MIN_DEPTH;
        setDepth(stack, depth);
        player.displayClientMessage(
                Component.literal("Boring depth: " + depth + " blocks"), true);
    }

    private void doFire(Level level, Player player, ItemStack launcherStack) {
        if (player.getCooldowns().isOnCooldown(this)) return;

        // Find an Ender C4 item in the player's inventory
        FoundC4 found = findEnderC4(player);
        if (found == null) {
            player.displayClientMessage(
                    Component.literal("No Ender C4 in inventory").withStyle(ChatFormatting.RED), true);
            return;
        }

        // Raycast to find first solid block (max 100 blocks)
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(MAX_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        if (hit.getType() != HitResult.Type.BLOCK) return;  // no solid block in range

        // From the surface hit point, bore [depth] blocks further in look direction
        int depth = getDepth(launcherStack);
        Vec3 target = hit.getLocation().add(look.scale(depth));
        BlockPos targetPos = BlockPos.containing(target);

        if (!level.isInWorldBounds(targetPos)) return;

        // Consume one Ender C4 from inventory
        found.stack().shrink(1);

        // Place the Ender C4 block at the target position
        BlockState state = found.block().defaultBlockState()
                .setValue(C4Block.FACING, Direction.UP);
        level.setBlock(targetPos, state, 3);

        // Set owner + color in block entity
        if (level.getBlockEntity(targetPos) instanceof EnderC4BlockEntity ebe) {
            ebe.setOwner(player.getUUID());
            if (found.block() instanceof EnderC4Block ec4b) {
                ebe.setColor(ec4b.getDyeColor());
            }
            ebe.setChanged();
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
    }

    // ── Inventory search ──────────────────────────────────────────────────────

    private record FoundC4(ItemStack stack, Block block) {}

    @Nullable
    private static FoundC4 findEnderC4(Player player) {
        for (ItemStack s : player.getInventory().items) {
            Block block = getEnderC4Block(s);
            if (block != null) return new FoundC4(s, block);
        }
        for (ItemStack s : player.getInventory().offhand) {
            Block block = getEnderC4Block(s);
            if (block != null) return new FoundC4(s, block);
        }
        return null;
    }

    @Nullable
    private static Block getEnderC4Block(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var item = stack.getItem();
        if (item == HydroponiCraftRegistry.ENDER_C4_ITEM.get()) {
            return HydroponiCraftRegistry.ENDER_C4_BLOCK.get();
        }
        for (Map.Entry<DyeColor, ?> entry : HydroponiCraftRegistry.COLORED_ENDER_C4_ITEMS.entrySet()) {
            @SuppressWarnings("unchecked")
            var holder = (net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.Item,
                    net.minecraft.world.item.BlockItem>)
                    HydroponiCraftRegistry.COLORED_ENDER_C4_ITEMS.get(entry.getKey());
            if (holder != null && item == holder.get()) {
                var blockHolder = HydroponiCraftRegistry.COLORED_ENDER_C4_BLOCKS.get(entry.getKey());
                return blockHolder != null ? blockHolder.get() : null;
            }
        }
        return null;
    }
}
