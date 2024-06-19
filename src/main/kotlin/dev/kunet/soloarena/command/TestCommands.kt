package dev.kunet.soloarena.command

import dev.kunet.soloarena.SoloArenaPlugin
import dev.kunet.soloarena.npc.NPC
import dev.kunet.soloarena.npc.NPCSnapshot
import dev.kunet.soloarena.npc.npcSnapshot
import dev.kunet.soloarena.util.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.io.*

fun SoloArenaPlugin.registerTestCommands() {
    var furinaCount = 0
    var huTaoCount = 0

    createCommand("furina") {
        executor = {
            val player = ensurePlayer()
            player.send("&9${"summoning furina".replace("", " ")}".comp())

            val furina = NPC("Furina${++furinaCount}", player.location.toMutableLocation(), FurinaSkin)
            furina.addObserver(player)
        }
    }

    createCommand("hutao") {
        executor = {
            val player = ensurePlayer()
            player.send("&c${"summoning hu tao".replace("", " ")}".comp())

            val hutao = NPC("HuTao${++huTaoCount}", player.location.toMutableLocation(), HuTaoSkin)
            hutao.addObserver(player)
        }
    }

    var testRecorder: TestRecorder? = null
    createCommand("testrecorder") {
        executor = ctx@{
            val player = ensurePlayer()

            testRecorder?.let {
                it.endRecording()
                unregisterEvents(it)
                return@ctx
            }

            val theRecorder = TestRecorder(player)
            registerEventsSync(theRecorder)
            testRecorder = theRecorder
            sender.send("&aStarting recoridng".comp())
        }
    }

    createCommand("testplayback") {
        tabCompleter = {
            when (subArgs.size) {
                0, 1 -> File(".").listFiles()?.filter { it.name.endsWith(".move.brawl") }?.map { it.name }
                    ?: emptyCompleter(this)

                else -> emptyCompleter(this)
            }
        }

        executor = ctx@{
            val player = ensurePlayer()

            val fileName = subArgs.getOrNull(0)
            if (fileName.isNullOrEmpty()) {
                sender.send("&cSpecify a valid replay name".comp())
                return@ctx
            }

            if (!fileName.startsWith("recording-") || !fileName.endsWith(".move.brawl")) {
                sender.send("&cSpecify a valid replay name".comp())
                return@ctx
            }

            val replayFile = File(fileName)
            registerEventsSync(TestPlayback(player, replayFile))
            sender.send("&aPlaying back...".comp())
        }
    }
}

class TestRecorder(val player: Player) : Listener {
    val fileName = "recording-${System.currentTimeMillis()}.move.brawl"

    val objectOutputStream = ObjectOutputStream(FileOutputStream(fileName))

    @EventHandler
    private fun onTick(event: TickEvent) {
        player.npcSnapshot().write(objectOutputStream)
    }

    fun endRecording() {
        objectOutputStream.close()

        player.send("&aSaved to $fileName".comp())
    }
}

class TestPlayback(val player: Player, val replayFile: File) : Listener {
    val npc: NPC = NPC("〠 furina ↈ", player.location.toMutableLocation(), FurinaSkin)
    val inputStream = ObjectInputStream(FileInputStream(replayFile))

    var isObserving = false

    @EventHandler
    private fun onTick(event: TickEvent) {
        if (inputStream.available() <= 0) {
            inputStream.close()
            npc.removeObserver(player)
            player.send("&cFinished playback".comp())
            HandlerList.unregisterAll(this)
            return
        }

        val snap = NPCSnapshot()
        snap.read(inputStream)
        npc.restoreSnapshot(snap)

        if (!isObserving) {
            npc.addObserver(player)
            isObserving = true
        }

        npc.tick()
    }
}
