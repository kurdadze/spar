package ge.mark.sparemployee.models

data class Worker(
    var controller_code: String,
    var pin: String = "",
    var datetime: String = "",
    var picture: String = "",
    var hash: String = "",
    var photo_name: String = ""
)
