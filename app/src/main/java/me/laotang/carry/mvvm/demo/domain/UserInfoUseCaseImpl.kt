package me.laotang.carry.mvvm.demo.domain

import kotlinx.coroutines.flow.Flow
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.model.repository.DataRepository
import me.laotang.carry.mvvm.domain.BaseUseCase
import me.laotang.carry.mvvm.domain.flowCatch
import javax.inject.Inject

/**
 * 实际业务请求封装类
 */
class UserInfoUseCaseImpl @Inject constructor(private val dataRepository: DataRepository) :
    BaseUseCase() {

    fun getUsers(lastIdQueried: Int): Flow<List<User>> {
        return flowCatch(listOf()) {
            val users = dataRepository.getUsers(lastIdQueried)
            emit(users)
        }
    }

}