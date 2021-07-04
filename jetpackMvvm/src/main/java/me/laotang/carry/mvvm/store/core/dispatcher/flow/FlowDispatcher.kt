package me.laotang.carry.mvvm.store.core.dispatcher.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import me.laotang.carry.mvvm.store.core.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.IDispatcher
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class FlowDispatcher<A>(private val dispatcher: IDispatcher<A, *>) : Dispatcher<Flow<A>, Job>() {
    private val scope: CoroutineScope

    init {
        val context: CoroutineContext = EmptyCoroutineContext
        val supervisorJob = SupervisorJob(context[Job])
        val coroutineContext = Dispatchers.Main.immediate + context + supervisorJob
        scope = CoroutineScope(coroutineContext)
    }

    override fun dispatch(action: Flow<A>): Job {
        return scope.launch {
            action.collect {
                dispatcher.dispatch(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}


val <A, R> IDispatcher<A, R>.asFlowDispatcher: FlowDispatcher<A>
    get() {
        return FlowDispatcher(this)
    }