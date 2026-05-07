package com.hydroponicraft.client;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.entity.EnderPearlLauncherCart;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                HydroponiCraftRegistry.ENDER_PEARL_LAUNCHER_CART_TYPE.get(),
                ctx -> new MinecartRenderer<EnderPearlLauncherCart>(ctx, ModelLayers.MINECART));
    }
}
