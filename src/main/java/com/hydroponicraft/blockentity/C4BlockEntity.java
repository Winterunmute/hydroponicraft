package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class C4BlockEntity extends BlockEntity {

    // Server-side registry of all loaded C4 block positions per level.
    // WeakHashMap so levels are GC'd when unloaded.
    private static final Map<Level, Set<BlockPos>> ACTIVE = new WeakHashMap<>();

    @Nullable
    private UUID ownerUUID;

    public C4BlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.C4_BE.get(), pos, state);
    }

    // ── Owner ─────────────────────────────────────────────────────────────────

    @Nullable
    public UUID getOwner() { return ownerUUID; }

    public void setOwner(UUID uuid) { this.ownerUUID = uuid; }

    // ── Active registry ───────────────────────────────────────────────────────

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            ACTIVE.computeIfAbsent(level, k -> new HashSet<>()).add(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            Set<BlockPos> set = ACTIVE.get(level);
            if (set != null) set.remove(worldPosition);
        }
        super.setRemoved();
    }

    /** Returns a snapshot of all loaded C4 positions in this level (safe to iterate). */
    public static Set<BlockPos> getActivePositions(Level level) {
        Set<BlockPos> set = ACTIVE.get(level);
        return set != null ? new HashSet<>(set) : Collections.emptySet();
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
