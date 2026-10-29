package com.example.oceanlights.client;

import com.example.oceanlights.LightData;

import java.util.Collections;
import java.util.List;

public final class ClientLights {
    private static volatile List<LightData> lights = Collections.emptyList();
    private static volatile float intensity = 1.0f;
    private static volatile float shadows = 0.85f;

    private ClientLights() {
    }

    public static void set(List<LightData> list, float intensityValue, float shadowsValue) {
        lights = list;
        intensity = intensityValue;
        shadows = shadowsValue;
    }

    public static List<LightData> get() { return lights; }
    public static float getIntensity() { return intensity; }
    public static float getShadows() { return shadows; }

    public static void clear() {
        lights = Collections.emptyList();
    }
}
