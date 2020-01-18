package ru.boomik.vrnbus.dal

import ru.boomik.vrnbus.dal.api.ICoddApi
import ru.boomik.vrnbus.dal.remote.services.CoddDataService
import ru.boomik.vrnbus.dal.remote.services.CoddPersistentDataService

object DataServices {
    lateinit var CoddDataService: CoddDataService
    lateinit var CoddPersistentDataService: CoddPersistentDataService

    fun init(cachePath: String) {
        val client = ApiClient()

        val service = client.createService(ICoddApi::class.java)
        CoddDataService = CoddDataService(service)
        CoddPersistentDataService = CoddPersistentDataService(service, cachePath, 3 * 24 * 60 * 60 * 1000/*3 days*/)
    }
}