package me.laotang.carry.mvvm.demo.ui.action

import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.store.Action

sealed class MainViewAction : Action {
    data class LoadAction(val lastIdQueried: Int) : MainViewAction()

    data class OnLoaded(val users: List<User>) : MainViewAction()
}


object MainViewActionCreator {
    fun load(lastIdQueried: Int): Action {
        return MainViewAction.LoadAction(lastIdQueried)
    }

    fun onLoaded(users: List<User>): Action {
        return MainViewAction.OnLoaded(users)
    }
}