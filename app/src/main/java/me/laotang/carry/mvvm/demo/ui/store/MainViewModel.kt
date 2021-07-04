package me.laotang.carry.mvvm.demo.ui.store

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.map
import me.laotang.carry.mvvm.demo.domain.UserInfoRequestImpl
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.ui.action.*
import me.laotang.carry.mvvm.dispatcher.Action
import me.laotang.carry.mvvm.dispatcher.middleware.LogMiddleware
import me.laotang.carry.mvvm.dispatcher.middleware.Middleware
import me.laotang.carry.mvvm.domain.IRequest
import me.laotang.carry.mvvm.vm.BaseViewModel
import me.laotang.carry.mvvm.vm.flowDispatcher

/**
 * 网络请求由Request维护，方便在多个viewModel中复用，一个viewModel也可以拥有多个Request
 * UI页面相关数据由viewModel维护，如（EditText中的内容、CheckBox的选中状态，网络请求返回的数据等）
 * viewModel尽量作为store使用，通过Dispatcher分发业务
 * 类似于redux，但是不想写太多模版代码、定义一大堆东西，没有整Reducer，能记录整个数据流就行，比较懒
 */
class MainViewModel @ViewModelInject constructor(private val mUserInfoRequestImpl: UserInfoRequestImpl) :
    BaseViewModel() {

    //MainActivity页面的state，每一个状态都对应相应的UI
    private val mData: MediatorLiveData<MainViewData> by lazy {
        val livedData = MediatorLiveData<MainViewData>()
        livedData.value = MainViewData()
        return@lazy livedData
    }

    //以接口的形式提供，避免view层接触数据和操作数据，保证viewModel为唯一可信数据源提供者
    val userInfoRequest: IRequest
        get() = mUserInfoRequestImpl

    //通过map返回指定的数据类型
    val usersLiveData: LiveData<List<User>>
        get() = mData.map { it.users }.distinctUntilChanged()

    private val mLoadingLiveData: UnPeekLiveData<Boolean> by lazy {
        UnPeekLiveData()
    }

    val loadingLiveData: LiveData<Boolean>
        get() = mLoadingLiveData.distinctUntilChanged()

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

    override fun addMiddleware(middlewareList: MutableList<Middleware<Action, Action>>) {
        super.addMiddleware(middlewareList)
        //提供action的日志记录功能
        middlewareList += LogMiddleware("ACTION")
    }

    override fun onEffect(action: Action) {
        when (action) {
            is GetUsers -> {
                dispatcher.dispatch(UserInfoActionCreator.showLoading(true))
                val flowAction = mUserInfoRequestImpl.getUsers(action.lastIdQueried)
                    .map { UserInfoActionCreator.onLoaded(it) }
                flowDispatcher.dispatch(flowAction)
            }
            is ShowLoading -> {
                mLoadingLiveData.setValue(action.show)
            }
            is OnUsersLoaded -> {
                dispatcher.dispatch(UserInfoActionCreator.showLoading(false))
                mData.value = requireData().copy(users = action.users)
            }
        }
    }

    private fun requireData(): MainViewData {
        return mData.value ?: MainViewData()
    }
}

/**
 * view层所需要的全部数据
 */
data class MainViewData(
    val users: List<User> = listOf(),
    val lastIdQueried: String = "0",
)