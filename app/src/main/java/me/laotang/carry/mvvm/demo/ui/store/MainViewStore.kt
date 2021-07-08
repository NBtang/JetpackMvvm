package me.laotang.carry.mvvm.demo.ui.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.laotang.carry.mvvm.demo.domain.UserInfoRequestImpl
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.store.GlobalActionCreator
import me.laotang.carry.mvvm.demo.store.GlobalStore
import me.laotang.carry.mvvm.demo.ui.action.MainViewAction
import me.laotang.carry.mvvm.demo.ui.action.MainViewActionCreator
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.SimpleStore
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.matchClass
import me.laotang.carry.mvvm.store.redux.middleware.LogMiddleware

/**
 * 数据
 */
data class MainViewState(
    val users: List<User> = listOf(),
    val lastIdQueried: String = "0",
)

/**
 * 业务相关请求由Request维护，方便复用
 * 通过action处理业务，更新state中的数据，再由State去分发
 */
class MainViewStore(
    state: MainViewState,
    private val globalStore: GlobalStore,
    private val mUserInfoRequestImpl: UserInfoRequestImpl,
) :
    SimpleStore<MainViewState>(state) {

    override fun getDispatcher(): Dispatcher<Action, Action> {
        return Dispatcher.create(this, globalStore.dispatcher)
            .chain(LogMiddleware("${MainViewStore::class.java.simpleName} action"))
    }

    @ExperimentalCoroutinesApi
    override fun getSideMatch(): SideMatch<Action, MainViewState> {
        return matchClass<Action, MainViewState>()
            .`when`(MainViewAction.LoadAction::class.java, ::loadUsers)
            .`when`(MainViewAction.OnLoaded::class.java, ::onLoadUsers)
    }

    @ExperimentalCoroutinesApi
    fun loadUsers(action: MainViewAction.LoadAction) {
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
    }

    private fun onLoadUsers(action: MainViewAction.OnLoaded, state: MainViewState): MainViewState {
        return state.copy(users = action.users)
    }

    override fun destroy() {
        super.destroy()
        mUserInfoRequestImpl.close()
    }

}