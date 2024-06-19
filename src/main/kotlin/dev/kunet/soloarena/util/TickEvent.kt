package dev.kunet.soloarena.util

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class TickEvent(val tickNumber: Long) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
