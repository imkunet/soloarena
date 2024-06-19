package dev.kunet.soloarena.command

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import dev.kunet.soloarena.util.colorCode
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import kotlin.math.max

class CommandContext(
    val sender: CommandSender,
    val player: Player?,

    val args: Array<out String>,
    val subArgs: Array<out String>,
) {
    fun lastSubArg() = subArgs.getOrElse(subArgs.lastIndex) { "" }

    fun ensurePlayer(): Player {
        if (player == null)
            throw CommandInterrupt("&cYou must be a player to use this!".colorCode())

        return player
    }

    fun getPlayer(target: String?): Player? {
        target ?: return null
        var find = Bukkit.getOnlinePlayers().find { it.name.equals(target, true) }
        if (find == null) find = Bukkit.getOnlinePlayers().find { it.uniqueId.toString() == target }
        if (find == null) find = Bukkit.getOnlinePlayers().find { it.name.startsWith(target, true) }
        return find
    }
}

class CommandInterrupt(message: String?) : Exception(message)

class CommandBuilder(
    val name: String,
    val description: String?,
    val permission: String?,
    val usageMessage: String?,
    val aliases: Set<String>,
) {
    val emptyCompleter: (CommandContext.() -> Collection<String>) = { listOf() }

    var executor: CommandContext.() -> Unit = { sender.sendMessage("&cThis command doesn't do anything".colorCode()) }
    var suspendingExecutor: (suspend CommandContext.() -> Unit)? = null
    var tabCompleter: (CommandContext.() -> Collection<String>) = { emptyCompleter(this) }

    internal val subcommands = mutableListOf<CommandBuilder>()

    fun createSubcommand(
        name: String,
        permission: String? = null,
        vararg aliases: String = arrayOf(),
        builder: CommandBuilder.() -> Unit = {}
    ) {
        val subCommand = CommandBuilder(name, null, permission, usageMessage, aliases.toSet())
        builder(subCommand)
        subcommands.add(subCommand)
    }

    fun defaultComplete(ctx: CommandContext): Collection<String> {
        val sender = ctx.sender
        val completionArg = ctx.lastSubArg()

        if (sender.isOp || sender !is Player) {
            return Bukkit.getOnlinePlayers()
                .map { it.name }
                .filter { it.startsWith(completionArg, true) }
                .toList()
        }

        return sender.world.players
            .asSequence()
            .filter { sender.canSee(it) }
            .map { it.name }
            .filter { it.startsWith(completionArg, true) }
            .toList()
    }
}

fun createCommandPlugin(
    javaPlugin: SuspendingJavaPlugin,
    name: String,
    description: String = "This command has no description",
    permission: String? = null,
    usageMessage: String = "/$name",
    vararg aliases: String = arrayOf(),
    builder: CommandBuilder.() -> Unit = {},
) {
    val command = CommandBuilder(name, description, permission, usageMessage, aliases.toSet())
    builder(command)

    val simpleCommandMap = Bukkit.getServer().commandMap as SimpleCommandMap
    simpleCommandMap.register("arena", CommandHandler(javaPlugin, command))
}

fun SuspendingJavaPlugin.createCommand(name: String,
                                       description: String = "This command has no description",
                                       permission: String? = null,
                                       usageMessage: String = "/$name",
                                       vararg aliases: String = arrayOf(),
                                       builder: CommandBuilder.() -> Unit = {},) {
    createCommandPlugin(this, name, description, permission, usageMessage, *aliases, builder = builder)
}

private fun testPerm(target: CommandSender, permission: String?): Boolean {
    if (testPermSilent(target, permission)) return true
    target.sendMessage("&cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.".colorCode())
    return false
}

private fun testPermSilent(target: CommandSender, permission: String?): Boolean {
    if (permission.isNullOrEmpty()) return true
    for (p in permission.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
        if (target.hasPermission(p)) return true
    }
    return false
}

private class CommandHandler(val javaPlugin: SuspendingJavaPlugin, val commandBuilder: CommandBuilder) : Command(
    commandBuilder.name,
    commandBuilder.description,
    commandBuilder.usageMessage ?: "/${commandBuilder.name}",
    commandBuilder.aliases.toList()
) {
    override fun getPermission() = commandBuilder.permission

    init {
        permission = commandBuilder.permission
    }

    private fun getSubcommand(command: CommandBuilder, arg: String): CommandBuilder? =
        command.subcommands.firstOrNull {
            it.name.equals(
                arg,
                true
            ) || (it.aliases.isNotEmpty() && it.aliases.any { alias -> alias.equals(arg, false) })
        }

    override fun execute(sender: CommandSender, commandLabel: String?, args: Array<out String>): Boolean {
        if (!testPerm(sender, commandBuilder.permission)) return true

        try {
            var depth = 0
            var head = commandBuilder
            var flag = false

            while (depth < args.size && !flag) {
                val subcommand = getSubcommand(head, args[depth])
                if (subcommand != null) {
                    head = subcommand
                    if (!testPerm(sender, head.permission)) return true
                } else flag = true
                ++depth
            }

            val subArgs = args.copyOfRange(max(depth - 1, 0), args.size)
            val player = if (sender is Player) sender else null
            val commandContext = CommandContext(sender, player, args, subArgs)
            val suspendingExecutor = head.suspendingExecutor
            if (suspendingExecutor != null) {
                javaPlugin.launch { suspendingExecutor(commandContext) }
            } else head.executor(commandContext)
        } catch (interrupt: CommandInterrupt) {
            if (interrupt.message != null) sender.sendMessage(interrupt.message)
        } catch (exception: Exception) {
            exception.printStackTrace()
            sender.sendMessage("&cA fatal error has occurred in the course of executing this command".colorCode())
        }

        return true
    }

    override fun tabComplete(sender: CommandSender, alias: String?, args: Array<out String>): MutableList<String> {
        if (!testPermSilent(sender, commandBuilder.permission)) return mutableListOf()

        var depth = 0
        var head = commandBuilder
        var flag = false

        while (depth < args.size && !flag) {
            val subcommand = getSubcommand(head, args[depth])
            if (subcommand != null) head = subcommand
            else flag = true
            ++depth
        }

        val currentArg = args[depth - 1]
        val candidates = mutableListOf<String>()

        for (subcommand in head.subcommands) {
            if (!testPermSilent(sender, subcommand.permission)) continue
            candidates.add(subcommand.name)
            candidates.addAll(subcommand.aliases)
        }

        val subcommandCandidates = candidates.filter { it.startsWith(currentArg, true) }.toList()

        val subArgs = args.copyOfRange(depth - 1, args.size)
        val player = if (sender is Player) sender else null

        val completions = mutableListOf<String>()
        completions.addAll(subcommandCandidates)
        completions.addAll(head.tabCompleter(CommandContext(sender, player, args, subArgs)))
        completions.sortedWith(String.CASE_INSENSITIVE_ORDER)

        return completions.toMutableList()
    }
}
