package com.ianeli.camerakey.cmds;

import com.ianeli.camerakey.CameraKeyMod;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = CameraKeyMod.MODID)
public class CommandRegistration {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("sequence")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(CameraKeyCommands::createSequenceDefault)
                                        .then(Commands.argument("interpolation_mode", StringArgumentType.word())
                                                .executes(CameraKeyCommands::createSequence))))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(CameraKeyCommands::deleteSequence)))
                        .then(Commands.literal("list")
                                .executes(CameraKeyCommands::listSequence))
                        .then(Commands.literal("play")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(CameraKeyCommands::playSequenceDefault)
                                        .then(Commands.argument("duration", FloatArgumentType.floatArg(0))
                                            .executes(CameraKeyCommands::playSequence))))
                        .then(Commands.literal("stop")
                                .executes(CameraKeyCommands::stopSequence))
        );

        event.getDispatcher().register(
                Commands.literal("keyframe")
                        .then(Commands.literal("add")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .executes(CameraKeyCommands::addKeyframe)
                                        .then(Commands.argument("interpolation_mode", StringArgumentType.word())
                                                .executes(CameraKeyCommands::addKeyframeWithInterpolation))))
                        .then(Commands.literal("pop")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .executes(CameraKeyCommands::popKeyframe)))
                        .then(Commands.literal("delete")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                .executes(CameraKeyCommands::deleteKeyframe))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                .executes(CameraKeyCommands::setKeyframe)
                                                .then(Commands.argument("interpolation_mode", StringArgumentType.word())
                                                        .executes(CameraKeyCommands::setKeyframeWithInterpolation)))))
                        .then(Commands.literal("list")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .executes(CameraKeyCommands::listKeyframes)))
                        .then(Commands.literal("goto")
                                .then(Commands.argument("sequence", StringArgumentType.word())
                                        .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                .executes(CameraKeyCommands::gotoKeyframe))))
        );
    }
}
