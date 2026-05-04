package com.hydroponicraft;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deferred explosion queue — fires scheduled explosions at the right game-tick.
 * Register {@link #onLevelTick} on the NeoForge game event bus.
 */
public class ExplosionQueue {

    private record PendingExplosion(Level level, double x, double y, double z,
                                    float strength, long targetTime) {}

    private static final List<PendingExplosion> QUEUE = new ArrayList<>();

    /** Schedule an explosion to fire at {@code targetTime} (server game-tick). */
    public static void schedule(Level level, double x, double y, double z,
                                float strength, long targetTime) {
        QUEUE.add(new PendingExplosion(level, x, y, z, strength, targetTime));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        long now = level.getGameTime();

        List<PendingExplosion> toFire = new ArrayList<>();
        Iterator<PendingExplosion> it = QUEUE.iterator();
        while (it.hasNext()) {
            PendingExplosion pe = it.next();
            if (pe.level() == level && pe.targetTime() <= now) {
                toFire.add(pe);
                it.remove();
            }
        }

        for (PendingExplosion pe : toFire) {
            level.explode(null, pe.x(), pe.y(), pe.z(), pe.strength(),
                    Level.ExplosionInteraction.TNT);
        }
    }
}
