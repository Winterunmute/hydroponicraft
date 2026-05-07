package com.hydroponicraft.block;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.blockentity.C4BlockEntity;
import com.hydroponicraft.blockentity.EnderC4BlockEntity;
import com.hydroponicraft.blockentity.RedstoneDetonatorBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RedstoneDetonatorBlock extends BaseEntityBlock {

    public static final MapCodec<RedstoneDetonatorBlock> CODEC = simpleCodec(RedstoneDetonatorBlock::new);

    public RedstoneDetonatorBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override public MapCodec<RedstoneDetonatorBlock> codec() { return CODEC; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedstoneDetonatorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof RedstoneDetonatorBlockEntity be) {
                be.setOwnerUUID(player.getUUID());
                be.setChanged();
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            if (level.getBlockEntity(pos) instanceof RedstoneDetonatorBlockEntity be) {
                detonateAll(level, be.getOwnerUUID(), be.getLinkedColor());
            }
        }
    }

    private static void detonateAll(Level level, @Nullable UUID owner, @Nullable DyeColor linkedColor) {
        if (owner == null) return;

        // --- Regular C4 ---
        List<BlockPos> c4Targets = new ArrayList<>();
        for (BlockPos pos : C4BlockEntity.getActivePositions(level)) {
            BlockState bs = level.getBlockState(pos);
            Block b = bs.getBlock();
            if (!isMatchingRegularC4(b, linkedColor)) continue;
            if (!(level.getBlockEntity(pos) instanceof C4BlockEntity c4be)) continue;
            if (!owner.equals(c4be.getOwner())) continue;
            c4Targets.add(pos);
        }
        for (BlockPos pos : c4Targets) C4Block.detonate(level, pos);

        // --- Ender C4 ---
        List<BlockPos> enderTargets = new ArrayList<>();
        for (BlockPos pos : EnderC4BlockEntity.getActivePositions(level)) {
            if (!(level.getBlockEntity(pos) instanceof EnderC4BlockEntity ebe)) continue;
            if (!owner.equals(ebe.getOwner())) continue;
            if (!colorMatches(ebe.getColor(), linkedColor)) continue;
            enderTargets.add(pos);
        }
        for (BlockPos pos : enderTargets) EnderC4Block.detonate(level, pos);
    }

    /** Unlinked detonator (null) → only base (uncolored) C4. Linked → matching colored C4. */
    private static boolean isMatchingRegularC4(Block block, @Nullable DyeColor linkedColor) {
        if (linkedColor == null) {
            return block == HydroponiCraftRegistry.C4_BLOCK.get();
        }
        var holder = HydroponiCraftRegistry.COLORED_C4_BLOCKS.get(linkedColor);
        return holder != null && block == holder.get();
    }

    /** Null linkedColor → only uncolored EnderC4. Linked → matching color. */
    private static boolean colorMatches(@Nullable DyeColor c4Color, @Nullable DyeColor linkedColor) {
        if (linkedColor == null) return c4Color == null;
        return linkedColor == c4Color;
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.getItem() instanceof DyeItem dye) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RedstoneDetonatorBlockEntity be) {
                be.setLinkedColor(dye.getDyeColor());
                be.setChanged();
                player.displayClientMessage(
                        Component.literal("Redstone Detonator linked to " + capitalize(dye.getDyeColor().getName())), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
                                               BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RedstoneDetonatorBlockEntity be) {
            if (player.isShiftKeyDown()) {
                be.setLinkedColor(null);
                be.setChanged();
                player.displayClientMessage(Component.literal("Redstone Detonator unlinked (detonates uncolored C4 only)"), true);
            } else {
                DyeColor c = be.getLinkedColor();
                String msg = c == null ? "Unlinked (detonates uncolored C4 only)" : "Linked to " + capitalize(c.getName());
                player.displayClientMessage(Component.literal("Redstone Detonator: " + msg), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    private static String capitalize(String s) {
        String[] parts = s.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
