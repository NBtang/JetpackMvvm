package me.laotang.carry.mvvm.demo.ui.store

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.redux.dispatcher.IDispatcher

/**
 * UI页面相关数据由viewModel维护，如（EditText中的内容、CheckBox的选中状态，网络请求返回的数据等）
 * viewModel维护store，同时分发state中的数据
 * 更新state的两种方式，一种直接通过store.setState修改，一种通过dispatcher发送action后更新
 */
class MainViewModel @ViewModelInject constructor(
    private val store: MainViewStore,
) :
    ViewModel() {

    private val dispatcher: IDispatcher<Action, Action>
        get() = store.dispatcher

    val usersLiveData: LiveData<List<User>>
        get() = store.getStateAsLiveData().map { it.users }

    private val lastIdQueried: String
        get() = store.getState().lastIdQueried


    fun loadUsers(loadMore: Boolean, showLoading: Boolean) {
        dispatcher.dispatch(
            MainViewActionCreator.load(
                lastIdQueried.toIntOrNull() ?: 0,
                loadMore,
                showLoading
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        store.destroy()
    }
}