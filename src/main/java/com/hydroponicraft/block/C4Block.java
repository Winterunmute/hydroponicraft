package com.hydroponicraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class C4Block extends Block {

    public static final MapCodec<C4Block> CODEC = simpleCodec(C4Block::new);

    // Explicit declaration — same 6-direction property as DirectionalBlock, but owned by this
    // class so createBlockStateDefinition and getValue/setValue all use the same instance.
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public static final float EXPLOSION_STRENGTH = 7.0f;

    // Thin slab shapes per attachment direction.
    // FACING = direction the C4 "faces away" from its mounting surface.
    private static final VoxelShape SHAPE_UP    = box(1,  0,  1,  15,  6, 15);
    private static final VoxelShape SHAPE_DOWN  = box(1, 10,  1,  15, 16, 15);
    private static final VoxelShape SHAPE_NORTH = box(1,  5,  0,  15, 11,  6);
    private static final VoxelShape SHAPE_SOUTH = box(1,  5, 10,  15, 11, 16);
    private static final VoxelShape SHAPE_EAST  = box(10, 5,  1,  16, 11, 15);
    private static final VoxelShape SHAPE_WEST  = box(0,  5,  1,   6, 11, 15);

    public C4Block(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public MapCodec<C4Block> codec() { return CODEC; }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN  -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST  -> SHAPE_EAST;
            case WEST  -> SHAPE_WEST;
            default    -> SHAPE_UP;
        };
    }

    // FACING = the clicked face (direction away from the surface clicked).
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    // C4 survives as long as the surface it is attached to is sturdy.
    // attachment = the direction OPPOSITE to FACING (toward the wall/floor/ceiling).
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing     = state.getValue(FACING);
        Direction attachment = facing.getOpposite();
        BlockPos  supportPos = pos.relative(attachment);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction attachment = state.getValue(FACING).getOpposite();
        if (direction == attachment && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    /**
     * Detonates the C4 at the given position.
     * Removes the block then creates a TNT-strength explosion at block centre.
     * Called externally by the Remote Detonator (future implementation).
     */
    public static void detonate(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            level.removeBlock(pos, false);
            level.explode(null,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    EXPLOSION_STRENGTH, Level.ExplosionInteraction.TNT);
        }
    }

    /** Temporary right-click detonation — for testing only. Remove before release. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        detonate(level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
