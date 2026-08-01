package com.ianeli.camerakey.util;

import net.minecraft.world.phys.Vec3;

public class ease {
    public static double easeInOutQuadratic(double x) {
        if (x < 0.5) {
            return 2.0 * x * x;
        } else {
            return 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0;
        }
    }

    public static double easeInOutCubic(double x) {
        if (x < 0.5) {
            return 4.0 * x * x * x;
        } else {
            return 1.0 - Math.pow(-2.0 * x + 2.0, 3.0) / 2.0;
        }
    }

    public static double easeOutBounce(double x) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (x < 1 / d1) {
            return n1 * x * x;
        } else if (x < 2 / d1) {
            return n1 * (x -= 1.5 / d1) * x + 0.75;
        } else if (x < 2.5 / d1) {
            return n1 * (x -= 2.25 / d1) * x + 0.9375;
        } else {
            return n1 * (x -= 2.625 / d1) * x + 0.984375;
        }
    }

    public static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return new Vec3(
                catmullRomComponent(p0.x, p1.x, p2.x, p3.x, t, t2, t3),
                catmullRomComponent(p0.y, p1.y, p2.y, p3.y, t, t2, t3),
                catmullRomComponent(p0.z, p1.z, p2.z, p3.z, t, t2, t3)
        );
    }
    private static double catmullRomComponent(double p0, double p1, double p2, double p3, double t, double t2, double t3) {
        return 0.5 * (
                (2 * p1)
                        + (-p0 + p2) * t
                        + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2
                        + (-p0 + 3 * p1 - 3 * p2 + p3) * t3
        );
    }
}
