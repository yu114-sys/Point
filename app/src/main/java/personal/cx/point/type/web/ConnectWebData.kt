package personal.cx.point.type.web

data class ConnectWebData (
    //message type
    val type: String,
    //web server port
    val port: Int,
    //key
    val key: String,
    //information
    val information: String
)