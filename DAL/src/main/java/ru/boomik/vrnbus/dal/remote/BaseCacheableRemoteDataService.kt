package ru.boomik.vrnbus.dal.remote

import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0


open class BaseCacheableRemoteDataService<T : Any>(override val serviceClass: T, cachePath: String, val cacheTime: Long) : BaseRemoteDataService<T>(serviceClass) {

    val cache = LocalFileCache(cachePath)

    suspend inline fun <T, reified TR : Any> makeRequestWithCacheAndConverter(loadingFun: KSuspendFunction0<T>, converterFun: KFunction1<T?, TR>, cacheName: String, cacheTime: Long = 0L, invalidateCache: Boolean = false, isList: Boolean = false, logFunc: KFunction1<String, Unit>? = null): RequestResultWithData<TR?> {

        var tmpCacheTime = cacheTime
        if (tmpCacheTime == 0L) tmpCacheTime = this.cacheTime
        var cachedResult: TR? = null
        if (!invalidateCache)
            try {
                cachedResult = cache.get<TR>(cacheName, logFunc)
            } catch (e: Throwable) {
                var ee = e
            }

        if (!invalidateCache && cachedResult != null) {
            logFunc?.invoke(cacheName+" Return from cache")
            return RequestResultWithData(cachedResult, RequestStatus.Ok)
        }

        return try {
            val networkResult = invokeWithRetry(loadingFun)
            val data = converterFun(networkResult)

            if (tmpCacheTime > 0) cache.write(cacheName, data, tmpCacheTime, logFunc)

            logFunc?.invoke(cacheName+" Return from network")
            RequestResultWithData(data, RequestStatus.Ok)

        } catch (e: Throwable) {
            RequestResultWithData(null, statusFromException(e), e.localizedMessage)
        }
    }


    protected inline fun <reified TR : Any> makeRequestFromCache(cacheName: String): RequestResultWithData<TR?> {
        val cachedResult = cache.get<TR>(cacheName)
        return if (cachedResult != null)
            RequestResultWithData(cachedResult, RequestStatus.Ok)
        else RequestResultWithData(null, RequestStatus.NotFound)
    }
}