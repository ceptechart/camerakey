package com.ianeli.camerakey.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record Sequence(
        String name,
        InterpolationMode defaultInterpolation,
        List<Keyframe> keyframes
) {
    public static final MapCodec<Sequence> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Sequence::name),
            InterpolationMode.CODEC.fieldOf("interpolation_mode").forGetter(Sequence::defaultInterpolation),
            Codec.list(Keyframe.CODEC.codec()).fieldOf("keyframes").forGetter(Sequence::keyframes))
            .apply(instance, Sequence::new));
}
