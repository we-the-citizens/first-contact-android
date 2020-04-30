package ro.wethecitizens.firstcontact.server

sealed class HttpCode(val code: Int) {
    object OK : HttpCode(200)
    object BAD_REQUEST : HttpCode(400)
    object UNAUTHORIZED : HttpCode(401)
    object SERVER_ERROR : HttpCode(500)
    object GATEWAY_ERROR : HttpCode(502)
    class UNKNOWN_ERROR(code: Int) : HttpCode(code)

    companion object {
        fun getType(code: Int): HttpCode = when (code) {
            OK.code -> OK
            BAD_REQUEST.code -> BAD_REQUEST
            UNAUTHORIZED.code -> UNAUTHORIZED
            SERVER_ERROR.code -> SERVER_ERROR
            GATEWAY_ERROR.code -> GATEWAY_ERROR
            else -> UNKNOWN_ERROR(code)
        }
    }
}