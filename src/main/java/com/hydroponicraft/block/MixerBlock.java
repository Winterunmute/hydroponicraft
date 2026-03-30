package com.hydroponicraft.block;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.blockentity.MixerBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;

public class MixerBlock extends KineticBlock implements IBE<MixerBlockEntity> {

    public MixerBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    // Shaft connects on top/bottom (Y axis)
    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        return direction.getAxis() == Direction.Axis.Y;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Right-click shows current fluid levels of all three tanks in chat
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MixerBlockEntity mixer) {
                player.sendSystemMessage(Component.literal(
                        "[Mixer] Input 1: " + formatTank(mixer.inputTank1.getFluid())
                        + " | Input 2: " + formatTank(mixer.inputTank2.getFluid())
                        + " | Output: "   + formatTank(mixer.outputTank.getFluid())));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static String formatTank(FluidStack stack) {
        if (stack.isEmpty()) return "Empty";
        return stack.getAmount() + "mB " + stack.getFluidType().getDescription().getString();
    }

    // IBE implementation — provides newBlockEntity() and getTicker() as defaults
    @Override
    public Class<MixerBlockEntity> getBlockEntityClass() {
        return MixerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MixerBlockEntity> getBlockEntityType() {
        return HydroponiCraftRegistry.MIXER_BE.get();
    }
}
