package me.laotang.carry.mvvm.demo.ui.store

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import me.laotang.carry.mvvm.demo.domain.UserInfoRequestImpl
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.store.GlobalStore
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.redux.dispatcher.IDispatcher

/**
 * 业务相关请求由Request维护，方便复用
 * UI页面相关数据由viewModel维护，如（EditText中的内容、CheckBox的选中状态，网络请求返回的数据等）
 * viewModel维护store，同时分发state中的数据
 * 更新state的两种方式，一种直接通过store.setState修改，一种通过dispatcher发送action后更新
 */
class MainViewModel @ViewModelInject constructor(
    private val globalStore: GlobalStore,
    private val userInfoRequest: UserInfoRequestImpl,
) :
    ViewModel() {

    /**
     * redux的store
     */
    private val store: MainViewStore by lazy {
        MainViewStore(MainViewState(), globalStore, userInfoRequest)
    }

    private val state: MainViewState
        get() = store.getState()

    //向外暴露dispatcher，为view层提供dispatcher
    val dispatcher: IDispatcher<Action, Action>
        get() = store.dispatcher

    val usersLiveData: LiveData<List<User>>
        get() = store.getStateAsLiveData().map { it.users }.distinctUntilChanged()

    val lastIdQueried: String
        get() = state.lastIdQueried

    override fun onCleared() {
        super.onCleared()
        store.destroy()
    }
}