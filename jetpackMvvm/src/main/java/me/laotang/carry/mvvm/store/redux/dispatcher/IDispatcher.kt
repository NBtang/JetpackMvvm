package me.laotang.carry.mvvm.store.redux.dispatcher

import me.laotang.carry.mvvm.store.redux.Effect


interface IDispatcher<A, R> {
    fun dispatch(action: A): R
    fun onCleared()
}

abstract class EffectDispatcher<A>(
    private val dispatcher: IDispatcher<A, A>? = null
) :
    Dispatcher<A, A>(), Effect<A> {

    private lateinit var mDispatcher: IDispatcher<A, A>

    init {
        initDispatcher()
    }

    private fun initDispatcher() {
        mDispatcher = if (dispatcher != null) {
            create(this, dispatcher)
        } else {
            create(this)
        }

    }

    override fun dispatch(action: A): A {
        return mDispatcher.dispatch(action)
    }
}