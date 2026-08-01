package com.ianeli.camerakey;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ianeli.camerakey.data.InterpolationMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue DEFAULT_SEQ_TIME = BUILDER
            .comment("Default amount of time to play a sequence for in seconds.")
            .defineInRange("default_seq_time", 10.0, 0.1, 1000);

    public static final ModConfigSpec.BooleanValue ENABLE_SPLINE = BUILDER
            .comment("Enable catmull-rom spline interpolation between keyframes. Creates smoother motion paths, but may lead to clipping or undesirable motion.")
            .define("enable_spline", true);

    public static final ModConfigSpec.EnumValue<InterpolationMode> DEFAULT_INTERPOLATION_MODE = BUILDER
            .comment("Default interpolation mode to use for new sequences.")
            .defineEnum("default_interpolation_mode", InterpolationMode.LINEAR);

    static final ModConfigSpec SPEC = BUILDER.build();
}
