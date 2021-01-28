package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Entity

class CommandSetMapPostGameLocation(val plugin: AmidstUs) : TabExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (sender !is Entity) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by entities!").color(ChatColor.RED).create()
      )
    } else if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /setmappostgamelocation <name>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        map.postGameLocation = sender.location
        sender.spigot().sendMessage(
          *ComponentBuilder("Changed the post game location of map \"${map.name}\" to your location.")
            .color(ChatColor.GREEN).create()
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