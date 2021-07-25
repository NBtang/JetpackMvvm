package me.laotang.carry.mvvm.store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.laotang.carry.mvvm.store.redux.Effect
import me.laotang.carry.mvvm.store.redux.SideMatch
import me.laotang.carry.mvvm.store.redux.Store
import me.laotang.carry.mvvm.store.redux.dispatcher.*


abstract class SimpleStore<S>(private val parentStore: SimpleStore<*>? = null) : Store<S>,
    Effect<Action> {

    private var childStores: MutableSet<SimpleStore<*>>? = null

    private lateinit var rootStore: SimpleStore<*>

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

    fun <T> getParentStore(): SimpleStore<T>? {
        if (parentStore == null) {
            return null
        }
        return parentStore as SimpleStore<T>
    }

    private var isDestroyed: Boolean = false

    init {
        initStore()
    }

    override fun onEffect(action: Action) {
        if (isDestroyed) {
            //已销毁，则不处理action
            return
        }
        //消费action
        onReallyEffect(action)
        //如果时广播类型，则广播
        if (action is BroadcastAction) {
            //广播
            rootStore.broadcast(action, this)
        }
    }

    open fun getStateAsLiveData(): LiveData<S> {
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
        //销毁时和ParentStore分离
        parentStore?.detachStore(this)
        //销毁时清空所有的childStore
        childStores?.clear()
        childStores = null
        mDispatcher.onCleared()
    }

    protected open fun attachStore(store: SimpleStore<*>) {
        if (childStores == null) {
            childStores = mutableSetOf()
        }
        childStores!!.add(store)
    }

    protected open fun detachStore(store: SimpleStore<*>) {
        childStores?.remove(store)
    }

    private fun initStore() {
        rootStore = parentStore?.rootStore ?: this
        parentStore?.attachStore(this)
    }

    private fun broadcast(action: Action, from: SimpleStore<*>) {
        //消费action
        onReallyEffect(action)
        //遍历childStore，再次广播
        childStores?.forEach {
            if (it != from) {
                it.broadcast(action, from)
            }
        }
    }

    private fun onReallyEffect(action: Action) {
        if (isDestroyed) {
            return
        }
        if (mSideMatch.effect(action)) {
            return
        }
        this.mStateLiveData.value = mSideMatch.reducer(action, mStateLiveData.value!!)
    }

    abstract fun initState(): S

    abstract fun getDispatcher(): Dispatcher<Action, Action>

    abstract fun getSideMatch(): SideMatch<Action, S>

}