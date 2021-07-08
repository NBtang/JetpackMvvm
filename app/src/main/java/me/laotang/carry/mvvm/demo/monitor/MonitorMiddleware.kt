package me.laotang.carry.mvvm.demo.monitor

import me.laotang.carry.mvvm.store.Action
import me.laotang.carry.mvvm.store.redux.Store
import me.laotang.carry.mvvm.store.redux.middleware.Middleware

/**
 * 埋点上报中间件，可以监听并上传需要的Action
 * 主要用于处理数据，上传的话最好交由专业的第三方日志上报库
 */
class MonitorMiddleware<S>(private val store: Store<S>) : Middleware<Action, Action> {

    override fun dispatch(next: Middleware.Next<Action, Action>, action: Action): Action? {
        val r = next.next(action)

        return r
    }
}

