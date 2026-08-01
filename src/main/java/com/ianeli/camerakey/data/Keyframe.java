package com.ianeli.camerakey.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record Keyframe(
        Vec3 position,
        float yaw,
        float pitch,
        Optional<InterpolationMode> overrideInterpolation
) {
    public static final MapCodec<Keyframe> CODEC = RecordCodecBuilder.mapCodec(keyFrameInstance -> keyFrameInstance.group(
            Vec3.CODEC.fieldOf("position").forGetter(Keyframe::position),
            Codec.FLOAT.fieldOf("yaw").forGetter(Keyframe::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(Keyframe::pitch),
            InterpolationMode.CODEC.optionalFieldOf("interpolation_mode").forGetter(Keyframe::overrideInterpolation))
            .apply(keyFrameInstance, Keyframe::new));
}
