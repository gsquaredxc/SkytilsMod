/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package skytils.skytilsmod.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.SendChatMessageEvent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapted from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * @author Moulberry
 */
object SBInfo {

    private val timePattern = ".+(am|pm)".toRegex()

    var time = ""

    @JvmField
    var lastOpenContainerName: String? = null
    private var joinedWorld: Long = -1

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!Utils.inSkyblock) return
        if (event.gui is GuiChest) {
            val chest = event.gui as GuiChest
            val container = chest.inventorySlots as ContainerChest
            val containerName = container.lowerChestInventory.displayName.unformattedText
            lastOpenContainerName = containerName
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        joinedWorld = System.currentTimeMillis()
        lastOpenContainerName = null
    }


    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getMinecraft().thePlayer == null || Minecraft.getMinecraft().theWorld == null || !Utils.inSkyblock) return
        try {
            val scoreboard = Minecraft.getMinecraft().thePlayer.worldScoreboard
            val sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1) //§707/14/20
            val scores: List<Score> = ArrayList(scoreboard.getSortedScores(sidebarObjective))
            val lines: MutableList<String> = ArrayList()
            for (i in scores.indices.reversed()) {
                val score = scores[i]
                val scorePlayerTeamOne = scoreboard.getPlayersTeam(score.playerName)
                var line = ScorePlayerTeam.formatPlayerName(scorePlayerTeamOne, score.playerName)
                line = line.stripControlCodes()
                lines.add(line)
            }
            if (lines.size >= 5) {
                //§74:40am
                val matcher = timePattern.find(lines[3])
                if (matcher != null) {
                    time = matcher.groupValues[0].stripControlCodes().trim { it <= ' ' }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}