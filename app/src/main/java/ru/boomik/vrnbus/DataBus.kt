package ru.boomik.vrnbus

import android.annotation.SuppressLint
import kotlin.reflect.KClass

typealias Notify<T> = (Notification<T>) -> Unit

data class Notification<T : Any>(var data: T, var eventName: String)
data class Subscriber<T : Any>(val type: KClass<T>, val notify: Notify<T>, val key: String)


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
        const val StationClick = "StationClick"
        const val BusClick = "BusClick"
        const val BusToMap = "ToMap"
        const val Update = "Update"
        const val FavoriteRoute = "FavoriteRoute"
        const val FavoriteStation = "FavoriteStation"
        const val Settings = "Settings"

        //region sendEvent
        inline fun <reified T : Any> sendEvent(key: String, obj: T?) {
            obj?.let { instance.sendEvent(key, obj)}
        }
        //endregion


        inline fun <reified T : Any> subscribe(notificationName: String, noinline sub: Notify<T>) {
            return instance.subscribe(notificationName, sub)
        }

        //endregion
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> sendEvent(key: String, obj: T) {
        val ls = subscribers[key]
        val notification = Notification(obj, key) as Notification<Any>
        val eventType = T::class
        ls?.forEach {
            if (it.type == eventType)
                it.notify(notification)
        } ?: println("NO LISTENERS FOR EVENT '$key'")
    }

    var subscribers: MutableMap<String, MutableList<Subscriber<Any>>> = mutableMapOf()


    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> subscribe(key: String, noinline sub: Notify<T>) {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        val subscriber = Subscriber(T::class, sub, key) as Subscriber<Any>
        if (!subscribers.containsKey(key)) {
            subscribers[key] = mutableListOf(subscriber)
        } else {
            val subs = subscribers[key]
            subs?.add(subscriber)
        }

    }
}