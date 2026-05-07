package com.hydroponicraft.blockentity;

import com.hydroponicraft.HydroponiCraftRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.UUID;

public class RedstoneDetonatorBlockEntity extends BlockEntity {

    @Nullable private UUID ownerUUID;
    @Nullable private DyeColor linkedColor;

    public RedstoneDetonatorBlockEntity(BlockPos pos, BlockState state) {
        super(HydroponiCraftRegistry.REDSTONE_DETONATOR_BE.get(), pos, state);
    }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(@Nullable UUID uuid) { this.ownerUUID = uuid; }

    @Nullable public DyeColor getLinkedColor() { return linkedColor; }
    public void setLinkedColor(@Nullable DyeColor color) { this.linkedColor = color; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        if (linkedColor != null) tag.putString("LinkedColor", linkedColor.getName());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerUUID = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        linkedColor = EnderC4BlockEntity.colorFromName(tag.getString("LinkedColor"));
    }
}
