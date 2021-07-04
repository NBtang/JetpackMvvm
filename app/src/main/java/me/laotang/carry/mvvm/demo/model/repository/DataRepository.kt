package me.laotang.carry.mvvm.demo.model.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.laotang.carry.core.IRepositoryManager
import me.laotang.carry.mvvm.demo.model.api.ApiService
import me.laotang.carry.mvvm.demo.model.entity.User
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 数据提供者（网络数据或者本地缓存数据）
 */
@Singleton
class DataRepository @Inject constructor(private val repositoryManager: IRepositoryManager) {

    private val apiService: ApiService by lazy {
        repositoryManager.obtainRetrofitService(ApiService::class.java)
    }

    suspend fun getUsers(lastIdQueried: Int = 0): List<User> {
        return withContext(Dispatchers.IO) {
            apiService.getUsers(lastIdQueried, 10)
        }
    }
}