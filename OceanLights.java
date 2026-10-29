package com.example.oceanlights;

import net.minecraftforge.fml.common.Mod;

@Mod(OceanLights.MODID)
public class OceanLights {
    public static final String MODID = "oceanlights";

    public OceanLights() {
        Network.register();
    }
}
