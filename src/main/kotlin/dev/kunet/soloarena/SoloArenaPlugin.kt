package dev.kunet.soloarena

import com.github.retrooper.packetevents.PacketEvents
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import dev.kunet.soloarena.command.registerTestCommands
import dev.kunet.soloarena.util.TickEvent
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

class SoloArenaPlugin : SuspendingJavaPlugin() {
    override fun onEnable() {
        val api = SpigotPacketEventsBuilder.build(this)
        api.settings
            .debug(false)
            .bStats(false)
            .checkForUpdates(false)
            .reEncodeByDefault(false)
        PacketEvents.setAPI(api)
        PacketEvents.getAPI().load()

        registerTestCommands()

        registerEvents(UniversalListeners())
        startTickEvents()
    }

    fun registerEvents(listener: Listener) {
        Bukkit.getPluginManager().registerSuspendingEvents(listener, this)
    }

    fun registerEventsSync(listener: Listener) {
        Bukkit.getPluginManager().registerEvents(listener, this)
    }

    fun unregisterEvents(listener: Listener) {
        HandlerList.unregisterAll(listener)
    }

    fun startTickEvents() {
        var tickNumber = 0L
        server.scheduler.runTaskTimer(this, {
            server.pluginManager.callEvent(TickEvent(tickNumber++))
        }, 0L, 1L)
    }
}
