package me.laotang.carry.mvvm.demo.model.api

import me.laotang.carry.mvvm.demo.model.entity.User
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {

    @Headers(HEADER_API_VERSION)
    @GET("/users")
    suspend fun getUsers(
        @Query("since") lastIdQueried: Int,
        @Query("per_page") perPage: Int
    ): List<User>

    companion object{
        const val HEADER_API_VERSION = "Accept: application/vnd.github.v3+json"
    }

}