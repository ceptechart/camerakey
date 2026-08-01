package com.ianeli.camerakey.playback;

import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPlaybackPayloadHandler {
    public static void playbackStartHandler(PlaybackStartPayload playbackStartPayload, final IPayloadContext context) {
        context.enqueueWork(() -> ClientPlayback.start(playbackStartPayload));
    }
    public static void playbackStopHandler(PlaybackStopPayload playbackStopPayload, final IPayloadContext context) {
        context.enqueueWork(ClientPlayback::stop);
    }
}
