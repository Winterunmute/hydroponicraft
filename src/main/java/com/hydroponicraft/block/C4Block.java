package com.hydroponicraft.block;

import com.hydroponicraft.ExplosionQueue;
import com.hydroponicraft.blockentity.C4BlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class C4Block extends BaseEntityBlock {

    public static final MapCodec<C4Block> CODEC = simpleCodec(C4Block::new);

    // Explicit declaration — owns the FACING property so all 17 block registrations
    // share the same instance in createBlockStateDefinition and getValue/setValue.
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
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public MapCodec<C4Block> codec() { return CODEC; }

    // ── BlockEntity ───────────────────────────────────────────────────────────

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new C4BlockEntity(pos, state);
    }

    /** Store placing player's UUID in the block entity. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof C4BlockEntity c4be) {
                c4be.setOwner(player.getUUID());
                c4be.setChanged();
            }
        }
    }

    // ── Shape / placement ─────────────────────────────────────────────────────

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

    // ── Detonation ────────────────────────────────────────────────────────────

    /**
     * Detonates the C4 at the given position with a five-stage explosion sequence.
     * Removes the block immediately, then schedules blasts over 16 ticks.
     */
    public static void detonate(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        level.removeBlock(pos, false);

        long now = level.getGameTime();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        RandomSource rng = level.random;

        // Stage 1 — tick 0: at block position, strength 10
        ExplosionQueue.schedule(level, cx, cy, cz, 10.0f, now);

        // Stage 2 — tick 4: 2 blocks below, strength 11
        ExplosionQueue.schedule(level, cx, cy - 2, cz, 11.0f, now + 4);

        // Stage 3 — tick 8: 3 blocks below, strength 12
        ExplosionQueue.schedule(level, cx, cy - 3, cz, 12.0f, now + 8);

        // Stage 4 — tick 12: random horizontal offset 1-2 blocks, strength 11
        int dir1 = rng.nextInt(4);
        int dist1 = 1 + rng.nextInt(2);
        ExplosionQueue.schedule(level,
                cx + H_DX[dir1] * dist1, cy, cz + H_DZ[dir1] * dist1,
                11.0f, now + 12);

        // Stage 5 — tick 16: different random horizontal offset 1-2 blocks, strength 10
        int dir2 = rng.nextInt(4);
        int dist2 = 1 + rng.nextInt(2);
        ExplosionQueue.schedule(level,
                cx + H_DX[dir2] * dist2, cy, cz + H_DZ[dir2] * dist2,
                10.0f, now + 16);
    }

    // Cardinal directions: N, E, S, W
    private static final int[] H_DX = { 0,  1,  0, -1};
    private static final int[] H_DZ = {-1,  0,  1,  0};

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
