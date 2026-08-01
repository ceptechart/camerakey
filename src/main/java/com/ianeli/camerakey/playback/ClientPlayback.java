package com.ianeli.camerakey.playback;

import com.ianeli.camerakey.CameraKeyMod;
import com.ianeli.camerakey.Config;
import com.ianeli.camerakey.data.InterpolationMode;
import com.ianeli.camerakey.data.Keyframe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.List;

import static com.ianeli.camerakey.util.ease.*;

@EventBusSubscriber(modid = CameraKeyMod.MODID, value = Dist.CLIENT)
public class ClientPlayback {
    private static CameraAnchorEntity anchor;
    private static Entity prevCameraEntity;

    private static List<Keyframe> keyframes;
    private static InterpolationMode defaultInterpolationMode;
    private static double segDuration;
    private static long startTime;
    private static float[] unwrappedYaws;

    private static Player frozerPlayer;
    private static Vec3 frozenPosition;
    private static boolean previousHideGui;

    private ClientPlayback() {}

    public static void start(PlaybackStartPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (payload.keyframeList().size() < 2) {
            return;
        }

        stop();

        keyframes = payload.keyframeList();
        defaultInterpolationMode = payload.interpolationMode();
        int segments = keyframes.size() - 1;
        segDuration = payload.duration() / segments;

        unwrappedYaws = new float[keyframes.size()];
        unwrappedYaws[0] = keyframes.get(0).yaw();
        for (int i = 1; i < keyframes.size(); i++) {
            float delta = Mth.wrapDegrees(keyframes.get(i).yaw() - keyframes.get(i - 1).yaw());
            unwrappedYaws[i] = unwrappedYaws[i - 1] + delta;
        }

        anchor = new CameraAnchorEntity(mc.level);
        Keyframe first = keyframes.get(0);

        anchor.moveTo(first.position(), first.yaw(), first.pitch());
        anchor.moveTo(first.position(), first.yaw(), first.pitch());

        prevCameraEntity = mc.getCameraEntity();
        mc.setCameraEntity(anchor);

        previousHideGui = mc.options.hideGui;
        mc.options.hideGui = true;

        frozerPlayer = mc.player;
        frozenPosition = frozerPlayer.position();

        startTime = System.nanoTime();
    }
    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        if (anchor != null && mc.player != null) {
            mc.setCameraEntity(prevCameraEntity != null ? prevCameraEntity : mc.player);
        }
        mc.options.hideGui = previousHideGui;
        anchor = null;
        keyframes = null;
        unwrappedYaws = null;
        prevCameraEntity = null;
        frozerPlayer = null;
        frozenPosition = null;
    }
    public static boolean isPlaying() {
        return anchor != null;
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!isPlaying()) {
            return;
        }
        Input input = event.getInput();
        input.leftImpulse = 0f;
        input.forwardImpulse = 0f;
        input.up = false;
        input.down = false;
        input.left = false;
        input.right = false;
        input.jumping = false;
        input.shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onClientTickPinPlayer(ClientTickEvent.Post event) {
        if (!isPlaying() || frozerPlayer == null) {
            return;
        }
        frozerPlayer.setDeltaMovement(Vec3.ZERO);
        frozerPlayer.setPos(frozenPosition.x, frozenPosition.y, frozenPosition.z);
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (isPlaying() && event.getEntity() == frozerPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!isPlaying()) {
            return;
        }

        double elapsed = (System.nanoTime() - startTime) / 1_000_000_000.0;
        double totalDuration = segDuration * (keyframes.size() - 1);

        if (elapsed >= totalDuration) {
            Keyframe last =  keyframes.get(keyframes.size() - 1);
            anchor.moveTo(last.position(), last.yaw(), last.pitch());
            stop();
            return;
        }

        int segmentIndex = (int) (elapsed / segDuration);
        segmentIndex = Math.min(segmentIndex, keyframes.size() - 2);
        double segmentElapsed = elapsed - segmentIndex * segDuration;
        double t = segDuration <= 0 ? 1.0 : segmentElapsed / segDuration;

        Keyframe from =  keyframes.get(segmentIndex);
        Keyframe to =  keyframes.get(segmentIndex + 1);
        InterpolationMode mode = to.overrideInterpolation().orElse(defaultInterpolationMode);

        Vec3 before = keyframes.get(Math.max(segmentIndex - 1, 0)).position();
        Vec3 after = keyframes.get(Math.min(segmentIndex + 2, keyframes.size() - 1)).position();

        Vec3 position;
        float yaw;
        float pitch;

        switch (mode) {
            case INSTANT -> {
                position = to.position();
                yaw = unwrappedYaws[segmentIndex + 1];
                pitch = to.pitch();
            }
            case LINEAR -> {
                position = getPosition(before, after, from, to, t);
                yaw = lerpAngle(unwrappedYaws[segmentIndex], unwrappedYaws[segmentIndex + 1], t);
                pitch = lerpAngle(from.pitch(), to.pitch(), t);
            }
            case QUADRATIC -> {
                t = easeInOutQuadratic(t);
                position = getPosition(before, after, from, to, t);
                yaw = lerpAngle(unwrappedYaws[segmentIndex], unwrappedYaws[segmentIndex + 1], t);
                pitch = lerpAngle(from.pitch(), to.pitch(), t);
            }
            case CUBIC -> {
                t = easeInOutCubic(t);
                position = getPosition(before, after, from, to, t);
                yaw = lerpAngle(unwrappedYaws[segmentIndex], unwrappedYaws[segmentIndex + 1], t);
                pitch = lerpAngle(from.pitch(), to.pitch(), t);
            }
            case BOUNCE -> {
                t = easeOutBounce(t);
                position = getPosition(before, after, from, to, t);
                yaw = lerpAngle(unwrappedYaws[segmentIndex], unwrappedYaws[segmentIndex + 1], t);
                pitch = lerpAngle(from.pitch(), to.pitch(), t);
            }
            //default to Linear as a failsafe. Should never happen.
            default -> {
                position = getPosition(before, after, from, to, t);
                yaw = lerpAngle(unwrappedYaws[segmentIndex], unwrappedYaws[segmentIndex + 1], t);
                pitch = lerpAngle(from.pitch(), to.pitch(), t);
            }
        }

        anchor.moveTo(position, yaw, pitch);
    }

    private static Vec3 getPosition(Vec3 before, Vec3 after, Keyframe from, Keyframe to, double t) {
        return Config.ENABLE_SPLINE.getAsBoolean() ? catmullRom(before, from.position(), to.position(), after, t) : lerp(from.position(), to.position(), t);
    }

    private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
        return new Vec3(
                Mth.lerp(t, from.x, to.x),
                Mth.lerp(t, from.y, to.y),
                Mth.lerp(t, from.z, to.z)
        );
    }
    public static float lerpAngle(double from, double to, double t) {
        double diff = (to - from) % 360.0;
        double shortestDiff = ((diff + 540.0) % 360.0) - 180.0;
        return (float) (from + shortestDiff * t);
    }
}
