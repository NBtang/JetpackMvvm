package me.laotang.carry.mvvm.demo.store

import me.laotang.carry.AppManager
import me.laotang.carry.core.subscriber.ProgressDialogUtil
import me.laotang.carry.mvvm.store.*
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.matchClass
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局数据
 */
data class GlobalState(val token: String = "")

/**
 * 全局Store，处理一些全局的action，处理以及提供全局范围的数据
 */
@Singleton
class GlobalStore @Inject constructor() :
    SimpleStore<GlobalState>() {

    override fun initState(): GlobalState {
        return GlobalState()
    }

    override fun getDispatcher(): Dispatcher<Action, Action> {
        return Dispatcher.create(this)
    }

    override fun getSideMatch(): SideMatch<Action, GlobalState> {
        return matchClass<Action, GlobalState>()
            .`when`(GlobalAction.LoadingAction::class.java, ::showLoading)
    }

    private fun showLoading(action: GlobalAction.LoadingAction) {
        AppManager.instance.getTopActivity()?.let {
            if (action.show) {
                ProgressDialogUtil.showLoadingDialog(
                    context = it,
                    content = action.message,
                    cancelable = false,
                )
            } else {
                ProgressDialogUtil.dismissLoadingDialog()
            }
        }
    }
}

/**
 * GlobalStore响应的action
 */
sealed class GlobalAction : Action {
    data class LoadingAction(val show: Boolean, val message: String) : GlobalAction(), BroadcastAction
}

object GlobalActionCreator {
    fun loading(show: Boolean, message: String = "加载中"): Action {
        return GlobalAction.LoadingAction(show, message)
    }
}

