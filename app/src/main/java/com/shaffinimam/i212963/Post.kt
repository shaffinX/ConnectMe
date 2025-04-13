package com.shaffinimam.i212963

data class Post(
    var postId: String = "",
    var userId: String = "",
    var image: String = "", // Base64 encoded image
    var caption: String = "",
    var dateCreated: String = "",
    var likesCount: Int = 0,
    var commentsCount: Int = 0
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", "", "", 0, 0)

    override fun toString(): String {
        return "Post(id=$postId, user=$userId, caption=$caption, date=$dateCreated, likes=$likesCount)"
    }
}