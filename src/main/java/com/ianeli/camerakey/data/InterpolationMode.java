package com.ianeli.camerakey.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum InterpolationMode implements StringRepresentable {
    INSTANT(0, "instant"),
    LINEAR(1, "linear"),
    QUADRATIC(2, "quadratic"),
    CUBIC(3, "cubic"),
    BOUNCE(4, "bounce");

    private static final Map<String, InterpolationMode> BY_NAME =
            Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(
                    InterpolationMode::getSerializedName,
                    mode -> mode
            ));
    public static InterpolationMode byName(String name) {
        return BY_NAME.get(name);
    }

    public static final Codec<InterpolationMode> CODEC =
            StringRepresentable.fromEnum(InterpolationMode::values);

    private final int id;
    private final String name;

    InterpolationMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }
    @Override
    public String getSerializedName() {
        return this.name;
    }
}
