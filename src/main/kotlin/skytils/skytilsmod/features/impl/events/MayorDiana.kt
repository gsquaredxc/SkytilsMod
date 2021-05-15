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

package skytils.skytilsmod.features.impl.events

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.mixins.AccessorMinecraft
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color

class MayorDiana {

    private val gaiaConstructHits = HashMap<EntityIronGolem, Int>()

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || !Skytils.config.trackGaiaHits) return
        val packet = event.packet
        if (packet is S29PacketSoundEffect) {
            if (packet.volume == 0f) return
            if (packet.volume == 0.8f && packet.soundName == "random.anvil_land") {
                val pos = BlockPos(packet.x, packet.y, packet.z)
                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity is EntityIronGolem && entity.isEntityAlive && entity.getDistanceSq(pos) <= 2 * 2) {
                        gaiaConstructHits.compute(entity) { _: EntityIronGolem, i: Int? -> if (i == null) 1 else i + 1 }
                        break
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.trackGaiaHits) return
        if (event.phase == TickEvent.Phase.START) for (golem in gaiaConstructHits.keys) {
            if (golem.hurtTime == 10) {
                gaiaConstructHits[golem] = 0
            }
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderLivingEvent.Post<EntityIronGolem>) {
        if (!Utils.inSkyblock || !Skytils.config.trackGaiaHits) return
        val golem: EntityIronGolem = event.entity as EntityIronGolem
        if (gaiaConstructHits.containsKey(golem)) {
                val percentageHp = golem.health / golem.maxHealth
                val neededHits = when {
                    percentageHp <= (1f / 3f) -> 7
                    percentageHp <= (2f / 3f) -> 6
                    else -> 5
                }
                val hits = gaiaConstructHits.getOrDefault(golem, 0)
                GlStateManager.disableDepth()
                RenderUtil.draw3DString(
                    Vec3(golem.posX, golem.posY + 2, golem.posZ),
                    "Hits: $hits / $neededHits",
                    if (hits < neededHits) Color.RED else Color.GREEN,
                    (mc as AccessorMinecraft).timer.renderPartialTicks
                )
                GlStateManager.enableDepth()
            }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        gaiaConstructHits.clear()
    }
}