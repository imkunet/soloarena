package dev.kunet.soloarena.arena

import dev.kunet.soloarena.util.MutableLocation
import dev.kunet.soloarena.util.toMutableLocation
import org.bukkit.entity.Player

class Brawler(
    val playerHandle: Player? = null
) {
    var location: MutableLocation = playerHandle?.location?.toMutableLocation() ?: MutableLocation()
    var health: Int = 2200
    var slowedTicks = 0
    var hunger = 10
    var energy = 0.0
}