package com.hydroponicraft.block;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.blockentity.ChemicalSynthesizerBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class ChemicalSynthesizerBlock extends KineticBlock implements IBE<ChemicalSynthesizerBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public ChemicalSynthesizerBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // Shaft connects on model-east = facing.getClockWise() in world space.
    // Accept shaft from either side of that axis (the shaft passes through the block).
    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        return direction.getAxis() == getRotationAxis(state);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getClockWise().getAxis();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Class<ChemicalSynthesizerBlockEntity> getBlockEntityClass() {
        return ChemicalSynthesizerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ChemicalSynthesizerBlockEntity> getBlockEntityType() {
        return HydroponiCraftRegistry.CHEMICAL_SYNTHESIZER_BE.get();
    }
}
