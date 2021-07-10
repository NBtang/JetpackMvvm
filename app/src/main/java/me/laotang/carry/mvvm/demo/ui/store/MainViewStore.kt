package me.laotang.carry.mvvm.demo.ui.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.laotang.carry.mvvm.demo.domain.UserInfoRequestImpl
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.monitor.MonitorAction
import me.laotang.carry.mvvm.demo.monitor.MonitorMiddleware
import me.laotang.carry.mvvm.demo.store.GlobalActionCreator
import me.laotang.carry.mvvm.demo.store.GlobalStore
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.SimpleStore
import me.laotang.carry.mvvm.store.dispatch
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.matchClass
import me.laotang.carry.mvvm.store.redux.middleware.LogMiddleware
import javax.inject.Inject

/**
 * 数据以及初始值
 */
data class MainViewState(
    val users: List<User> = listOf(),
    val lastIdQueried: String = "0",
)

/**
 * 业务相关请求由Request维护，方便复用
 * 通过action处理业务，更新state中的数据，再由State去分发
 */
class MainViewStore @Inject constructor(
    private val globalStore: GlobalStore,
    private val mUserInfoRequestImpl: UserInfoRequestImpl,
) :
    SimpleStore<MainViewState>(MainViewState()) {

    override fun getDispatcher(): Dispatcher<Action, Action> {
        return Dispatcher.create(this, globalStore.dispatcher)
            .chain(LogMiddleware("${MainViewStore::class.java.simpleName} action"))
            .chain(MonitorMiddleware(this))
    }

    /**
     * action消费者注册，SideEffect以及Reducer都注册在这里
     */
    @ExperimentalCoroutinesApi
    override fun getSideMatch(): SideMatch<Action, MainViewState> {
        return matchClass<Action, MainViewState>()
            .`when`(MainViewAction.LoadAction::class.java, ::loadUsers)
            .`when`(MainViewAction.OnLoaded::class.java, ::onLoadUsers)
    }

    /**
     * SideEffect，不能更新state，但是可以通过网络请求等操作，请求数据交由Reducer产生新的state以更新UI
     * 通知Reducer的机制为方式新的action
     */
    @ExperimentalCoroutinesApi
    private fun loadUsers(action: MainViewAction.LoadAction) {
        val loadMore = action.loadMore
        val lastIdQueried = if (!loadMore) 0 else action.lastIdQueried
        val showLoading = action.showLoading

        dispatch {
            if (showLoading)
                emit(GlobalActionCreator.loading(true, "加载中..."))

            //获取用户列表-flow的形式
            val flowAction = mUserInfoRequestImpl.getUsers(lastIdQueried)
                .map { MainViewActionCreator.onLoaded(it, loadMore) }
                .flatMapLatest {
                    //获取user列表完成后，隐藏loading框，并更新数据
                    flow {
                        if (showLoading)
                            emit(GlobalActionCreator.loading(false))
                        emit(it)
                    }
                }
            emit(flowAction)
        }
    }

    /**
     * Reducer，返回新的state，因为数据驱动的机制，这将导致UI更新
     * Reducer应该是幂等的，同样的入参，返回的结果应该为一样
     */
    private fun onLoadUsers(action: MainViewAction.OnLoaded, state: MainViewState): MainViewState {
        val newUsers = if (action.loadMore) {
            state.users + action.users
        } else {
            action.users
        }
        return state.copy(
            users = newUsers,
            lastIdQueried = "${newUsers.lastOrNull()?.id ?: 0}",
        )
    }

    override fun destroy() {
        super.destroy()
        mUserInfoRequestImpl.close()
    }

}

/**
 * action
 * 使用internal修饰，则可以将这些action设置为私有，必须使用ActionCreator才能创建创建action
 */
internal sealed class MainViewAction : Action, MonitorAction {
    data class LoadAction(val lastIdQueried: Int, val loadMore: Boolean, val showLoading: Boolean) :
        MainViewAction()

    data class OnLoaded(val users: List<User>, val loadMore: Boolean) : MainViewAction()
}


object MainViewActionCreator {
    internal fun load(lastIdQueried: Int, loadMore: Boolean, showLoading: Boolean = true): Action {
        return MainViewAction.LoadAction(lastIdQueried, loadMore, !loadMore && showLoading)
    }

    internal fun onLoaded(users: List<User>, loadMore: Boolean): Action {
        return MainViewAction.OnLoaded(users, loadMore)
    }
}