package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import java.util.regex.Pattern

class RenameMapCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "renamemap"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /renamemap <map name> <new map name>").color(ChatColor.RED).create()
      )
    } else if (!Pattern.compile("^[a-z0-9_]+$").matcher(args[1]).matches()) {
      sender.spigot().sendMessage(
        *ComponentBuilder("New name may only include lowercase letters, numbers and underscores!")
          .color(ChatColor.RED).create()
      )
    } else if (this.plugin.maps.find { it.name == args[1] } != null) {
      sender.spigot().sendMessage(
        *ComponentBuilder("New name must be unique!").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        map.name = args[1]
        sender.spigot().sendMessage(
          *ComponentBuilder("Changed name of map ${map.displayName} to ${map.name}.").color(ChatColor.GREEN).create()
        )
      }
    }

    return true
  }

  override fun onTabComplete(
    sender: CommandSender,
    command: Command,
    alias: String,
    args: Array<out String>
  ): List<String> {
    return when (args.size) {
      1 -> plugin.maps.map { it.name }.filter { it.startsWith(args[0]) }
      else -> listOf()
    }
  }
}
