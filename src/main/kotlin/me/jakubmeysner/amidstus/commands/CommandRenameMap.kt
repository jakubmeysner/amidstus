package me.jakubmeysner.amidstus.commands

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

class CommandRenameMap(val plugin: AmidstUs) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /renamemap <name> <new name>").color(ChatColor.RED).create()
      )
    } else if (!Pattern.compile("^[a-z]+$").matcher(args[1]).matches()) {
      sender.spigot().sendMessage(
        *ComponentBuilder("New name may only include lowercase letters!").color(ChatColor.RED).create()
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
          *ComponentBuilder("Changed name from \"${args[0]}\" to \"${map.name}\".").color(ChatColor.GREEN).create()
        )
      }
    }

    return true
  }
}
