package ru.boomik.vrnbus.dal.remote

import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import ru.boomik.vrnbus.dal.Consts.RetryCount
import ru.boomik.vrnbus.dal.Consts.RetryDelayMilliseconds
import kotlin.reflect.KFunction1
import kotlin.reflect.KSuspendFunction0

open class BaseRemoteDataService<T : Any>(open val serviceClass: T) {

    val converters = DataConverter()

    suspend fun <T> makeRequest(loadingFun: KSuspendFunction0<T>): RequestResultWithData<T> {
        return try {
            val result = invokeWithRetry(loadingFun)
            RequestResultWithData(result, RequestStatus.Ok)

        } catch (e: Throwable) {
            RequestResultWithData(null, statusFromException(e), e.localizedMessage)
        }
    }

    suspend fun <T, TR> makeRequestWithConverter(loadingFun: KSuspendFunction0<T>, converterFun: KFunction1<T?, TR>): RequestResultWithData<TR> {
        return try {
            val result = invokeWithRetry(loadingFun)
            val data = converterFun(result)
            RequestResultWithData(data, RequestStatus.Ok)

        } catch (e: Throwable) {
            RequestResultWithData(null, statusFromException(e), e.localizedMessage)
        }
    }

    internal fun statusFromException(e: Throwable): RequestStatus {
        if (e is JsonSyntaxException) return RequestStatus.SerializationError
        if (e is HttpException) return requestStatusFromHttpCode(e.code())
        return RequestStatus.Unknown
    }

    private fun requestStatusFromHttpCode(code: Int): RequestStatus {
        return try {
            RequestStatus.fromCode(code)
        } catch (e: Throwable) {
            RequestStatus.Unknown
        }
    }

    internal suspend fun <T1> invokeWithRetry(loadingFun: KSuspendFunction0<T1>): T1? {
        var exception: Throwable?
        var result: T1? = null
        var retryRemained = RetryCount
        do {
            try {
                result = loadingFun()
                exception = null
            } catch (e: Throwable) {
                exception = e
                delay(RetryDelayMilliseconds)
            }
            retryRemained--
        } while (exception != null && retryRemained > 0)

        if (exception != null)
            throw exception

        return result
    }

    private suspend fun <T1> invokeWithRetry2(loadingFun: () -> T1?): T1? {
        var exception: Throwable?
        var result: T1? = null
        var retryRemained = RetryCount
        do {
            try {
                result = loadingFun()
                exception = null
            } catch (e: Throwable) {
                exception = e
                delay(RetryDelayMilliseconds)
            }
            retryRemained--
        } while (exception != null && retryRemained > 0)

        if (exception != null)
            throw exception

        return result
    }

}