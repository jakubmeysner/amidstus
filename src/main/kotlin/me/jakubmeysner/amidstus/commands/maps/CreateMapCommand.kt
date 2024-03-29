package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Map
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.regex.Pattern

class CreateMapCommand(val plugin: AmidstUs) : TabExecutor, Named {
    override val name = "createmap"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size != 1) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Usage: /createmap <map name>").color(ChatColor.RED).create()
            )
        } else if (!Pattern.compile("^[a-z0-9_]+$").matcher(args[0]).matches()) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Name may only include lowercase letters, numbers and underscores!")
                    .color(ChatColor.RED).create()
            )
        } else if (this.plugin.maps.find { it.name == args[0] } != null) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Name must be unique!").color(ChatColor.RED).create()
            )
        } else {
            val map = Map(args[0])
            plugin.maps.add(map)
            sender.spigot().sendMessage(
                *ComponentBuilder("Created new map \"${map.displayName}\".").color(ChatColor.GREEN).create()
            )
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        return listOf()
    }
}
