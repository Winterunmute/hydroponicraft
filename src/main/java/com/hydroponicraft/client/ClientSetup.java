package com.hydroponicraft.client;

import com.hydroponicraft.HydroponiCraftRegistry;
import com.hydroponicraft.client.screen.GatheringChestScreen;
import com.hydroponicraft.entity.EnderPearlLauncherCart;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientSetup {

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                HydroponiCraftRegistry.ENDER_PEARL_LAUNCHER_CART_TYPE.get(),
                ctx -> new MinecartRenderer<EnderPearlLauncherCart>(ctx, ModelLayers.MINECART));
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(HydroponiCraftRegistry.GATHERING_CHEST_MENU.get(), GatheringChestScreen::new);
    }
}
