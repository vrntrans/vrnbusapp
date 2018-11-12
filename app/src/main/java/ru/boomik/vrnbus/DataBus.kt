package ru.boomik.vrnbus

import android.annotation.SuppressLint
import java.security.InvalidParameterException

typealias Subscriber<T> = (Notification<T>) -> Unit
data class Notification<T : Any?>(var data: T?, var eventName: String)

class DataBus {


    //region Instance
    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE: DataBus = DataBus()
    }


    //endregion

    @Suppress("UNCHECKED_CAST")
    companion object {
        val instance: DataBus by lazy { Holder.INSTANCE }

        const val Traffic = "Traffic"
        const val Referer = "Referer"
        const val StationClick = "StationClick"
        const val BusClick = "BusClick"
        const val BusToMap = "ToMap"
        const val Update = "Update"
        const val FavoriteRoute = "FavoriteRoute"
        const val FavoriteStation = "FavoriteStation"

        //region sendEvent
        fun <T> sendEvent(key: String, obj: T) {
            instance.sendEvent(key, obj)
        }
        //endregion

/*
        //region subscribe
        fun <T> subscribe(key: String, listener: (T) -> Unit) {
            var lst: (T) -> Unit = listener

            instance.getListeners<T>(key).add(listener)
        }*/


        inline fun <reified T : Any?> subscribe(notificationName: String, noinline sub: Subscriber<T?>) {
            return instance.subscribe(notificationName, sub)
        }

        //endregion
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> sendEvent(key: String, obj: T?) {
        synchronized(subscribers) {
            val ls = subscribers[key]
            val notification = Notification(obj, key) as Notification<Any?>
            ls?.forEach {
                it(notification)
            } ?: println("NO LISTENERS FOR EVENT '$key'")
        }
    }


    //region Listeners
    var subscribers : MutableMap<String, List<Subscriber<Any?>>> = mutableMapOf()

    init {
    }
    //endregion


    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any?> subscribe(key: String, noinline sub: Subscriber<T?>) {
        (sub as? Subscriber<Any?>)?.let { subscriber ->
            if (key.isBlank()) throw IllegalStateException("Key not be empty")
            if (!subscribers.containsKey(key)) {
                subscribers[key] = mutableListOf(subscriber)
            } else {
                val subs= subscribers[key] as? MutableList<Subscriber<Any?>>
                subs?.add(subscriber)
            }
        }
    }

    //region InternalMethods
    @Suppress("UNCHECKED_CAST")
    private fun <T> getListeners(key: String): MutableList<Subscriber<T>> {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        if (!subscribers.containsKey(key)) throw NoSuchFieldException("No listeners for this $key found")
        return subscribers[key] as? MutableList<Subscriber<T>>
                ?: throw InvalidParameterException("Incorrect value type")
    }
    //endregion

}