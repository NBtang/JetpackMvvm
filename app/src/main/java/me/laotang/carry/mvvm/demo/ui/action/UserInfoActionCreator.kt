package me.laotang.carry.mvvm.demo.ui.action

import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.dispatcher.Action

data class GetUsers(val lastIdQueried: Int) : Action()
data class ShowLoading(val show: Boolean) : Action()
data class OnUsersLoaded(val users: List<User>) : Action()

object UserInfoActionCreator {
    fun getUsers(lastIdQueried: Int): Action {
        return GetUsers(lastIdQueried)
    }

    fun showLoading(show: Boolean): Action {
        return ShowLoading(show)
    }

    fun onLoaded(users: List<User>): Action {
        return OnUsersLoaded(users)
    }
}