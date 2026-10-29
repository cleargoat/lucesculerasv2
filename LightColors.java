package com.example.oceanlights;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Nombres de colores (ingles y espanol) y hexadecimal de 6 digitos. */
public final class LightColors {
    public static final Map<String, Integer> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put("yellow", 0xFFE933);
        NAMES.put("orange", 0xFF7A0A);
        NAMES.put("red", 0xFF1A1A);
        NAMES.put("blue", 0x2F52FF);
        NAMES.put("pink", 0xFF5CBE);
        NAMES.put("white", 0xFFFFFF);
        NAMES.put("green", 0x20E040);
        NAMES.put("lime", 0xA6FF2E);
        NAMES.put("cyan", 0x20E8FF);
        NAMES.put("aqua", 0x20E8FF);
        NAMES.put("purple", 0x9A33FF);
        NAMES.put("violet", 0xB04CFF);
        NAMES.put("magenta", 0xFF22E0);
        NAMES.put("gold", 0xFFC21A);
        NAMES.put("lightblue", 0x6FC3FF);
        NAMES.put("turquoise", 0x30E0C0);
        NAMES.put("brown", 0xA05A20);
        NAMES.put("amarillo", 0xFFE933);
        NAMES.put("naranja", 0xFF7A0A);
        NAMES.put("rojo", 0xFF1A1A);
        NAMES.put("azul", 0x2F52FF);
        NAMES.put("rosa", 0xFF5CBE);
        NAMES.put("blanco", 0xFFFFFF);
        NAMES.put("verde", 0x20E040);
        NAMES.put("celeste", 0x6FC3FF);
        NAMES.put("morado", 0x9A33FF);
        NAMES.put("dorado", 0xFFC21A);
    }

    private LightColors() {
    }

    /** Devuelve el color RGB o null si no es valido. */
    public static Integer parse(String text) {
        String t = text.toLowerCase(Locale.ROOT).trim();
        if (t.startsWith("#")) t = t.substring(1);
        Integer c = NAMES.get(t);
        if (c == null && t.matches("[0-9a-f]{6}")) c = Integer.parseInt(t, 16);
        return c;
    }
}
