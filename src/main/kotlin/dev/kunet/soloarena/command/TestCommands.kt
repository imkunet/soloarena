package dev.kunet.soloarena.command

import dev.kunet.soloarena.SoloArenaPlugin
import dev.kunet.soloarena.arena.mech.animateCone
import dev.kunet.soloarena.npc.NPC
import dev.kunet.soloarena.npc.NPCSnapshot
import dev.kunet.soloarena.npc.npcSnapshot
import dev.kunet.soloarena.util.*
import net.jpountz.lz4.LZ4FrameInputStream
import net.jpountz.lz4.LZ4FrameOutputStream
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerAnimationEvent
import org.bukkit.event.player.PlayerInteractEvent
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
                testRecorder = null
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
            registerEventsSync(TestPlayback(player, replayFile, "${++furinaCount}"))
            sender.send("&aPlaying back...".comp())
        }
    }

    createCommand("gogogo") {
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

            registerEventsSync(GoEr(fileName, player, this@registerTestCommands))
            sender.send("&aPlaying back...".comp())
        }
    }

    createCommand("cone") {
        executor = {
            val player = ensurePlayer()
            animateCone(player.world, player.location.toMutableLocation(), player.isSneaking)
        }
    }
}

class GoEr(val fileName: String, val player: Player, val plugin: SoloArenaPlugin) : Listener {
    private var count = 0

    @EventHandler
    private fun onTick(event: TickEvent) {
        if (count > 250) {
            HandlerList.unregisterAll(this)
            return
        }

        val replayFile = File(fileName)
        plugin.registerEventsSync(TestPlayback(player, replayFile, "${++count}"))
    }
}

class TestRecorder(val player: Player) : Listener {
    val fileName = "recording-${System.currentTimeMillis()}.move.brawl"
    val outputStream = ObjectOutputStream(LZ4FrameOutputStream(FileOutputStream(fileName)))

    var isSwingTick = false

    @EventHandler
    private fun onTick(event: TickEvent) {
        val npcSnapshot = player.npcSnapshot()
        npcSnapshot.hitTick = isSwingTick
        isSwingTick = false
        npcSnapshot.write(outputStream)
    }

    @EventHandler
    private fun onSwing(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_AIR && event.action != Action.LEFT_CLICK_BLOCK) return
        isSwingTick = true
    }

    @EventHandler
    private fun onAnimation(event: PlayerAnimationEvent) {
        isSwingTick = true
    }

    fun endRecording() {
        outputStream.close()
        player.send(
            "&aSaved to $fileName. ".comp()
                    + "&9&nClick to play".comp()
                .setAction(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/testplayback $fileName"))
                    + " &9&nBrainrot".comp()
                .setAction(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/gogogo $fileName"))
        )
    }
}

class TestPlayback(val player: Player, val replayFile: File, val name: String) : Listener {
    val npc: NPC = NPC(name, player.location.toMutableLocation(), if (Math.random() > 0.5) FurinaSkin else HuTaoSkin)
    val inputStream = ObjectInputStream(LZ4FrameInputStream(FileInputStream(replayFile)))

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
