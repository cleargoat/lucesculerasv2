package com.example.oceanlights;

/** Una luz estatica: posicion en el mundo, color y radio. */
public class LightData {
    public final double x, y, z;
    public final int rgb;
    public final float radius;

    public LightData(double x, double y, double z, int rgb, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rgb = rgb;
        this.radius = radius;
    }
}
