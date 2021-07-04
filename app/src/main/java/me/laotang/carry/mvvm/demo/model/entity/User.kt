package me.laotang.carry.mvvm.demo.model.entity

import com.squareup.moshi.*

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int,
    @Json(name = "login") val login: String,
    @Json(name = "type") val userType: UserType,
    @Json(name = "avatar_url") val avatar_url: String
){
    val avatarUrl: String
        get() = if (avatar_url.isEmpty()) {
            avatar_url
        } else {
            avatar_url.split("\\?".toRegex()).toTypedArray()[0]
        }
}

enum class UserType {
    User,
    Admin,
}