package me.laotang.carry.mvvm.store.redux.dispatcher

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import me.laotang.carry.mvvm.store.redux.dispatcher.flow.FlowDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.flow.asFlowDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.rx.RxDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.rx.asRxDispatcher

interface IDispatcherWrapper<A> {
    fun dispatch(action: A): A
    fun dispatch(action: Flow<A>)
    fun dispatch(action: Observable<A>)
}

class DispatcherWrapper<A>(private val dispatcher: Dispatcher<A, A>) : IDispatcherWrapper<A>,
    IDispatcher<A, A> {

    private var mFlowDispatcher: FlowDispatcher<A>? = null

    private var mRxDispatcher: RxDispatcher<A>? = null

    private fun getFlowDispatcher(): FlowDispatcher<A> {
        if (mFlowDispatcher == null) {
            mFlowDispatcher = dispatcher.asFlowDispatcher
        }
        return mFlowDispatcher!!
    }

    private fun getRxDispatcher(): RxDispatcher<A> {
        if (mRxDispatcher == null) {
            mRxDispatcher = dispatcher.asRxDispatcher
        }
        return mRxDispatcher!!
    }

    override fun dispatch(action: A): A {
        dispatcher.dispatch(action)
        return action
    }

    override fun dispatch(action: Flow<A>) {
        getFlowDispatcher().dispatch(action)
    }

    override fun dispatch(action: Observable<A>) {
        getRxDispatcher().dispatch(action)
    }

    override fun onCleared() {
        dispatcher.onCleared()
        mFlowDispatcher?.onCleared()
        mRxDispatcher?.onCleared()
    }
}

