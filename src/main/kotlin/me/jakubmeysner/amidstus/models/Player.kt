package me.jakubmeysner.amidstus.models

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import org.bukkit.ChatColor as BukkitChatColor

class Player(val bukkit: Player, var pending: Boolean = false) {
    companion object {
        val LeaveGameItemStack = ItemStack(Material.OAK_DOOR).apply {
            itemMeta = itemMeta?.apply { setDisplayName("${BukkitChatColor.RED}Leave game") }
        }

        val ImpostorSwordItemStack = ItemStack(Material.DIAMOND_SWORD).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${BukkitChatColor.DARK_RED}Impostor sword")
                isUnbreakable = true
            }
        }

        val ChangeMapOptionsItemStack = ItemStack(Material.REDSTONE).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${BukkitChatColor.BLUE}Change Game Options")
            }
        }

        val StartGameItemStack = ItemStack(Material.MINECART).apply {
            itemMeta = itemMeta?.apply {
                setDisplayName("${BukkitChatColor.GREEN}Start Game")
            }
        }

        fun playPublicGame(plugin: AmidstUs, map: Map?, bukkitPlayer: Player) {
            val games = plugin.games.filter {
                (map == null || it.map == map) &&
                    it.players.size < it.map.maxNumberOfPlayers &&
                    it.status == Game.Status.NOT_STARTED
            }.sortedByDescending { it.players.size }

            val game = if (games.isEmpty()) {
                val game = Game(
                    map ?: plugin.maps.filter { it.playable }.shuffled()[0],
                    Game.Type.PUBLIC
                )
                plugin.games.add(game)
                game
            } else {
                games[0]
            }

            val player = Player(bukkitPlayer)
            player.joinGame(game, plugin)

            bukkitPlayer.spigot().sendMessage(
                *ComponentBuilder("You are now playing on ${game.map.displayName}!")
                    .color(ChatColor.GREEN).create()
            )

            if (game.players.size == game.map.autoStartNumberOfPlayers) {
                if (game.autoStartTask == null) {
                    game.autoStartTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                        if (game.type == Game.Type.PRIVATE) {
                            game.autoStartTask?.cancel()
                            game.autoStartTask = null
                        } else if (game.players.size < game.map.autoStartNumberOfPlayers) {
                            game.autoStartTask?.cancel()
                            game.autoStartTask = null
                            game.autoStartSecondsLeft = null
                        } else {
                            when (game.autoStartSecondsLeft) {
                                null -> game.autoStartSecondsLeft = 15
                                in 1..15 -> game.autoStartSecondsLeft = game.autoStartSecondsLeft!! - 1
                            }

                            if (game.autoStartSecondsLeft == 0) {
                                game.autoStartTask?.cancel()
                                game.autoStartTask = null
                                game.autoStartSecondsLeft = null
                                game.start(plugin)
                            } else {
                                for (itPlayer in game.players) {
                                    itPlayer.bukkit.sendTitle(
                                        "${BukkitChatColor.YELLOW}${BukkitChatColor.BOLD}${game.autoStartSecondsLeft}",
                                        null,
                                        0,
                                        20,
                                        0
                                    )
                                }
                            }
                        }
                    }, 0, 20)
                }
            }
        }
    }

    var promoted = false
    var impostor = false
    var dead = false
    var fakeEntityId: Int? = null
    var killCooldownActive = false
    var killCooldownSecondsLeft: Int? = null
    var killCooldownTask: BukkitTask? = null

    fun joinGame(game: Game, plugin: AmidstUs) {
        if (!game.players.contains(this)) {
            game.players.add(this)
        }

        bukkit.teleport(game.map.preGameLocation ?: return)
        if (game.map.time != null) bukkit.setPlayerTime(game.map.time!!.toLong(), false)

        for (player in plugin.server.onlinePlayers) {
            if (game.players.any { it.bukkit == player }) {
                bukkit.showPlayer(plugin, player)
                player.showPlayer(plugin, bukkit)
            } else {
                bukkit.hidePlayer(plugin, player)
                player.hidePlayer(plugin, bukkit)
            }
        }

        bukkit.inventory.clear()
        bukkit.inventory.heldItemSlot = 0
        bukkit.inventory.setItem(8, LeaveGameItemStack)
    }

    fun leaveGame(game: Game, plugin: AmidstUs) {
        for (player in game.players) {
            if (player.fakeEntityId != null) {
                plugin.protocolManager.sendServerPacket(bukkit,
                    PacketContainer(PacketType.Play.Server.ENTITY_DESTROY).apply {
                        integers.write(0, player.fakeEntityId)
                    }
                )
            }
        }

        game.players.remove(this)

        if (game.players.size == 0) {
            plugin.games.remove(game)
        } else if (game.type == Game.Type.PRIVATE && game.players.none { it.promoted }) {
            val randomPlayer = game.players.random()
            randomPlayer.promoted = true
            randomPlayer.bukkit.inventory.setItem(1, ChangeMapOptionsItemStack)
            randomPlayer.bukkit.inventory.setItem(0, StartGameItemStack)

            randomPlayer.bukkit.spigot().sendMessage(
                *ComponentBuilder("Because all promoted players have left the game, you have been randomly promoted.")
                    .color(ChatColor.GREEN).create()
            )
        }

        bukkit.inventory.clear()

        if (pending) return

        if (
            game.players.none { !it.dead && it.impostor } ||
            game.players.count { !it.dead && it.impostor } >= game.players.count { !it.dead && !it.impostor }
        ) {
            game.end(plugin)
        }

        bukkit.teleport(game.map.postGameLocation ?: return)
        bukkit.resetPlayerTime()

        for (player in plugin.server.onlinePlayers) {
            if (plugin.games.any { it.players.any { it.bukkit == player } }) {
                bukkit.hidePlayer(plugin, player)
                player.hidePlayer(plugin, bukkit)
            } else {
                bukkit.showPlayer(plugin, player)
                player.showPlayer(plugin, bukkit)
            }
        }

        plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
            ?.removeEntry(bukkit.name)
    }
}
