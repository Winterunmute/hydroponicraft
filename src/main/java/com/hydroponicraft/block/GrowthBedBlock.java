package com.hydroponicraft.block;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.blockentity.GrowthBedBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.common.util.TriState;

import javax.annotation.Nullable;

public class GrowthBedBlock extends BaseEntityBlock {

    public static final MapCodec<GrowthBedBlock> CODEC = simpleCodec(GrowthBedBlock::new);

    public GrowthBedBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                                                net.minecraft.world.phys.shapes.CollisionContext context) {
        return Shapes.block();
    }

    // Allow any crop to be placed directly on the Growth Bed
    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos pos,
                                    Direction facing, BlockState plantState) {
        return plantState.getBlock() instanceof CropBlock ? TriState.TRUE : TriState.DEFAULT;
    }

    // Keep placed crops growing at their base rate even when the bed is idle
    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrowthBedBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, HydroponiCraftRegistry.GROWTH_BED_BE.get(),
                (lvl, pos, st, be) -> be.tick());
    }
}
