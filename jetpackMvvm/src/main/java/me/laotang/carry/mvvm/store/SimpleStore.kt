package me.laotang.carry.mvvm.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.laotang.carry.mvvm.store.redux.Effect
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.IDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.flow.FlowDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.flow.asFlowDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.rx.RxDispatcher
import me.laotang.carry.mvvm.store.redux.dispatcher.rx.asRxDispatcher
import me.laotang.carry.mvvm.store.redux.Store


abstract class SimpleStore<S>(
    state: S,
) : Store<S>, Effect<Action> {

    private val mStateLiveData: MutableLiveData<S> by lazy {
        MutableLiveData(state)
    }

    private val mDispatcher: Dispatcher<Action, Action> by lazy {
        getDispatcher()
    }

    private val mSideMatch: SideMatch<Action, S> by lazy {
        getSideMatch()
    }

    val dispatcher: IDispatcher<Action, Action>
        get() = mDispatcher

    private var mFlowDispatcher: FlowDispatcher<Action>? = null

    private var mRxDispatcher: RxDispatcher<Action>? = null

    val flowDispatcher: IDispatcher<Flow<Action>, Job>
        get() = getFlowDispatcher()

    val rxDispatcher: IDispatcher<Observable<Action>, Unit>
        get() = getRxDispatcher()

    private var isDestroyed: Boolean = false

    override fun onEffect(action: Action) {
        if (isDestroyed) {
            return
        }
        if (mSideMatch.effect(action)) {
            return
        }
        this.mStateLiveData.value = mSideMatch.reducer(action, mStateLiveData.value!!)
    }

    private fun getFlowDispatcher(): FlowDispatcher<Action> {
        if (mFlowDispatcher == null) {
            mFlowDispatcher = dispatcher.asFlowDispatcher
        }
        return mFlowDispatcher!!
    }

    private fun getRxDispatcher(): RxDispatcher<Action> {
        if (mRxDispatcher == null) {
            mRxDispatcher = dispatcher.asRxDispatcher
        }
        return mRxDispatcher!!
    }

    fun getStateAsLiveData(): LiveData<S> {
        return this.mStateLiveData
    }

    override fun getState(): S {
        return this.mStateLiveData.value!!
    }

    override fun setState(state: S) {
        this.mStateLiveData.value = state
    }

    open fun destroy() {
        isDestroyed = true
        dispatcher.onCleared()
        mFlowDispatcher?.onCleared()
        mRxDispatcher?.onCleared()
    }

    abstract fun getDispatcher(): Dispatcher<Action, Action>

    abstract fun getSideMatch(): SideMatch<Action, S>

}


interface DispatcherDelegate {
    fun emit(action: Action)
    fun emit(action: Flow<Action>)
    fun emit(action: Observable<Action>)
}


fun <S> SimpleStore<S>.dispatch(block: DispatcherDelegate.() -> Unit) {
    val store = this
    val dispatcherWrapper = object : DispatcherDelegate {
        override fun emit(action: Action) {
            store.dispatcher.dispatch(action)
        }

        override fun emit(action: Flow<Action>) {
            store.flowDispatcher.dispatch(action)
        }

        override fun emit(action: Observable<Action>) {
            store.rxDispatcher.dispatch(action)
        }
    }
    dispatcherWrapper.block()
}