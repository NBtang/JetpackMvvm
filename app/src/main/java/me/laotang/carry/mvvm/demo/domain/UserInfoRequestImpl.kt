package me.laotang.carry.mvvm.demo.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.model.repository.DataRepository
import me.laotang.carry.mvvm.domain.BaseRequest
import javax.inject.Inject

/**
 * 实际业务请求封装类
 */
class UserInfoRequestImpl @Inject constructor(private val dataRepository: DataRepository) :
    BaseRequest() {

    fun getUsers(lastIdQueried: Int): Flow<List<User>> {
        return flow {
            val users = dataRepository.getUsers(lastIdQueried)
            emit(users)
        }.catch { cause ->
            mHandlerFactory.handleError(cause)
            emit(listOf())
        }
    }

}