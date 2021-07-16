package me.laotang.carry.mvvm.demo.middleware.monitor

import kotlinx.coroutines.*
import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.redux.Store
import me.laotang.carry.mvvm.store.redux.middleware.Middleware
import timber.log.Timber
import java.util.concurrent.LinkedBlockingQueue

/**
 * 埋点上报中间件，可以监听并上传需要的Action
 * 主要用于处理数据，上传的话最好交由专业的第三方日志上报库
 */
class MonitorMiddleware<S>(private val store: Store<S>) :
    Middleware<Action, Action> {

    private val queue: LinkedBlockingQueue<Pair<Action, S>> =
        LinkedBlockingQueue<Pair<Action, S>>()

    private val job: Job

    init {
        val scope = CoroutineScope(Dispatchers.IO)
        job = scope.launch {
            while (isActive) {
                upload()
            }
        }
    }

    override fun dispatch(next: Middleware.Next<Action, Action>, action: Action): Action? {
        val r = next.next(action)
        //上报数据
        //本次action以及该action响应后的最新的state
        if (action is MonitorAction) {
            val state = store.getState()
            val reportMessage = Pair(action, state)
            queue.add(reportMessage)
        }
        return r
    }

    override fun onCleared() {
        job.cancel()
    }

    /**
     * 模拟上报
     * 实际生产中，一般使用第三方库上报，实现埋点数据持久化，补传等功能
     */
    private fun upload() {
        try {
            val message = queue.take()
            Timber.d(message.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

interface MonitorAction

