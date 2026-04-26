package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

/**
 * MoonFallController: HUD-based moon descent cinematic.
 * - draws a large moon sprite that moves downward as it approaches impact
 * - draws red fog overlay and countdown timer
 */
public final class MoonFallController {
    private static final Identifier MOON_TEX = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/moon_overlay.png");

    private static boolean active = false;
    private static long initialRemaining = 0L;
    private static long currentRemaining = 0L;

    private MoonFallController() {}

    public static void onPayload(boolean payloadShowIntro, int day, long remainingTicks) {
        int finalDay = TemporalTrialsConfig.CYCLE_LENGTH_DAYS;
        long warningThreshold = 5 * 60 * 20L; // 5 minutes in ticks

        if (payloadShowIntro || (day >= finalDay && remainingTicks <= warningThreshold)) {
            if (!active) {
                active = true;
                initialRemaining = remainingTicks;
            }
            currentRemaining = remainingTicks;
        } else {
            if (active && remainingTicks > warningThreshold) {
                active = false;
                initialRemaining = 0;
                currentRemaining = 0;
            }
        }
    }

    private static void onHudRender(DrawContext ctx, float tickDelta) {
        if (!active) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        double progress = 0.0;
        if (initialRemaining > 0) {
            progress = 1.0 - ((double) currentRemaining / (double) Math.max(1L, initialRemaining));
            progress = Math.max(0.0, Math.min(1.0, progress));
        }

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        // red fog overlay (increasing alpha with progress)
        int alpha = (int) Math.round(30 + progress * 160); // 30 -> ~190
        ctx.fill(0, 0, sw, sh, ((alpha & 0xFF) << 24) | 0x220000);

        // Moon sprite: grows and descends with progress
        int moonSize = (int) (64 + progress * 320); // 64 -> 384
        int x = (sw / 2) - (moonSize / 2);
        int y = (int) (sh * 0.08 + progress * (sh * 0.7)); // moves from near top downwards

        try {
            ctx.drawTexture(RenderLayer::getEntitySolid, MOON_TEX, x, y, 0f, 0f, moonSize, moonSize, moonSize, moonSize);
        } catch (Throwable t) {
            // fallback: text label if texture fails
            String lbl = "MOON";
            ctx.drawText(client.textRenderer, Text.literal(lbl).asOrderedText(), sw / 2 - client.textRenderer.getWidth(lbl) / 2, y, 0xFFFFFF, false);
        }

        // Countdown timer mm:ss
        long totalSecs = currentRemaining / 20;
        long mins = totalSecs / 60;
        long secs = totalSecs % 60;
        String timeStr = String.format("%02d:%02d", mins, secs);
        String info = "Moonfall: " + timeStr;
        ctx.drawText(client.textRenderer, Text.literal(info).asOrderedText(), 10, 10, 0xFFB0B0, false);

        // small jitter effect for "chaos"
        if (progress > 0.6) {
            int shake = (int) (Math.sin((System.currentTimeMillis() / 100.0)) * (progress * 3));
            ctx.fill(0 + shake, sh - 50 + shake, sw + shake, sh, 0x10FF0000);
        }
    }

    public static void register() {
        HudRenderCallback.EVENT.register(MoonFallController::onHudRender);
    }
}