package com.example.oceanlights;

import com.example.oceanlights.client.ClientLights;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Servidor -> cliente: luces cercanas (armor stands con el tag Luz), intensidad y fuerza de las sombras. */
public class LightsPacket {
    public final List<LightData> lights;
    public final float intensity;
    public final float shadows;

    public LightsPacket(List<LightData> lights, float intensity, float shadows) {
        this.lights = lights;
        this.intensity = intensity;
        this.shadows = shadows;
    }

    public static void encode(LightsPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.intensity);
        buf.writeFloat(msg.shadows);
        buf.writeVarInt(msg.lights.size());
        for (LightData l : msg.lights) {
            buf.writeDouble(l.x);
            buf.writeDouble(l.y);
            buf.writeDouble(l.z);
            buf.writeInt(l.rgb);
            buf.writeFloat(l.radius);
        }
    }

    public static LightsPacket decode(FriendlyByteBuf buf) {
        float intensity = buf.readFloat();
        float shadows = buf.readFloat();
        int n = buf.readVarInt();
        List<LightData> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(new LightData(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(), buf.readFloat()));
        }
        return new LightsPacket(list, intensity, shadows);
    }

    public static void handle(LightsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientLights.set(msg.lights, msg.intensity, msg.shadows)));
        ctx.get().setPacketHandled(true);
    }
}
