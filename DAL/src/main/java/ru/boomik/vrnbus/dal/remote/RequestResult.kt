package ru.boomik.vrnbus.dal.remote

interface IRequestResult {
    val message: String?
    val status: RequestStatus
}


data class RequestResult(override var status: RequestStatus, override val message: String? = null) : IRequestResult
data class RequestResultWithData<T>(var data: T? = null, override var status: RequestStatus, override val message: String? = null) : IRequestResult
data class RequestResultWithDataAndError<T, TError>(var data: T, var error: TError, override var status: RequestStatus, override val message: String? = null) : IRequestResult

enum class RequestStatus(val code: Int) {
    Unknown(0),
    Ok(200),
    NotModified(304),
    BadRequest(400),
    Unauthorized(401),
    Forbidden(403),
    NotFound(404),
    NotAcceptable(406),
    Unprocessable(422),
    InternalServerError(500),
    ServiceUnavailable(503),
    Canceled(1001),
    InvalidRequest(1002),
    SerializationError(1003),
    NoInternet(1004);

    companion object {
        private val map = values().associateBy(RequestStatus::code)

        fun fromCode(code: Int): RequestStatus {
            return map.getValue(code)
        }
    }
}