package skytils.skytilsmod.utils

import club.sk1er.vigilance.Vigilant
import com.gsquaredxc.hyskyAPI.PublicListeners
import com.gsquaredxc.hyskyAPI.eventListeners.Event
import com.gsquaredxc.hyskyAPI.eventListeners.EventRegister
import kotlin.reflect.KProperty

object ConfigUtil {

    @JvmStatic
    fun connectConfigToListener(
        v: Vigilant,
        o: Any,
        e: Class<out Event?>,
        p: KProperty<Boolean>,
        b: Boolean,
        s: String
    ){
        EventRegister.register(
            o.javaClass.getMethod("onTitlePacket", e),o
        )
        if (b) {
            PublicListeners.listenerHashMap[e]?.deregister(s)
        }
        v.registerListener(p){
            if (it) {
                PublicListeners.listenerHashMap[e]?.reregister(s)
            }else {
                PublicListeners.listenerHashMap[e]?.deregister(s)
            }
        }
    }
}