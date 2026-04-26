package com.aric3435.temporaltrials.client;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import com.aric3435.temporaltrials.config.TemporalTrialsClientConfig;
import com.aric3435.temporaltrials.config.TemporalTrialsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Clock HUD drawn 20px above hotbar. Respects client option HIDE_CLOCK_DURING_TRANSITION.
 */
public final class ClockHudRenderer {
    private static final Identifier TEX_OUTER = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/clock_outer.png");
    private static final Identifier TEX_INNER = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/clock_inner.png");
    private static final Identifier TEX_ICON = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/icon_sun_moon.png");
    private static final Identifier TEX_ORB = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/minute_orb.png");

    public static void register() {
        HudRenderCallback.EVENT.register((ctx, tick) -> onHudRender(ctx, tick));
    }

    private static void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        if (TemporalTrialsClientConfig.HIDE_CLOCK_DURING_TRANSITION && DayTransitionController.isActive()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!ClientLoopState.isActive()) return;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        int hotbarHeight = 24;
        int yBase = sh - hotbarHeight - 20 - 64;
        int xBase = (sw / 2) - 32;
        int clockSize = 64;

        try {
            ctx.drawGuiTexture(net.minecraft.client.render.RenderLayer::getText, TEX_OUTER, xBase, yBase, clockSize, clockSize);
            ctx.drawGuiTexture(net.minecraft.client.render.RenderLayer::getText, TEX_INNER, xBase + 8, yBase + 8, clockSize - 16, clockSize - 16);
        } catch (Throwable t) {
            ctx.drawText(client.textRenderer, Text.literal("Clock").asOrderedText(), xBase, yBase, 0xFFFFFF, false);
        }

        long dayTicks = 24000L;
        long timeOfDay = client.world.getTimeOfDay() % dayTicks;
        double minuteFraction = (timeOfDay % 1000) / 1000.0;
        double minuteAngle = minuteFraction * Math.PI * 2.0;

        int centerX = xBase + clockSize / 2;
        int centerY = yBase + clockSize / 2;
        int radius = (clockSize / 2) - 6;

        int orbX = centerX + (int) Math.round(Math.cos(minuteAngle - Math.PI / 2) * radius) - 6;
        int orbY = centerY + (int) Math.round(Math.sin(minuteAngle - Math.PI / 2) * radius) - 6;

        try {
            ctx.drawGuiTexture(net.minecraft.client.render.RenderLayer::getText, TEX_ORB, orbX, orbY, 12, 12);
        } catch (Throwable ignored) {}

        boolean isNight = timeOfDay >= 13000;
        double iconAngle = (timeOfDay / (double) dayTicks) * Math.PI * 2.0;
        int iconRadius = radius / 2;
        int iconX = centerX + (int) Math.round(Math.cos(iconAngle - Math.PI / 2) * iconRadius) - 8;
        int iconY = centerY + (int) Math.round(Math.sin(iconAngle - Math.PI / 2) * iconRadius) - 8;

        try {
            ctx.drawTexture(net.minecraft.client.render.RenderLayer::getText, TEX_ICON, iconX, iconY, 0f, isNight ? 16f : 0f, 16, 16, 16, 32);
        } catch (Throwable ignored) {}

        String dayLabel = "Day " + ClientLoopState.getDay();
        ctx.drawText(client.textRenderer, Text.literal(dayLabel).asOrderedText(), centerX - client.textRenderer.getWidth(dayLabel) / 2, yBase + clockSize + 4, 0xFFDDAA, false);
    }
}