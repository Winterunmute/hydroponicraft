package com.hydroponicraft;

import com.hydroponicraft.blockentity.GatheringChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GatheringChestManager {

    private record PendingGather(
            Level level,
            BlockPos c4Pos,
            @Nullable UUID owner,
            @Nullable DyeColor color,
            long expiryTick
    ) {}

    private static final List<PendingGather> PENDING = new ArrayList<>();
    private static final Map<Level, Set<BlockPos>> CHEST_POSITIONS = new WeakHashMap<>();

    // ── Called by EnderC4Block when detonated ─────────────────────────────────

    public static void registerDetonation(Level level, BlockPos pos,
                                          @Nullable UUID owner, @Nullable DyeColor color) {
        PENDING.add(new PendingGather(level, pos, owner, color, level.getGameTime() + 40));
    }

    // ── Chest tracking ────────────────────────────────────────────────────────

    public static void trackChest(Level level, BlockPos pos) {
        CHEST_POSITIONS.computeIfAbsent(level, k -> new HashSet<>()).add(pos);
    }

    public static void untrackChest(Level level, BlockPos pos) {
        Set<BlockPos> set = CHEST_POSITIONS.get(level);
        if (set != null) set.remove(pos);
    }

    // ── Server tick ───────────────────────────────────────────────────────────

    public static void onServerTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        long now = level.getGameTime();

        Iterator<PendingGather> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingGather pg = it.next();
            if (pg.level() != level) continue;
            if (pg.expiryTick() > now) continue;
            it.remove();
            processPendingGather(pg, level);
        }
    }

    private static void processPendingGather(PendingGather pg, Level level) {
        BlockPos c4Pos = pg.c4Pos();
        AABB searchBox = new AABB(
                c4Pos.getX() - 48, c4Pos.getY() - 48, c4Pos.getZ() - 48,
                c4Pos.getX() + 49, c4Pos.getY() + 49, c4Pos.getZ() + 49);

        List<ItemEntity> items = new ArrayList<>(
                level.getEntitiesOfClass(ItemEntity.class, searchBox, e -> !e.isRemoved()));
        if (items.isEmpty()) return;

        Set<BlockPos> chestPositions = CHEST_POSITIONS.get(level);
        if (chestPositions == null || chestPositions.isEmpty()) return;

        // Collect ALL matching chests, sorted closest-first to the explosion.
        List<GatheringChestBlockEntity> matchingChests = new HashSet<>(chestPositions).stream()
                .filter(pos -> level.getBlockEntity(pos) instanceof GatheringChestBlockEntity chest
                        && chest.matchesDetonation(pg.owner(), pg.color()))
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(c4Pos)))
                .map(pos -> (GatheringChestBlockEntity) level.getBlockEntity(pos))
                .collect(Collectors.toList());

        // Fill chests sequentially: closest first, spill to next when full.
        for (GatheringChestBlockEntity chest : matchingChests) {
            if (items.isEmpty()) break;
            Iterator<ItemEntity> itemIt = items.iterator();
            while (itemIt.hasNext()) {
                ItemEntity ie = itemIt.next();
                if (ie.isRemoved()) { itemIt.remove(); continue; }
                ItemStack remaining = chest.insertItem(ie.getItem().copy());
                if (remaining.isEmpty()) {
                    ie.discard();
                    itemIt.remove();
                } else {
                    ie.setItem(remaining);
                }
            }
        }
        // Any items still in 'items' stay in the world (all chests full).
    }
}
