package com.example.oceanlights;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class Network {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(OceanLights.MODID, "main"),
            () -> "1",
            v -> true,
            v -> true);

    private Network() {
    }

    public static void register() {
        CHANNEL.registerMessage(0, LightsPacket.class,
                LightsPacket::encode, LightsPacket::decode, LightsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
