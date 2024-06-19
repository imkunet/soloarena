package dev.kunet.soloarena.util

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player

fun String.colorCode(colorChar: Char = '&'): String =
    ChatColor.translateAlternateColorCodes(colorChar, this)

fun Player.sendPacket(packetWrapper: PacketWrapper<*>) {
    PacketEvents.getAPI().playerManager.sendPacket(this, packetWrapper)
}

fun PacketWrapper<*>.send(player: Player) = player.sendPacket(this)

fun PacketWrapper<*>.send(players: List<Player>) = players.forEach { it.sendPacket(this) }

fun org.bukkit.Location.toPELocation() = com.github.retrooper.packetevents.protocol.world.Location(x, y, z, yaw, pitch)
fun org.bukkit.Location.toMutableLocation() = MutableLocation(x, y, z, yaw, pitch)
