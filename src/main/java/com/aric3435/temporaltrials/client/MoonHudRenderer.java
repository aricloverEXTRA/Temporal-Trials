package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class MoonHudRenderer implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        if (!ClientLoopState.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!TemporalTrialsConfig.BACKGROUND_MUSIC_ENABLED) return;

        long loopDuration = TemporalTrialsConfig.getLoopDurationTicks();
        long remaining = ClientLoopState.getRemainingTicks();
        double fraction = 0.0;
        if (loopDuration > 0) {
            fraction = 1.0 - Math.max(0.0, Math.min(1.0, (double) remaining / (double) loopDuration));
        }

        int width = 80;
        int height = 8;
        int x = client.getWindow().getScaledWidth() - width - 10;
        int y = 10;

        ctx.fill(x, y, x + width, y + height, 0x60000000);
        int prog = (int) Math.round(fraction * width);
        ctx.fill(x, y, x + prog, y + height, 0xAAFFFFFF);

        String label = "Moon " + (int) Math.round(fraction * 100.0) + "%";
        ctx.drawText(client.textRenderer, Text.literal(label).asOrderedText(), x, y + height + 2, 0xFFFFFF, false);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new MoonHudRenderer());
    }
}