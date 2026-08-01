package com.ianeli.camerakey.cmds;
import com.ianeli.camerakey.Config;
import com.ianeli.camerakey.data.InterpolationMode;
import com.ianeli.camerakey.data.Keyframe;
import com.ianeli.camerakey.data.SavedSequences;
import com.ianeli.camerakey.data.Sequence;
import com.ianeli.camerakey.playback.PlaybackStartPayload;
import com.ianeli.camerakey.playback.PlaybackStopPayload;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CameraKeyCommands {
    static final int COLOR_INFO = 0xFFFF55;
    static final int COLOR_INFO_SPECIAL = 0xFF55FF;
    static final int COLOR_WARN = 0xEB7114;

    static int createSequence(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        String sequenceName = ctx.getArgument("name", String.class);
        String interpolationType = ctx.getArgument("interpolation_mode", String.class);
        InterpolationMode interpolationMode = parseInterpolationMode(ctx, interpolationType);
        if (interpolationMode == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        if (savedSequences.doesSequenceExist(sequenceName)) {
            sendFailure(ctx, String.format("Sequence \"%s\" already exists.", sequenceName));
            return 0;
        }

        Keyframe startKeyframe = keyframeFromPlayer(player, Optional.empty());
        Sequence newSequence = new Sequence(sequenceName, interpolationMode, List.of(startKeyframe));
        savedSequences.addSequence(newSequence);
        sendSuccess(ctx, String.format("Created sequence \"%s\" with %s interpolation mode.", sequenceName, interpolationType), true, COLOR_INFO_SPECIAL);
        return 1;
    }
    static int createSequenceDefault(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        String sequenceName = ctx.getArgument("name", String.class);
        InterpolationMode interpolationMode = Config.DEFAULT_INTERPOLATION_MODE.get();

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        if (savedSequences.doesSequenceExist(sequenceName)) {
            sendFailure(ctx, String.format("Sequence \"%s\" already exists.", sequenceName));
            return 0;
        }

        Keyframe startKeyframe = keyframeFromPlayer(player, Optional.empty());
        Sequence newSequence = new Sequence(sequenceName, interpolationMode, List.of(startKeyframe));
        savedSequences.addSequence(newSequence);
        sendSuccess(ctx, String.format("Created sequence \"%s\" with %s interpolation mode.", sequenceName, interpolationMode.getSerializedName()), true, COLOR_INFO_SPECIAL);
        return 1;
    }
    static int deleteSequence(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        String sequenceName = ctx.getArgument("name", String.class);
        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        if (requireSequence(ctx, savedSequences, sequenceName).isEmpty()) {return 0;}

        savedSequences.removeSequence(sequenceName);
        sendSuccess(ctx, String.format("Deleted sequence \"%s\".", sequenceName), true, COLOR_WARN);
        return 1;
    }

    static int listSequence(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        if (savedSequences.getSequenceCount() == 0) {
            sendFailure(ctx, "No sequences found. Use /sequence create <name> <interpolation_mode>");
            return 0;
        }
        sendMessage(ctx, "Current sequences:", COLOR_INFO);
        for (String sequenceName : savedSequences.getSequenceNames()) {
            sendMessage(ctx, String.format(" - %s", sequenceName), COLOR_INFO_SPECIAL);
        }
        return 1;
    }

    static int playSequence(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        String sequenceName = ctx.getArgument("name", String.class);
        float duration = ctx.getArgument("duration", Float.class);

        if (duration <= 0) {
            sendFailure(ctx, "Duration must be greater than 0.");
            return 0;
        }

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        Optional<Sequence> sequenceOpt  = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        if (sequence.keyframes().size() < 2) {
            sendFailure(ctx, String.format("Sequence \"%s\" needs at least 2 keyframes to play.", sequenceName));
            return 0;
        }

        PacketDistributor.sendToPlayer(player, new PlaybackStartPayload(sequence.defaultInterpolation(), sequence.keyframes(), duration));
        sendSuccess(ctx, String.format("Playing sequence \"%s\" (%.1fs).", sequenceName, duration), false, COLOR_INFO);
        return 1;
    }
    static int playSequenceDefault(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        String sequenceName = ctx.getArgument("name", String.class);
        float duration = (float)Config.DEFAULT_SEQ_TIME.getAsDouble();

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        Optional<Sequence> sequenceOpt  = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        if (sequence.keyframes().size() < 2) {
            sendFailure(ctx, String.format("Sequence \"%s\" needs at least 2 keyframes to play.", sequenceName));
            return 0;
        }

        PacketDistributor.sendToPlayer(player, new PlaybackStartPayload(sequence.defaultInterpolation(), sequence.keyframes(), duration));
        sendSuccess(ctx, String.format("Playing sequence \"%s\" (%.1fs).", sequenceName, duration), false, COLOR_INFO);
        return 1;
    }
    static int stopSequence(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        PacketDistributor.sendToPlayer(player, new PlaybackStopPayload());
        sendSuccess(ctx, "Stopped playback.", false, COLOR_INFO);
        return 1;
    }

    static int listKeyframes(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        sendMessage(ctx, String.format("Sequence \"%s\" has %d keyframe(s):", sequenceName, sequence.keyframes().size()), COLOR_INFO);
        var i = 0;
        for (Keyframe keyframe : sequence.keyframes()) {
            sendMessage(ctx, String.format("Keyframe %d -", i), COLOR_INFO_SPECIAL);
            sendMessage(ctx, String.format("    X: %.2f Y: %.2f Z: %.2f", keyframe.position().x(), keyframe.position().y(), keyframe.position().z()));
            sendMessage(ctx, String.format("    Pitch: %.2f Yaw: %.2f", keyframe.pitch(), keyframe.yaw()));
            sendMessage(ctx, String.format("    Interpolation: %s", keyframe.overrideInterpolation().orElse(sequence.defaultInterpolation()).getSerializedName()));
            i++;
        }
        return 1;
    }

    static int addKeyframe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        keyframes.add(keyframeFromPlayer(player, Optional.empty()));
        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Added keyframe to \"%s\" (%d total)", sequenceName, newSequence.keyframes().size()), false, COLOR_INFO);
        return 1;
    }

    static int addKeyframeWithInterpolation(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);

        String interpolationType = ctx.getArgument("interpolation_mode", String.class);
        InterpolationMode interpolationMode = parseInterpolationMode(ctx, interpolationType);
        if (interpolationMode == null) {return 0;}

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        keyframes.add(keyframeFromPlayer(player, Optional.of(interpolationMode)));
        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Added keyframe to \"%s\". (%d total)", sequenceName, newSequence.keyframes().size()), false, COLOR_INFO);
        return 1;
    }

    static int popKeyframe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        keyframes.removeLast();
        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Removed last keyframe from \"%s\". (%d total)", sequenceName, newSequence.keyframes().size()), false, COLOR_WARN);
        return 1;
    }

    static int deleteKeyframe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);
        int keyframeIndex = ctx.getArgument("index", int.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        if (!isValidIndex(ctx, keyframes, sequenceName, keyframeIndex)) {return 0;}
        keyframes.remove(keyframeIndex);

        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Removed keyframe %d from \"%s\". (%d total)", keyframeIndex, sequenceName, newSequence.keyframes().size()), false, COLOR_WARN);
        return 1;
    }

    static int setKeyframe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);
        int keyframeIndex = ctx.getArgument("index", int.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        if (!isValidIndex(ctx, keyframes, sequenceName, keyframeIndex)) {return 0;}
        keyframes.set(keyframeIndex, keyframeFromPlayer(player, Optional.empty()));

        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Replaced keyframe %d of sequence \"%s\".", keyframeIndex, sequenceName), false, COLOR_INFO);
        return 1;
    }

    static int setKeyframeWithInterpolation(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);
        int keyframeIndex = ctx.getArgument("index", int.class);

        String interpolationType = ctx.getArgument("interpolation_mode", String.class);
        InterpolationMode interpolationMode = parseInterpolationMode(ctx, interpolationType);
        if (interpolationMode == null) {return 0;}

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}
        Sequence sequence = sequenceOpt.get();

        List<Keyframe> keyframes = new ArrayList<>(sequence.keyframes());
        if (!isValidIndex(ctx, keyframes, sequenceName, keyframeIndex)) {return 0;}
        keyframes.set(keyframeIndex, keyframeFromPlayer(player, Optional.of(interpolationMode)));

        Sequence newSequence = saveSequenceWithKeyframes(savedSequences, sequence, keyframes);
        sendSuccess(ctx, String.format("Replaced keyframe %d of sequence \"%s\".", keyframeIndex, sequenceName), false, COLOR_INFO);
        return 1;
    }

    static int gotoKeyframe(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {return 0;}

        SavedSequences savedSequences = SavedSequences.get(ctx.getSource().getLevel());
        String sequenceName = ctx.getArgument("sequence", String.class);
        int keyframeIndex = ctx.getArgument("index", int.class);

        Optional<Sequence> sequenceOpt = requireSequence(ctx, savedSequences, sequenceName);
        if (sequenceOpt.isEmpty()) {return 0;}

        List<Keyframe> keyframes = new ArrayList<>(sequenceOpt.get().keyframes());
        if (!isValidIndex(ctx, keyframes, sequenceName, keyframeIndex)) {return 0;}

        Keyframe keyframe = keyframes.get(keyframeIndex);
        player.teleportTo(player.serverLevel(), keyframe.position().x(), keyframe.position().y(), keyframe.position().z(), keyframe.yaw(), keyframe.pitch());
        sendSuccess(ctx, String.format("Moved to keyframe %d of \"%s\"", keyframeIndex, sequenceName), false, COLOR_INFO);
        return 1;
    }

    //Helper methods

    private static void sendFailure(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendFailure(Component.literal(message));
    }
    private static void sendFailure(CommandContext<CommandSourceStack> ctx, String message, int color) {
        ctx.getSource().sendFailure(Component.literal(message).withColor(color));
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> ctx, String message, boolean broadcastToOps) {
        ctx.getSource().sendSuccess(() -> Component.literal(message), broadcastToOps);
    }
    private static void sendSuccess(CommandContext<CommandSourceStack> ctx, String message, boolean broadcastToOps, int color) {
        ctx.getSource().sendSuccess(() -> Component.literal(message).withColor(color), broadcastToOps);
    }

    private static void sendMessage(CommandContext<CommandSourceStack> ctx, String message) {
        ctx.getSource().sendSystemMessage(Component.literal(message));
    }
    private static void sendMessage(CommandContext<CommandSourceStack> ctx, String message, int color) {
        ctx.getSource().sendSystemMessage(Component.literal(message).withColor(color));
    }

    //Parses an interpolation mode string, sending a failure message and returning null if it's invalid.
    private static InterpolationMode parseInterpolationMode(CommandContext<CommandSourceStack> ctx, String interpolationType) {
        InterpolationMode mode = InterpolationMode.byName(interpolationType);
        if (mode == null) {
            sendFailure(ctx, String.format("Unknown interpolation mode \"%s\".", interpolationType));
        }
        return mode;
    }

    //Looks up a sequence by name, sending a failure message if it doesn't exist.
    private static Optional<Sequence> requireSequence(CommandContext<CommandSourceStack> ctx, SavedSequences savedSequences, String sequenceName) {
        Optional<Sequence> sequence = savedSequences.getSequence(sequenceName);
        if (sequence.isEmpty()) {
            sendFailure(ctx, String.format("Sequence \"%s\" does not exist.", sequenceName));
        }
        return sequence;
    }

    //Validates that a keyframe index is in range for a non-empty keyframe list, sending a failure message otherwise.
    private static boolean isValidIndex(CommandContext<CommandSourceStack> ctx, List<Keyframe> keyframes, String sequenceName, int index) {
        if (keyframes.isEmpty()) {
            sendFailure(ctx, String.format("Sequence %s does not contain any keyframes.", sequenceName));
            return false;
        }
        if (index < 0 || index >= keyframes.size()) {
            sendFailure(ctx, String.format("Index %d is out of range. Must be between %d and %d.)", index, 0, keyframes.size() - 1));
            return false;
        }
        return true;
    }

    //Builds a new keyframe object from the ServerPlayer.
    private static Keyframe keyframeFromPlayer(ServerPlayer player, Optional<InterpolationMode> overrideInterpolation) {
        return new Keyframe(player.getEyePosition(), player.getYRot(), player.getXRot(), overrideInterpolation);
    }

    //Builds a new Sequence with the given keyframes (keeping the original's name/interpolation), saves it, and returns it.
    private static Sequence saveSequenceWithKeyframes(SavedSequences savedSequences, Sequence sequence, List<Keyframe> keyframes) {
        Sequence newSequence = new Sequence(sequence.name(), sequence.defaultInterpolation(), keyframes);
        savedSequences.addSequence(newSequence);
        return newSequence;
    }
}