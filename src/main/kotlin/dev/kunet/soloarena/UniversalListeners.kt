package dev.kunet.soloarena

import dev.kunet.soloarena.util.TickEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

class UniversalListeners : Listener {
    @EventHandler
    private fun handleWorldInit(event: WorldInitEvent) {
        event.world.keepSpawnInMemory = false
        event.world.isAutoSave = false

        event.world.setStorm(false)
        event.world.isThundering = false

        event.world.weatherDuration = Int.MAX_VALUE
        event.world.thunderDuration = Int.MAX_VALUE

        event.world.setGameRuleValue("doDaylightCycle", "false")
        event.world.setGameRuleValue("doMobSpawning", "false")
        event.world.setGameRuleValue("doFireTick", "false")
        event.world.setGameRuleValue("doMobLoot", "false")
        event.world.setGameRuleValue("doMobSpawning", "false")
        event.world.setGameRuleValue("randomTickSpeed", "0")

        event.world.time = 140
    }

    @EventHandler
    private fun onTick(event: TickEvent) {
        if (event.tickNumber % 20L != 0L) return
        for (world in Bukkit.getWorlds()) {
            world.setStorm(false)
            world.isThundering = false

            world.weatherDuration = Int.MAX_VALUE
            world.thunderDuration = Int.MAX_VALUE
        }
    }
}
