package com.ianeli.camerakey.playback;

import com.ianeli.camerakey.CameraKeyMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PlaybackStopPayload() implements CustomPacketPayload {
    public static final Type<PlaybackStopPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CameraKeyMod.MODID, "playback_stop"));

    public static final StreamCodec<ByteBuf, PlaybackStopPayload> STREAM_CODEC = StreamCodec.unit(new PlaybackStopPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
