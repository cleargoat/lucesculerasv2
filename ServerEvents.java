package com.example.oceanlights;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

/**
 * Luces estaticas: cualquier armor stand (o entidad) con el tag "Luz" emite luz.
 *   Tag "Luz_<color>"   color por nombre (red, blue, azul...) o hex de 6 digitos (ej. Luz_ff8800)
 *   Tag "Luz_r<N>"      radio en bloques (2 a 48, por defecto 16)
 */
@Mod.EventBusSubscriber(modid = OceanLights.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static final String TAG = "Luz";
    private static final int MAX_LIGHTS = 8;
    private static final Set<UUID> hadLights = new HashSet<>();
    // Intensidad global (se manda a los clientes junto con las luces)
    private static float intensity = 1.0f;
    private static float shadows = 0.85f;    // 0 = la luz atraviesa todo, 1 = las paredes la bloquean del todo

    // ---------- Comandos ----------

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        List<String> names = new ArrayList<>(LightColors.NAMES.keySet());

        d.register(Commands.literal("oceanlights")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("remove")
                        .executes(c -> remove(c.getSource(), 5))
                        .then(Commands.argument("radio", IntegerArgumentType.integer(1, 64))
                                .executes(c -> remove(c.getSource(), IntegerArgumentType.getInteger(c, "radio")))))
                .then(Commands.literal("clear").executes(c -> clear(c.getSource())))
                .then(Commands.literal("sombras")
                        .then(Commands.argument("valor", FloatArgumentType.floatArg(0f, 1f))
                                .executes(c -> {
                                    shadows = FloatArgumentType.getFloat(c, "valor");
                                    c.getSource().sendSuccess(() -> Component.literal("Sombras: " + shadows), true);
                                    return 1;
                                })))
                .then(Commands.literal("intensidad")
                        .then(Commands.argument("valor", FloatArgumentType.floatArg(0.2f, 3f))
                                .executes(c -> {
                                    intensity = FloatArgumentType.getFloat(c, "valor");
                                    c.getSource().sendSuccess(() -> Component.literal("Intensidad: " + intensity), true);
                                    return 1;
                                })))
                .then(Commands.argument("color", StringArgumentType.word())
                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(names, b))
                        .executes(c -> place(c.getSource(), StringArgumentType.getString(c, "color"), 16))
                        .then(Commands.argument("radio", IntegerArgumentType.integer(2, 48))
                                .executes(c -> place(c.getSource(), StringArgumentType.getString(c, "color"),
                                        IntegerArgumentType.getInteger(c, "radio"))))));
    }

    private static int place(CommandSourceStack src, String colorText, int radius) {
        String color = colorText.toLowerCase(Locale.ROOT).replace("#", "");
        if (LightColors.parse(color) == null || !color.matches("[a-z0-9]+")) {
            src.sendFailure(Component.literal("Color no valido. Prueba: yellow, orange, red, blue, pink, white, green, cyan, purple... o hex como ff8800"));
            return 0;
        }
        Vec3 p = src.getPosition();
        String cmd = String.format(Locale.ROOT,
                "summon minecraft:armor_stand %.2f %.2f %.2f "
                        + "{Tags:[\"%s\",\"%s_%s\",\"%s_r%d\"],Invisible:1b,Invulnerable:1b,NoGravity:1b,Marker:1b,Silent:1b}",
                p.x, p.y, p.z, TAG, TAG, color, TAG, radius);
        src.getServer().getCommands().performPrefixedCommand(src.withSuppressedOutput(), cmd);
        src.sendSuccess(() -> Component.literal("Luz " + color + " creada (radio " + radius + ")"), true);
        return 1;
    }

    private static List<ArmorStand> lightStands(ServerLevel level, AABB box) {
        return level.getEntitiesOfClass(ArmorStand.class, box, e -> e.getTags().contains(TAG));
    }

    private static int remove(CommandSourceStack src, int radius) {
        AABB box = new AABB(src.getPosition(), src.getPosition()).inflate(radius);
        List<ArmorStand> found = lightStands(src.getLevel(), box);
        found.forEach(Entity::discard);
        src.sendSuccess(() -> Component.literal("Luces eliminadas: " + found.size()), true);
        return found.size();
    }

    private static int clear(CommandSourceStack src) {
        List<Entity> found = new ArrayList<>();
        for (Entity e : src.getLevel().getAllEntities()) {
            if (e.getTags().contains(TAG)) found.add(e);
        }
        found.forEach(Entity::discard);
        src.sendSuccess(() -> Component.literal("Luces eliminadas en esta dimension: " + found.size()), true);
        return found.size();
    }

    // ---------- Envio de luces a los clientes ----------

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 10 != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            AABB box = player.getBoundingBox().inflate(80);
            List<Entity> found = player.serverLevel().getEntities((Entity) null, box, e -> e.getTags().contains(TAG));

            if (found.isEmpty() && !hadLights.contains(player.getUUID())) continue;

            found.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));
            List<LightData> lights = new ArrayList<>();
            for (Entity e : found) {
                if (lights.size() >= MAX_LIGHTS) break;
                int rgb = 0xFFFFFF;
                float radius = 16f;
                for (String t : e.getTags()) {
                    if (!t.startsWith(TAG + "_")) continue;
                    String rest = t.substring(TAG.length() + 1);
                    if (rest.matches("r\\d{1,2}")) {
                        radius = Math.max(2, Math.min(48, Integer.parseInt(rest.substring(1))));
                    } else {
                        Integer c = LightColors.parse(rest);
                        if (c != null) rgb = c;
                    }
                }
                lights.add(new LightData(e.getX(), e.getY(), e.getZ(), rgb, radius));
            }

            if (lights.isEmpty()) hadLights.remove(player.getUUID());
            else hadLights.add(player.getUUID());
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LightsPacket(lights, intensity, shadows));
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        hadLights.clear();
    }
}
