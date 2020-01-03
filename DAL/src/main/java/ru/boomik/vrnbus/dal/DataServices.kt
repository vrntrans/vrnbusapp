package ru.boomik.vrnbus.dal

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.remote.services.CoddDataService

object DataServices {
    lateinit var CoddDataService: CoddDataService

    fun init(cachePath: String) {
        val client = ApiClient()
        client.createDefaultAdapter()

        val service = client.createService(ICoddApi::class.java)
        CoddDataService = CoddDataService(service)

    }
}