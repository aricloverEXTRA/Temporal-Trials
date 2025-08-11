package com.aric3435.temporaltrials

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.Box
import net.minecraft.world.World
import net.minecraft.world.World.ExplosionSourceType
import net.minecraft.world.gen.WorldPreset

object TemporalTrialsMod : ModInitializer {
  private val FLUTE      = ModItems.FLUTE_OF_TIME
  private val CHANNEL    = Constants.LOOP_STATE_ID

  private var loopStart  = 0L
  private var loopActive = false
  private val savedInv   = arrayOfNulls<ItemStack>(36)

  private var lastUse    = 0L
  private const val COOLDOWN  = 6_000L
  private const val DURATION  = 36_000L
  private const val INTERVAL  = 3_600L

  private var paleWeight = 5
  private val chatCd     = mutableMapOf<PlayerEntity, Long>()

  class LoopState(val active: Boolean, val start: Long, val show: Boolean) : CustomPayload {
    override fun getId() = ID
    companion object {
      val ID   = CustomPayload.Id<LoopState>(CHANNEL)
      val CODEC = PacketCodec.of<PacketByteBuf, LoopState>(
        { pkt, buf ->
          buf.writeBoolean(pkt.active)
             .writeLong(pkt.start)
             .writeBoolean(pkt.show)
        },
        { buf ->
          LoopState(
            buf.readBoolean(),
            buf.readLong(),
            buf.readBoolean()
          )
        }
      )
    }
  }

  override fun onInitialize() {
    ModItems.initialize()

    // Flute usage
    UseItemCallback.EVENT.register { player, world, hand ->
      if (world.isClient) return@register ActionResult.PASS
      // STUB: always allow in every world
      // if (!isTemporalWorld(world)) return@register ActionResult.PASS
      if (player.getStackInHand(hand).item != FLUTE) return@register ActionResult.PASS

      val now = world.timeOfDay
      if (now - lastUse < COOLDOWN) {
        player.sendMessage(Text.of("Flute is on cooldown."), true)
        return@register ActionResult.PASS
      }
      lastUse = now

      saveInv(player)
      resetLoop(world as ServerWorld, fluteTriggered = true)
      player.sendMessage(Text.of("Time rewound."), false)
      sendLoopPacket(world, show = true)
      ActionResult.SUCCESS
    }

    // Strip stray flutes
    ServerTickEvents.END_SERVER_TICK.register { server ->
      server.worlds.forEach { world ->
        // if (!isTemporalWorld(world)) return@forEach
        world.players.forEach { p ->
          repeat(p.inventory.size()) { i ->
            if (p.inventory.getStack(i).item == FLUTE)
              p.inventory.setStack(i, ItemStack.EMPTY)
          }
          if (p.getStackInHand(Hand.OFF_HAND).item == FLUTE)
            p.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY)
          p.inventory.armor.indices.forEach { i ->
            if (p.inventory.armor[i].item == FLUTE)
              p.inventory.armor[i] = ItemStack.EMPTY
          }
        }
      }
    }

    // Main loop
    ServerTickEvents.END_WORLD_TICK.register { world ->
      if (world.isClient || !loopActive) return@register

      val elapsed = world.timeOfDay - loopStart
      sendLoopPacket(world as ServerWorld, show = false)

      if (elapsed % INTERVAL == 0L && paleWeight < 75) {
        paleWeight += 7
        world.players.forEach {
          it.sendMessage(Text.of("Pale Garden spawn rate: $paleWeight%"), true)
        }
      }

      if (world.timeOfDay % 1000L == 0L) {
        world.players.forEach { p ->
          val hasElytra = (p.inventory.main + p.inventory.offHand + p.inventory.armor)
            .any { it.item == Items.ELYTRA }
          val lastMsg = chatCd[p] ?: 0L
          if (hasElytra && world.timeOfDay - lastMsg >= 1000L) {
            p.sendMessage(Text.of("Fly to the moon before time runs out!"), true)
            chatCd[p] = world.timeOfDay
          }
        }
      }

      if (elapsed >= DURATION) {
        if (!checkWin(world as ServerWorld)) {
          triggerChaos(world)
          resetLoop(world, fluteTriggered = false)
          sendLoopPacket(world, show = true)
          world.players.forEach {
            it.sendMessage(Text.of("Day 3 ended — resetting time."), false)
          }
        }
      }
    }
  }

  // Stubbed out—always returns true
  private fun isTemporalWorld(world: World): Boolean = true

  private fun saveInv(player: PlayerEntity) {
    repeat(player.inventory.size()) { i ->
      savedInv[i] = player.inventory.getStack(i).copy()
    }
  }

  private fun loadInv(player: PlayerEntity) {
    repeat(player.inventory.size()) { i ->
      player.inventory.setStack(i, savedInv[i]?.copy() ?: ItemStack.EMPTY)
    }
  }

  private fun resetLoop(world: ServerWorld, fluteTriggered: Boolean) {
    loopStart  = world.timeOfDay
    loopActive = true
    paleWeight = 5
    world.setTimeOfDay(0L)

    world.players.forEach { p ->
      if (fluteTriggered) saveInv(p)
      loadInv(p)
      p.health = p.maxHealth
      p.setPos(0.0, world.seaLevel.toDouble(), 0.0)
    }

    val border = world.worldBorder
    val bounds = Box(
      border.boundWest,  world.bottomY.toDouble(), border.boundNorth,
      border.boundEast,  (world.bottomY + world.height).toDouble(), border.boundSouth
    )
    world.getEntitiesByClass(Entity::class.java, bounds) { it !is PlayerEntity }
      .forEach { it.discard() }
  }

  private fun checkWin(world: ServerWorld): Boolean {
    world.players.forEach { p ->
      val stacks = p.inventory.main + p.inventory.offHand + p.inventory.armor
      if (stacks.any { it.item == Items.ELYTRA && (it.maxDamage - it.damage) > 100 }) {
        p.sendMessage(Text.of("Chaos averted!"), false)
        loopActive = false
        sendLoopPacket(world, show = false)
        return true
      }
    }
    return false
  }

  private fun triggerChaos(world: ServerWorld) {
    world.players.forEach { p ->
      p.sendMessage(Text.of("World descends into chaos!"), false)
      val pos = p.blockPos
      world.createExplosion(
        null as Entity?,
        pos.x.toDouble(),
        pos.y.toDouble(),
        pos.z.toDouble(),
        4.0f, true, ExplosionSourceType.TNT
      )
    }
  }

  private fun sendLoopPacket(world: ServerWorld, show: Boolean) {
    val packet = LoopState(loopActive, loopStart, show)
    world.players.forEach {
      ServerPlayNetworking.send(it as ServerPlayerEntity, packet)
    }
  }
}
