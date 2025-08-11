package com.aric3435.temporaltrials

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class LoopStatePayload(
  val isActive: Boolean,
  val startTime: Long,
  val showScreen: Boolean
) : CustomPayload {
  override fun getId() = ID

  companion object {
    // ↓ use the shared constant, NOT a private field in your mod class
    val ID = CustomPayload.Id<LoopStatePayload>(Constants.LOOP_STATE_ID)

    val CODEC: PacketCodec<PacketByteBuf, LoopStatePayload> = PacketCodec.of(
      { pkt, buf ->
        buf.writeBoolean(pkt.isActive)
           .writeLong(pkt.startTime)
           .writeBoolean(pkt.showScreen)
      },
      { buf ->
        LoopStatePayload(
          buf.readBoolean(),
          buf.readLong(),
          buf.readBoolean()
        )
      }
    )
  }
}