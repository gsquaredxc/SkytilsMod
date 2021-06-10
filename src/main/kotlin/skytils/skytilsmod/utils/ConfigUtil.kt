package skytils.skytilsmod.utils

import gg.essential.vigilance.Vigilant
import com.gsquaredxc.hyskyAPI.PublicListeners
import com.gsquaredxc.hyskyAPI.StateRegister.StateRegister
import com.gsquaredxc.hyskyAPI.eventListeners.Event
import com.gsquaredxc.hyskyAPI.eventListeners.EventCallback
import com.gsquaredxc.hyskyAPI.eventListeners.EventRegister
import com.gsquaredxc.hyskyAPI.eventListeners.EventRegister.privateMethodToHandle
import java.lang.reflect.Modifier
import kotlin.reflect.KProperty

object ConfigUtil {

    @JvmStatic
    fun connectConfigToListener(
        v: Vigilant,
        m: String,
        o: Any,
        e: Class<out Event?>,
        p: KProperty<Boolean>,
        b: Boolean,
        s: String
    ) {
        EventRegister.register(o.javaClass.getMethod(m, e), o)
        if (b) {
            PublicListeners.listenerHashMap[e]?.deregister(s)
        }
        v.registerListener(p) {
            if (it) {
                PublicListeners.listenerHashMap[e]?.reregister(s)
            } else {
                PublicListeners.listenerHashMap[e]?.deregister(s)
            }
        }
    }

    //the state knows everything
    @JvmStatic
    fun connectConfigToState(
        v: Vigilant,
        m: String,
        o: Any,
        e: Class<out Event?>,
        state: StateRegister,
        p: KProperty<Boolean>,
        b: Boolean,
        s: String
    ) {
        val listener = PublicListeners.listenerHashMap[e]
        val callback = getCallback(m, o, e, s)
        if (b) {
            state.queueCallbackIfNegative(callback, listener!!)
        }
        v.registerListener(p) {
            if (it) {
                state.queueCallbackIfNegative(callback, listener!!)
            } else {
                state.dequeueCallback(s)
            }
        }
    }

    //isnt dystopian i swear
    @JvmStatic
    fun registerWithState(
        m: String,
        o: Any,
        e: Class<out Event>,
        state: StateRegister,
        s: String
    ) {
        val listener = PublicListeners.listenerHashMap[e]
        val callback = getCallback(m, o, e, s)
        state.queueCallbackIfNegative(callback, listener!!)
    }

    @JvmStatic
    fun getCallback(
        m: String,
        o: Any,
        e: Class<out Event>,
        s: String
    ): EventCallback {
        val method = o.javaClass.getMethod(m, e)
        var mh = privateMethodToHandle(method)
        if (!Modifier.isStatic(method.modifiers)) {
            mh = mh.bindTo(o)
        }
        return EventCallback(mh, s)
    }
}