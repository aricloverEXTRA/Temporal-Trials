package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Shows a centered "Dawn of the X Day" overlay with fade in/hold/fade out.
 * Respects client config SHOW_DAY_TRANSITION_SCREEN.
 */
public final class DayTransitionController {
    private static final int FADE_IN = 60;
    private static final int HOLD = 40;
    private static final int FADE_OUT = 60;
    private static final int DURATION_TICKS = FADE_IN + HOLD + FADE_OUT;

    private static int counter = 0;
    private static String title = "";
    private static String subtitle = "";

    private DayTransitionController() {}

    public static void showTransition(int day) {
        if (!TemporalTrialsClientConfig.SHOW_DAY_TRANSITION_SCREEN) return;
        title = "Dawn of the " + TemporalTrialsConfig.getDayName(day);
        subtitle = (day >= TemporalTrialsConfig.CYCLE_LENGTH_DAYS) ? "The Final Day" : "";
        counter = DURATION_TICKS;
    }

    private static void onHud(DrawContext ctx, RenderTickCounter tickCounter) {
        if (counter <= 0) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();

        int t = counter;
        int alpha;
        if (t > (HOLD + FADE_OUT)) { // fade-in
            double f = (double)(DURATION_TICKS - t) / (double) FADE_IN;
            alpha = (int) Math.round(f * 255.0);
        } else if (t > FADE_OUT) { // hold
            alpha = 255;
        } else { // fade-out
            double f = (double) t / (double) FADE_OUT;
            alpha = (int) Math.round(f * 255.0);
        }
        alpha = Math.max(0, Math.min(255, alpha));

        int boxW = Math.min(w - 80, 600);
        int boxH = 80;
        int x = (w - boxW) / 2;
        int y = (h / 2) - (boxH / 2);

        ctx.fill(x, y, x + boxW, y + boxH, ((alpha & 0xFF) << 24) | 0x00102030);

        ctx.drawText(client.textRenderer, Text.literal(title).asOrderedText(), w / 2 - client.textRenderer.getWidth(title) / 2, y + 10, ((alpha & 0xFF) << 24) | 0xFFFFFF, false);
        if (!subtitle.isEmpty()) {
            ctx.drawText(client.textRenderer, Text.literal(subtitle).asOrderedText(), w / 2 - client.textRenderer.getWidth(subtitle) / 2, y + 34, ((alpha & 0xFF) << 24) | 0xFFDDAA, false);
        }

        counter = Math.max(0, counter - 1);
    }

    public static boolean isActive() {
        return counter > 0;
    }

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tick) -> onHud(ctx, tick));
    }
}