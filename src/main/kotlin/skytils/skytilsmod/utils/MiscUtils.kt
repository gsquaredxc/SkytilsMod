package skytils.skytilsmod.utils

import net.minecraft.util.ChatComponentText
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.DataFetcher

object MiscUtils {
    fun checkForItem(array: LinkedHashMap<String, *>, unformatted: String): Any? {
        if (array.isEmpty()) {
            mc.thePlayer.addChatMessage(ChatComponentText("§cSkytils did not load any solutions."))
            DataFetcher.reloadData()
            return null
        }
        return array.getOrDefault(array.keys.find { s: String ->
            unformatted.contains(s)
        }, null)
    }
}
