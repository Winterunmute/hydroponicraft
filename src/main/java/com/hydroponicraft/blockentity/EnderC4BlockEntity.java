package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class EnderC4BlockEntity extends BlockEntity {

    private static final Map<Level, Set<BlockPos>> ACTIVE = new WeakHashMap<>();

    @Nullable private UUID ownerUUID;
    @Nullable private DyeColor color;

    public EnderC4BlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.ENDER_C4_BE.get(), pos, state);
    }

    @Nullable public UUID getOwner() { return ownerUUID; }
    public void setOwner(@Nullable UUID uuid) { this.ownerUUID = uuid; }

    @Nullable public DyeColor getColor() { return color; }
    public void setColor(@Nullable DyeColor color) { this.color = color; }

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

    public static Set<BlockPos> getActivePositions(Level level) {
        Set<BlockPos> set = ACTIVE.get(level);
        return set != null ? new HashSet<>(set) : Collections.emptySet();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (color != null) tag.putString("Color", color.getName());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        color = colorFromName(tag.getString("Color"));
    }

    static @Nullable DyeColor colorFromName(String name) {
        if (name == null || name.isEmpty()) return null;
        for (DyeColor c : DyeColor.values()) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }
}
