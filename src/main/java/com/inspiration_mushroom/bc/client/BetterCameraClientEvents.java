package com.inspiration_mushroom.bc.client;

import com.inspiration_mushroom.bc.BetterCamera;
import com.inspiration_mushroom.bc.client.gui.BetterCameraConfigScreen;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterCamera.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BetterCameraClientEvents {
    private BetterCameraClientEvents() {
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("bettercamera").executes(context -> {
            Minecraft.getInstance().setScreen(new BetterCameraConfigScreen(null));
            return Command.SINGLE_SUCCESS;
        }));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void applyPositionSmoothing(ViewportEvent.ComputeCameraAngles event) {
        CameraPositionController.getInstance().apply(
                Minecraft.getInstance(),
                event.getCamera(),
                (float) event.getPartialTick()
        );
    }
}
