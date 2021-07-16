package me.laotang.carry.mvvm.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.laotang.carry.mvvm.store.redux.Effect
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.Store
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.DispatcherWrapper
import me.laotang.carry.mvvm.store.redux.dispatcher.IDispatcherWrapper

abstract class StoreViewModelStore<S> : ViewModel(), Store<S>, Effect<Action> {

    private val mStateLiveData: MutableLiveData<S> by lazy {
        MutableLiveData(initState())
    }

    private val mSideMatch: SideMatch<Action, S> by lazy {
        getSideMatch()
    }

    private val mDispatcher: DispatcherWrapper<Action> by lazy {
        DispatcherWrapper(getDispatcher())
    }

    val dispatcher: IDispatcherWrapper<Action>
        get() = mDispatcher

    open fun getStateAsLiveData(): LiveData<S> {
        return this.mStateLiveData
    }

    override fun getState(): S {
        return this.mStateLiveData.value!!
    }

    override fun setState(state: S) {
        this.mStateLiveData.value = state
    }

    override fun onEffect(action: Action) {
        if (mSideMatch.effect(action)) {
            return
        }
        this.mStateLiveData.value = mSideMatch.reducer(action, mStateLiveData.value!!)
    }


    override fun onCleared() {
        super.onCleared()
        mDispatcher.onCleared()
    }

    abstract fun initState(): S

    abstract fun getDispatcher(): Dispatcher<Action, Action>

    abstract fun getSideMatch(): SideMatch<Action, S>
}