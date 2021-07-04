package me.laotang.carry.mvvm.vm

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.laotang.carry.mvvm.dispatcher.Action
import me.laotang.carry.mvvm.dispatcher.Dispatcher
import me.laotang.carry.mvvm.dispatcher.Effect
import me.laotang.carry.mvvm.dispatcher.IDispatcher
import me.laotang.carry.mvvm.dispatcher.flow.FlowDispatcher
import me.laotang.carry.mvvm.dispatcher.middleware.Middleware
import me.laotang.carry.mvvm.dispatcher.rx.RxDispatcher
import java.util.*

abstract class BaseViewModel : ViewModel(), Effect<Action> {

    internal lateinit var mDispatcher: Dispatcher<Action, Action>

    internal var mRxDispatcher: RxDispatcher<Action>? = null
    internal var mFlowDispatcher: FlowDispatcher<Action>? = null

    private val mTags: Map<String, Any> = HashMap()

    val dispatcher: IDispatcher<Action, Action>
        get() = mDispatcher

    init {
        initDispatcher()
    }

    private fun initDispatcher() {
        val middlewareList = mutableListOf<Middleware<Action, Action>>()
        addMiddleware(middlewareList)
        mDispatcher = Dispatcher.create<Effect<Action>, Action>(this)
            .chain(*middlewareList.toTypedArray())
    }

    protected open fun addMiddleware(middlewareList: MutableList<Middleware<Action, Action>>) {}

    //释放内存
    override fun onCleared() {
        super.onCleared()
        mDispatcher.onCleared()
        mRxDispatcher?.onCleared()
        mFlowDispatcher?.onCleared()
    }
}

val BaseViewModel.rxDispatcher: IDispatcher<Observable<Action>, Unit>
    get() {
        if (this.mRxDispatcher == null) {
            mRxDispatcher = RxDispatcher(mDispatcher)
        }
        return mRxDispatcher!!
    }

val BaseViewModel.flowDispatcher: Dispatcher<Flow<Action>, Job>
    get() {
        if (this.mFlowDispatcher == null) {
            mFlowDispatcher = FlowDispatcher(mDispatcher)
        }
        return mFlowDispatcher!!
    }