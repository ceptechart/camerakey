package com.ianeli.camerakey.playback;

import com.ianeli.camerakey.CameraKeyMod;
import com.ianeli.camerakey.data.InterpolationMode;
import com.ianeli.camerakey.data.Keyframe;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record PlaybackStartPayload(
        InterpolationMode interpolationMode,
        List<Keyframe> keyframeList,
        float duration
) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<PlaybackStartPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CameraKeyMod.MODID, "playback_start"));

    private static final StreamCodec<ByteBuf, InterpolationMode> INTERPOLATION_MODE_CODEC =
            ByteBufCodecs.VAR_INT.map(id -> InterpolationMode.values()[id], InterpolationMode::ordinal);

    private static final StreamCodec<ByteBuf, Vec3> VEC_3_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, Vec3::x,
            ByteBufCodecs.DOUBLE, Vec3::y,
            ByteBufCodecs.DOUBLE, Vec3::z,
            Vec3::new
    );

    private static final StreamCodec<ByteBuf, Keyframe> KEYFRAME_STREAM_CODEC = StreamCodec.composite(
            VEC_3_STREAM_CODEC, Keyframe::position,
            ByteBufCodecs.FLOAT, Keyframe::yaw,
            ByteBufCodecs.FLOAT, Keyframe::pitch,
            INTERPOLATION_MODE_CODEC.apply(ByteBufCodecs::optional), Keyframe::overrideInterpolation,
            Keyframe::new
    );

    public static final StreamCodec<ByteBuf, PlaybackStartPayload> STREAM_CODEC = StreamCodec.composite(
            INTERPOLATION_MODE_CODEC, PlaybackStartPayload::interpolationMode,
            KEYFRAME_STREAM_CODEC.apply(ByteBufCodecs.list()), PlaybackStartPayload::keyframeList,
            ByteBufCodecs.FLOAT, PlaybackStartPayload::duration,
            PlaybackStartPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
