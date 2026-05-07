package com.hydroponicraft.block;

import com.hydroponicraft.ExplosionQueue;
import com.hydroponicraft.GatheringChestManager;
import com.hydroponicraft.blockentity.EnderC4BlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class EnderC4Block extends C4Block {

    // We reuse the parent codec; DeferredRegister manages block identity.
    @Nullable private final DyeColor dyeColor;

    public EnderC4Block(BlockBehaviour.Properties props, @Nullable DyeColor color) {
        super(props);
        this.dyeColor = color;
    }

    /** Constructor for simpleCodec (uncolored variant). */
    public EnderC4Block(BlockBehaviour.Properties props) {
        this(props, null);
    }

    @Nullable
    public DyeColor getDyeColor() { return dyeColor; }

    // ── Block entity ──────────────────────────────────────────────────────────

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderC4BlockEntity(pos, state);
    }

    /** Store owner + color in the EnderC4BlockEntity (not C4BlockEntity). */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && placer instanceof Player player) {
            if (level.getBlockEntity(pos) instanceof EnderC4BlockEntity ebe) {
                ebe.setOwner(player.getUUID());
                ebe.setColor(dyeColor);
                ebe.setChanged();
            }
        }
    }

    // ── Client-side particle aura ─────────────────────────────────────────────

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            double ox = (random.nextDouble() - 0.5) * 1.2;
            double oy = random.nextDouble() * 0.8;
            double oz = (random.nextDouble() - 0.5) * 1.2;
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5 + ox,
                    pos.getY() + oy,
                    pos.getZ() + 0.5 + oz,
                    0, 0.05, 0);
        }
    }

    // ── Detonation ────────────────────────────────────────────────────────────

    public static void detonate(Level level, BlockPos pos) {
        if (level.isClientSide()) return;

        UUID owner = null;
        DyeColor color = null;
        if (level.getBlockEntity(pos) instanceof EnderC4BlockEntity ebe) {
            owner = ebe.getOwner();
            color = ebe.getColor();
        }
        level.removeBlock(pos, false);
        GatheringChestManager.registerDetonation(level, pos, owner, color);

        long now = level.getGameTime();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        RandomSource rng = level.random;

        ExplosionQueue.schedule(level, cx, cy, cz, 10.0f, now);
        ExplosionQueue.schedule(level, cx, cy - 2, cz, 11.0f, now + 4);
        ExplosionQueue.schedule(level, cx, cy - 3, cz, 12.0f, now + 8);
        int dir1 = rng.nextInt(4);
        int dist1 = 1 + rng.nextInt(2);
        ExplosionQueue.schedule(level, cx + H_DX[dir1] * dist1, cy, cz + H_DZ[dir1] * dist1, 11.0f, now + 12);
        int dir2 = rng.nextInt(4);
        int dist2 = 1 + rng.nextInt(2);
        ExplosionQueue.schedule(level, cx + H_DX[dir2] * dist2, cy, cz + H_DZ[dir2] * dist2, 10.0f, now + 16);
    }

    private static final int[] H_DX = { 0,  1,  0, -1};
    private static final int[] H_DZ = {-1,  0,  1,  0};

    @Override
    public MapCodec<C4Block> codec() { return C4Block.CODEC; }
}
