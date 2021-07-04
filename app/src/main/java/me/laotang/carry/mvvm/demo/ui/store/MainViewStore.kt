package me.laotang.carry.mvvm.demo.ui.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.laotang.carry.mvvm.demo.domain.UserInfoRequestImpl
import me.laotang.carry.mvvm.demo.store.GlobalAction
import me.laotang.carry.mvvm.demo.store.GlobalActionCreator
import me.laotang.carry.mvvm.demo.store.GlobalStore
import me.laotang.carry.mvvm.demo.ui.action.MainViewAction
import me.laotang.carry.mvvm.demo.ui.action.MainViewActionCreator
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.SideEffect
import me.laotang.carry.mvvm.store.SimpleStore
import me.laotang.carry.mvvm.store.core.middleware.LogMiddleware
import me.laotang.carry.mvvm.store.core.middleware.Middleware
import javax.inject.Inject

class MainViewStore @Inject constructor(
    globalStore: GlobalStore,
    private val mUserInfoRequestImpl: UserInfoRequestImpl,
) :
    SimpleStore(globalStore) {

    @ExperimentalCoroutinesApi
    override fun getSideEffects(): List<SideEffect> {
        return listOf(
            ::getUsersSideEffect,
        )
    }

    override fun addMiddleware(middlewareList: MutableList<Middleware<Action, Action>>) {
        super.addMiddleware(middlewareList)
        //提供action的日志记录功能
        middlewareList += LogMiddleware("${MainViewStore::class.java.simpleName} action")
    }

    @ExperimentalCoroutinesApi
    private fun getUsersSideEffect(action: Action): Boolean {
        if (action is MainViewAction.LoadAction) {
            dispatcher.dispatch(GlobalActionCreator.loading(true, "加载中..."))
            val flowAction = mUserInfoRequestImpl.getUsers(action.lastIdQueried)
                .map { MainViewActionCreator.onLoaded(it) }
                .flatMapLatest {
                    //获取user列表完成后，隐藏loading框，并更新数据
                    flow {
                        emit(GlobalActionCreator.loading(false))
                        emit(it)
                    }
                }
            flowDispatcher.dispatch(flowAction)
            return true
        }
        return false
    }

    override fun destroy() {
        super.destroy()
        mUserInfoRequestImpl.close()
    }
}