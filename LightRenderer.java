package com.example.oceanlights.client;

import com.example.oceanlights.LightData;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

/** Dibuja el reflejo de las luces estaticas sobre lo que ya se renderizo, usando la profundidad de la pantalla. */
public final class LightRenderer {
    private LightRenderer() {
    }

    public static void render(Matrix4f proj, Matrix4f view, Vec3 cam) {
        ShaderInstance sh = ClientSetup.lightShader;
        List<LightData> lights = ClientLights.get();
        if (sh == null || lights.isEmpty()) return;

        float intensity = ClientLights.getIntensity();
        float[] posA = new float[16], posB = new float[16], colA = new float[16], colB = new float[16];
        for (int i = 0; i < Math.min(8, lights.size()); i++) {
            LightData l = lights.get(i);
            Vector4f v = new Vector4f((float) (l.x - cam.x), (float) (l.y + 0.8 - cam.y), (float) (l.z - cam.z), 1f);
            view.transform(v);
            float[] pos = i < 4 ? posA : posB;
            float[] col = i < 4 ? colA : colB;
            int o = (i % 4) * 4;
            pos[o] = v.x;
            pos[o + 1] = v.y;
            pos[o + 2] = v.z;
            pos[o + 3] = l.radius;
            col[o] = ((l.rgb >> 16) & 255) / 255f;
            col[o + 1] = ((l.rgb >> 8) & 255) / 255f;
            col[o + 2] = (l.rgb & 255) / 255f;
            col[o + 3] = intensity;
        }

        RenderTarget rt = Minecraft.getInstance().getMainRenderTarget();
        sh.setSampler("DepthSampler", rt.getDepthTextureId());
        sh.safeGetUniform("InvProj").set(new Matrix4f(proj).invert());
        sh.safeGetUniform("Proj").set(proj);
        sh.safeGetUniform("PosA").set(posA);
        sh.safeGetUniform("PosB").set(posB);
        sh.safeGetUniform("ColA").set(colA);
        sh.safeGetUniform("ColB").set(colB);
        sh.safeGetUniform("Params").set(ClientLights.getShadows(), 0f, 0f, 0f);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.setShader(() -> sh);

        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        b.vertex(-1, -1, 0).endVertex();
        b.vertex(1, -1, 0).endVertex();
        b.vertex(1, 1, 0).endVertex();
        b.vertex(-1, 1, 0).endVertex();
        BufferUploader.drawWithShader(b.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }
}
