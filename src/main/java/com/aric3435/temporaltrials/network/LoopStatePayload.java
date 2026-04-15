package com.aric3435.temporaltrials.network;

import com.aric3435.temporaltrials.TemporalTrialsMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Typed CustomPayload used by the Fabric play-payload API in these mappings.
 * Provides ID and CODEC so ClientPlayNetworking.registerGlobalReceiver can use it.
 */
public final class LoopStatePayload implements CustomPayload {

    public static final Id<LoopStatePayload> ID =
            new Id<>(Identifier.of(TemporalTrialsMod.MOD_ID, "loop_state"));

    public static final PacketCodec<PacketByteBuf, LoopStatePayload> CODEC =
            PacketCodec.of(LoopStatePayload::write, LoopStatePayload::read);

    private final boolean active;
    private final int day;
    private final long remainingTicks;
    private final boolean showIntro;

    public LoopStatePayload(boolean active, int day, long remainingTicks, boolean showIntro) {
        this.active = active;
        this.day = day;
        this.remainingTicks = remainingTicks;
        this.showIntro = showIntro;
    }

    public boolean isActive() {
        return active;
    }

    public int getDay() {
        return day;
    }

    public long getRemainingTicks() {
        return remainingTicks;
    }

    public boolean shouldShowIntro() {
        return showIntro;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    private static void write(LoopStatePayload payload, PacketByteBuf buf) {
        buf.writeBoolean(payload.active);
        buf.writeVarInt(payload.day);
        buf.writeVarLong(payload.remainingTicks);
        buf.writeBoolean(payload.showIntro);
    }

    private static LoopStatePayload read(PacketByteBuf buf) {
        boolean active = buf.readBoolean();
        int day = buf.readVarInt();
        long remaining = buf.readVarLong();
        boolean showIntro = buf.readBoolean();
        return new LoopStatePayload(active, day, remaining, showIntro);
    }
}
