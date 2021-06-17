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
package skytils.skytilsmod.features.impl.mining

import com.gsquaredxc.hyskyAPI.annotations.EventListener
import com.gsquaredxc.hyskyAPI.state.PlayerStates.LocationState
import com.gsquaredxc.hyskyAPI.state.location.ServerTypes
import net.minecraft.block.BlockCarpet
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.RenderBlockInWorldEvent
import skytils.skytilsmod.utils.Utils

class DarkModeMist {
    @EventListener(id="STRENDERDarkMode")
    fun onGetBlockModel(event: RenderBlockInWorldEvent) {
        if (!Utils.inSkyblock || !Skytils.config.darkModeMist) return
        if (LocationState.serverType == ServerTypes.DwarvenMines && event.state != null && event.pos != null) {
            val state = event.state
            if ((event.pos as BlockPos).y <= 76) {
                if ((state as IBlockState).block === Blocks.stained_glass &&
                    (state as IBlockState).getValue(BlockStainedGlass.COLOR) == EnumDyeColor.WHITE
                ) {
                    event.state = state.withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY)
                }
                if ((state as IBlockState).block === Blocks.carpet && (state as IBlockState).getValue(BlockCarpet.COLOR) == EnumDyeColor.WHITE) {
                    event.state = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY)
                }
            }
        }
    }
}