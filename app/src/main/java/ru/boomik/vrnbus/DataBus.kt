package ru.boomik.vrnbus

import kotlin.reflect.KClass

typealias Notify<T> = (Notification<T>) -> Unit

data class Notification<T : Any>(var data: T, var eventName: String)
data class Subscriber<T : Any>(val type: KClass<T>, val notify: Notify<T>, val key: String)


object DataBus {

    const val Traffic = "Traffic"
    const val StationClick = "StationClick"
    const val BusClick = "BusClick"
    const val BusToMap = "ToMap"
    const val Update = "Update"
    const val FavoriteRoute = "FavoriteRoute"
    const val ClickRoute = "ClickRoute"
    const val LongClickRoute = "LongClickRoute"
    const val FavoriteStation = "FavoriteStation"
    const val Settings = "Settings"
    const val ResetRoutes = "ResetRoutes"
    const val AddRoutes = "AddRoutes"


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

    inline fun <reified T : Any> unsubscribe (key: String) {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        if (!subscribers.containsKey(key)) return
        val subs = subscribers[key] ?: return
        val forUnsubscribe = subs.filter { it.type == T::class }
        if (!forUnsubscribe.any()) return
        subs.removeAll(forUnsubscribe)
        if (!subs.any()) subscribers.remove(key)
    }

    fun unsubscribeKey (key: String) {
        if (key.isBlank()) throw IllegalStateException("Key not be empty")
        if (!subscribers.containsKey(key)) return
        subscribers.remove(key)
    }

    fun unsubscribeAll() {
        subscribers.clear()
    }
}