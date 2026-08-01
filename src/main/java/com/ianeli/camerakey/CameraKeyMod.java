package com.ianeli.camerakey;

import com.ianeli.camerakey.cmds.CameraKeyCommands;
import com.ianeli.camerakey.playback.ClientPlaybackPayloadHandler;
import com.ianeli.camerakey.playback.PlaybackStartPayload;
import com.ianeli.camerakey.playback.PlaybackStopPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CameraKeyMod.MODID)
public class CameraKeyMod {
    public static final String MODID = "camerakey";
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CameraKeyMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(CameraKeyMod::registerPayloads);
    }


    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                PlaybackStartPayload.TYPE,
                PlaybackStartPayload.STREAM_CODEC,
                ClientPlaybackPayloadHandler::playbackStartHandler
        );
        registrar.playToClient(
                PlaybackStopPayload.TYPE,
                PlaybackStopPayload.STREAM_CODEC,
                ClientPlaybackPayloadHandler::playbackStopHandler
        );
    }
}
