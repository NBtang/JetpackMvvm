package me.laotang.carry.mvvm.demo.store

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.laotang.carry.AppManager
import me.laotang.carry.core.subscriber.ProgressDialogUtil
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.SideEffect
import me.laotang.carry.mvvm.store.SimpleStore
import me.laotang.carry.mvvm.store.core.middleware.LogMiddleware
import me.laotang.carry.mvvm.store.core.middleware.Middleware
import me.laotang.carry.mvvm.store.core.state.State
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局Store，处理一些全局的action，处理以及提供全局范围的数据
 */
@Singleton
class GlobalStore @Inject constructor(@ApplicationContext private val context: Context) :
    SimpleStore(), State<Action> {

    init {
        addState(this)
    }

    override fun addMiddleware(middlewareList: MutableList<Middleware<Action, Action>>) {
        super.addMiddleware(middlewareList)
        //提供action的日志记录功能
        middlewareList += LogMiddleware("${GlobalStore::class.java.simpleName} action")
    }

    @ExperimentalCoroutinesApi
    override fun getSideEffects(): List<SideEffect> {
        return listOf(
            ::loadingActionSideEffect
        )
    }

    //loading框显示以及隐藏事件，对于应用来讲，应属于唯一事件，同时只能出现一个loading框
    @ExperimentalCoroutinesApi
    private fun loadingActionSideEffect(action: Action): Boolean {
        if (action is GlobalAction.LoadingAction) {
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
            return true
        }
        return false
    }

    override fun setState(state: Action) {

    }
}


sealed class GlobalAction : Action {
    data class LoadingAction(val show: Boolean, val message: String) : GlobalAction(), Action
}

object GlobalActionCreator {
    fun loading(show: Boolean, message: String = ""): Action {
        return GlobalAction.LoadingAction(show, message)
    }
}

