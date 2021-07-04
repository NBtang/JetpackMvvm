package me.laotang.carry.mvvm.demo.ui.store

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import io.reactivex.functions.Consumer
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.ui.action.*
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.core.dispatcher.IDispatcher
import me.laotang.carry.mvvm.store.core.state.State

/**
 * 网络请求由Request维护，方便在多个viewModel中复用，一个viewModel也可以拥有多个Request
 * UI页面相关数据由viewModel维护，如（EditText中的内容、CheckBox的选中状态，网络请求返回的数据等）
 * 类似于redux，但是不想写太多模版代码、定义一大堆东西，没有整Reducer，能记录整个数据流就行，比较懒
 */
class MainViewModel @ViewModelInject constructor(private val store: MainViewStore) :
    ViewModel(), State<Action> {

    //MainActivity页面的state，每一个状态都对应相应的UI
    private val mData: MediatorLiveData<MainViewData> by lazy {
        val livedData = MediatorLiveData<MainViewData>()
        livedData.value = MainViewData()
        return@lazy livedData
    }

    val dispatcher: IDispatcher<Action, Action>
        get() = store.dispatcher

    //通过map返回指定的数据类型
    val usersLiveData: LiveData<List<User>>
        get() = mData.map { it.users }.distinctUntilChanged()

    //通过dataBinding实现和view层的editText的textChange事件的绑定，在viewModel实时保存
    //单项绑定
    val lastIdQueried: String
        get() = mData.value?.lastIdQueried ?: ""

    val lastIdQueriedChanges: Consumer<CharSequence> by lazy {
        Consumer<CharSequence> {
            val lastIdQueried = it.toString()
            mData.value = requireData().copy(lastIdQueried = lastIdQueried)
        }
    }

    init {
        store.addState(this)
    }

    override fun setState(state: Action) {
        when (state) {
            is MainViewAction.OnLoaded -> {
                mData.value = requireData().copy(users = state.users)
            }
        }
    }

    private fun requireData(): MainViewData {
        return mData.value ?: MainViewData()
    }

    override fun onCleared() {
        super.onCleared()
        store.destroy()
    }
}

/**
 * view层所需要的全部数据
 */
data class MainViewData(
    val users: List<User> = listOf(),
    val lastIdQueried: String = "0",
)