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
package skytils.skytilsmod.gui

import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RainbowColorConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.font.DefaultFonts
import gg.essential.vigilance.VigilanceConfig
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.gui.keyshortcuts.KeyShortcutsGui
import skytils.skytilsmod.utils.openGUI
import java.awt.Desktop
import java.net.URI

class OptionsGui : WindowScreen(newGuiScale = 2) {

    private val skytilsText: UIText = UIText("Skytils", shadow = false).childOf(window).constrain {
        x = CenterConstraint()
        y = RelativeConstraint(0.25f) - 75.pixels()
        textScale = 12.5.pixels()
        fontProvider = DefaultFonts.ELEMENTA_MINECRAFT_FONT_RENDERER
    }

    init {
        // TODO figure out the math to make everything fit on screen even when it's small
        SimpleButton("Config").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.25f) + 100.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            Skytils.config.openGUI()
        }
        SimpleButton("Edit Aliases").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(CommandAliasesGui())
        }
        SimpleButton("Edit Locations").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(LocationEditGui())
        }
        SimpleButton("Edit Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(KeyShortcutsGui())
        }
        SimpleButton("Edit Vigilance").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            VigilanceConfig.openGUI()
        }
        SimpleButton("Discord").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() - 3 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://discord.gg/skytils"))
            }
        }
        SimpleButton("GitHub").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() * 2 - 6 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://github.com/Skytils/SkytilsMod"))
            }
        }
        animate()
    }

    fun animate() {
        skytilsText.animate {
            setColorAnimation(Animations.IN_OUT_SIN, 1f, RainbowColorConstraint())
                .onComplete {
                    animate()
                }
        }
    }
}