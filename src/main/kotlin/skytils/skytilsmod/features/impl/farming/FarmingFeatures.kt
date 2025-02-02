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
package skytils.skytilsmod.features.impl.farming

import com.gsquaredxc.hyskyAPI.annotations.EventListener
import com.gsquaredxc.hyskyAPI.events.misc.TickStartEvent
import com.gsquaredxc.hyskyAPI.events.packets.TitleInEvent
import com.gsquaredxc.hyskyAPI.utils.SafeMessageSender.SAFE_MESSAGE_SENDER
import net.minecraft.block.Block
import net.minecraft.client.gui.GuiChat
import net.minecraft.init.Blocks
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemHoe
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.DamageBlockEvent
import skytils.skytilsmod.utils.MiscUtils.checkForItem
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes

class FarmingFeatures {
    private var lastNotifyBreakTime: Long = 0

    @SubscribeEvent
    fun onAttemptBreak(event: DamageBlockEvent) {
        val p = mc.thePlayer
        if (!Utils.inSkyblock || p == null || mc.theWorld == null) return
        val heldItem = p.heldItem
        val block = mc.theWorld.getBlockState(event.pos).block
        if (Skytils.config.preventBreakingFarms && heldItem != null) {
            if ((heldItem.item is ItemHoe || heldItem.item is ItemAxe) && farmBlocks.contains(block)) {
                event.isCanceled = true
                if (System.currentTimeMillis() - lastNotifyBreakTime > 10000) {
                    lastNotifyBreakTime = System.currentTimeMillis()
                    p.playSound("note.bass", 1f, 0.5f)
                    val notif =
                        ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has prevented you from breaking that block!")
                    p.addChatMessage(notif)
                }
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

        val formatted = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()

        if (Skytils.config.acceptTrapperTask) {
            if (formatted.contains("§a§l[YES]")) {
                val listOfSiblings = event.message.siblings
                acceptTrapperCommand =
                    listOfSiblings.find { it.unformattedText.contains("[YES]") }?.chatStyle?.chatClickEvent?.value ?: ""
                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.LIGHT_PURPLE.toString() + "Skytils: Open chat then click anywhere on screen to accept task"))
            }
        }
        if (Skytils.config.trapperPing) {
            if (unformatted.startsWith("[NPC] Trevor The Trapper: You can find your")) {
                trapperStart = System.currentTimeMillis().toDouble()
                animalFound = false
            } else if (unformatted.startsWith("Return to the Trapper soon to get a new animal to hunt!")) {
                if (trapperStart > 0 && System.currentTimeMillis() - trapperStart > 60000) { //1 minute cooldown
                    Utils.playLoudSound("note.pling", 1.0)
                    mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.LIGHT_PURPLE.toString() + "Skytils: Trapper cooldown has already expired!"))
                    trapperStart = -1.0
                }
                animalFound = true
            }
        }

        if (Skytils.config.hungryHikerSolver && formatted.startsWith("§e[NPC] Hungry Hiker§f: ")) {
            val solution = checkForItem(hungerHikerItems,unformatted)
            TickTask(4) {
                if (solution != null) {
                    mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "The Hiker needs: " + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.BOLD + solution + EnumChatFormatting.GREEN + "!"))
                } else {
                    if (unformatted.contains("I asked for") || unformatted.contains("The food I want")) {
                        println("Missing Hiker item: $unformatted")
                        mc.thePlayer.addChatMessage(
                            ChatComponentText(
                                String.format(
                                    "§cSkytils couldn't determine the Hiker item. There were %s solutions loaded.",
                                    hungerHikerItems.size
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    @EventListener(id="STFarmingDrop")
    fun onTitlePacket(event: TitleInEvent): Boolean {
        if (event.message != null) {
            val unformatted = event.message.unformattedText.stripControlCodes()
            if (unformatted.contains("DROP!")) {
                return true
            }
        }
        return false
    }

    //TODO: create timer system
    @EventListener(id="STFarmingTrapper")
    fun onTick(event: TickStartEvent) {
        if (trapperStart > 0 && mc.thePlayer != null) {
            if (System.currentTimeMillis() - trapperStart > 60000 && animalFound) { //1 minute cooldown
                trapperStart = -1.0
                mc.thePlayer.addChatMessage(ChatComponentText("§dSkytils: Trapper cooldown has now expired!"))
                for (i in 0..4) {
                    SoundQueue.addToQueue(SoundQueue.QueuedSound("note.pling", 1f, ticks = i * 4, isLoud = true))
                }
            }
        }
        return
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        trapperStart = -1.0
    }

    @SubscribeEvent
    fun onMouseInputPost(event: GuiScreenEvent.MouseInputEvent.Post) {
        if (!Utils.inSkyblock) return
        if (Mouse.getEventButton() == 0 && event.gui is GuiChat) {
            if (Skytils.config.acceptTrapperTask && acceptTrapperCommand.isNotBlank()) {
                SAFE_MESSAGE_SENDER.queueMessage(acceptTrapperCommand)
                acceptTrapperCommand = ""
            }
        }
    }

    companion object {
        var hungerHikerItems = LinkedHashMap<String, String>()
        var trapperStart = -1.0
        var animalFound = false
        var acceptTrapperCommand = ""
        val farmBlocks = setOf<Block>(
            Blocks.dirt,
            Blocks.farmland,
            Blocks.carpet,
            Blocks.glowstone,
            Blocks.sea_lantern,
            Blocks.soul_sand,
            Blocks.waterlily,
            Blocks.standing_sign,
            Blocks.wall_sign,
            Blocks.wooden_slab,
            Blocks.double_wooden_slab,
            Blocks.oak_fence,
            Blocks.dark_oak_fence,
            Blocks.birch_fence,
            Blocks.spruce_fence,
            Blocks.acacia_fence,
            Blocks.jungle_fence,
            Blocks.oak_fence_gate,
            Blocks.acacia_fence_gate,
            Blocks.birch_fence_gate,
            Blocks.jungle_fence_gate,
            Blocks.spruce_fence_gate,
            Blocks.dark_oak_fence_gate,
            Blocks.glass,
            Blocks.glass_pane,
            Blocks.stained_glass,
            Blocks.stained_glass_pane
        )
    }
}