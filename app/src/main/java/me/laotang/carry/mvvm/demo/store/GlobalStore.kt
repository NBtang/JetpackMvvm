package me.laotang.carry.mvvm.demo.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import me.laotang.carry.AppManager
import me.laotang.carry.core.subscriber.ProgressDialogUtil
import me.laotang.carry.mvvm.store.*
import me.laotang.carry.mvvm.store.redux.Effect
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.middleware.LogMiddleware
import me.laotang.carry.mvvm.store.redux.Store
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
    Effect<Action>, Store<GlobalState> {

    private val mGlobalStateLiveData: MutableLiveData<GlobalState> by lazy {
        MutableLiveData(GlobalState())
    }

    private val mDispatcher: Dispatcher<Action, Action> by lazy {
        Dispatcher.create(this)
            .chain(LogMiddleware("${GlobalStore::class.java.simpleName} action"))
    }

    val dispatcher: Dispatcher<Action, Action>
        get() = mDispatcher

    val token: String
        get() {
            return mGlobalStateLiveData.value!!.token
        }

    val isLoginLiveData: LiveData<Boolean>
        get() = mGlobalStateLiveData.map { it.token.isNotEmpty() }.distinctUntilChanged()

    val isLogin: Boolean
        get() = token.isNotEmpty()


    override fun onEffect(action: Action) {
        //loading框显示以及隐藏事件，对于应用来讲，应属于唯一事件，同时只能出现一个loading框
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
        }
    }

    override fun getState(): GlobalState {
        return mGlobalStateLiveData.value!!
    }

    override fun setState(state: GlobalState) {
        mGlobalStateLiveData.value = state
    }
}

/**
 * GlobalStore响应的action
 */
sealed class GlobalAction : Action {
    data class LoadingAction(val show: Boolean, val message: String) : GlobalAction(), Action
}

object GlobalActionCreator {
    fun loading(show: Boolean, message: String = "加载中"): Action {
        return GlobalAction.LoadingAction(show, message)
    }
}

