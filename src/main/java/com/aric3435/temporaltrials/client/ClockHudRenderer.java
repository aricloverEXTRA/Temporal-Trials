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
 * Analog-style clock HUD placed 20px above the hotbar.
 * Sprites expected in resources/assets/temporal_trials/textures/gui/:
 * - clock_outer.png
 * - clock_inner.png
 * - icon_sun_moon.png
 * - minute_orb.png
 *
 * Visuals:
 * - outer ring is decorative (drawn static)
 * - inner circle drawn static
 * - sun/moon icon orbits the outer ring (indicates day/night)
 * - minute_orb is positioned around outer ring according to minutes (ticks)
 */
public final class ClockHudRenderer {
    private static final Identifier TEX_OUTER = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/clock_outer.png");
    private static final Identifier TEX_INNER = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/clock_inner.png");
    private static final Identifier TEX_ICON = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/icon_sun_moon.png");
    private static final Identifier TEX_ORB = Identifier.of(TemporalTrialsMod.MOD_ID, "textures/gui/minute_orb.png");

    public static void register() {
        HudRenderCallback.EVENT.register(ClockHudRenderer::onHudRender);
    }

    private static void onHudRender(DrawContext ctx, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        if (!ClientLoopState.isActive()) return;

        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        // Hotbar position approx: bottom - 24. Place clock 20px above that
        int hotbarHeight = 24;
        int yBase = sh - hotbarHeight - 20 - 64; // 64 = clock size
        int xBase = (sw / 2) - 32; // 64 width -> center

        int clockSize = 64;

        // draw outer and inner rings
        try {
            ctx.drawTexture(RenderLayer::getEntitySolid, TEX_OUTER, xBase, yBase, 0f, 0f, clockSize, clockSize, clockSize, clockSize);
            ctx.drawTexture(RenderLayer::getEntitySolid, TEX_INNER, xBase + 8, yBase + 8, 0f, 0f, clockSize - 16, clockSize - 16, clockSize - 16, clockSize - 16);
        } catch (Throwable t) {
            // ignore texture errors; draw fallback text
            ctx.drawText(client.textRenderer, Text.literal("Clock").asOrderedText(), xBase, yBase, 0xFFFFFF, false);
        }

        // Compute minute angle from world time
        long dayTicks = 24000L;
        long timeOfDay = client.world.getTimeOfDay() % dayTicks; // 0..23999
        double minuteFraction = (timeOfDay % 1000) / 1000.0; // 1000 ticks = "minute" here (arbitrary)
        double minuteAngle = minuteFraction * Math.PI * 2.0;

        // place minute orb along outer ring
        int centerX = xBase + clockSize / 2;
        int centerY = yBase + clockSize / 2;
        int radius = (clockSize / 2) - 6;

        int orbX = centerX + (int) Math.round(Math.cos(minuteAngle - Math.PI / 2) * radius) - 6;
        int orbY = centerY + (int) Math.round(Math.sin(minuteAngle - Math.PI / 2) * radius) - 6;

        try {
            ctx.drawTexture(RenderLayer::getEntitySolid, TEX_ORB, orbX, orbY, 0f, 0f, 12, 12, 12, 12);
        } catch (Throwable ignored) {}

        // place sun/moon icon depending on time (day/night)
        boolean isNight = timeOfDay >= 13000;
        // orbit the icon at half radius, different angle (use proportional to day fraction)
        double dayFraction = (double) ClientLoopState.getDay() / Math.max(1.0, TemporalTrialsConfig.CYCLE_LENGTH_DAYS);
        double iconAngle = (timeOfDay / (double) dayTicks) * Math.PI * 2.0;

        int iconRadius = radius / 2;
        int iconX = centerX + (int) Math.round(Math.cos(iconAngle - Math.PI / 2) * iconRadius) - 8;
        int iconY = centerY + (int) Math.round(Math.sin(iconAngle - Math.PI / 2) * iconRadius) - 8;

        try {
            ctx.drawTexture(RenderLayer::getEntitySolid, TEX_ICON, iconX, iconY, 0f, isNight ? 16f : 0f, 16, 16, 16, 32);
        } catch (Throwable ignored) {}

        // small day label under clock
        String dayLabel = "Day " + ClientLoopState.getDay();
        ctx.drawText(client.textRenderer, Text.literal(dayLabel).asOrderedText(), centerX - client.textRenderer.getWidth(dayLabel) / 2, yBase + clockSize + 4, 0xFFDDAA, false);
    }
}