package com.shaffinimam.i212963

data class User(
    var id: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val followers: String = "0",
    val following: String = "0",
    var picture: String = ""
)

