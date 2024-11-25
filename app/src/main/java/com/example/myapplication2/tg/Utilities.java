package com.example.myapplication2.tg;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author a.s.korchagin
 */
public class Utilities {

    public static Random fastRandom = new Random(System.nanoTime());

    public static long clamp(long value, long maxValue, long minValue) {
        return Math.max(Math.min(value, maxValue), minValue);
    }

    public static float clamp(float value, float maxValue, float minValue) {
        if (Float.isNaN(value)) {
            return minValue;
        }
        if (Float.isInfinite(value)) {
            return maxValue;
        }
        return Math.max(Math.min(value, maxValue), minValue);
    }
}
