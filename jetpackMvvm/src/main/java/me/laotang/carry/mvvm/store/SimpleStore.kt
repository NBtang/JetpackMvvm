package me.laotang.carry.mvvm.store

import io.reactivex.Observable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import me.laotang.carry.mvvm.store.core.dispatcher.Dispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.IDispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.flow.FlowDispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.flow.asFlowDispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.rx.RxDispatcher
import me.laotang.carry.mvvm.store.core.dispatcher.rx.asRxDispatcher
import me.laotang.carry.mvvm.store.core.effect.Effect
import me.laotang.carry.mvvm.store.core.middleware.Middleware
import me.laotang.carry.mvvm.store.core.state.State

typealias SideEffect = (action: Action) -> Boolean

abstract class SimpleStore(
    protected val parentStore: SimpleStore? = null,
    private val middlewares: List<Middleware<Action, Action>>? = null
) : Effect<Action> {


    private var sideEffects: List<SideEffect>? = null

    private lateinit var mDispatcher: Dispatcher<Action, Action>

    private var mFlowDispatcher: FlowDispatcher<Action>? = null

    private var mRxDispatcher: RxDispatcher<Action>? = null

    val dispatcher: IDispatcher<Action, Action>
        get() = mDispatcher

    protected val flowDispatcher: IDispatcher<Flow<Action>, Job>
        get() = getFlowDispatcher()


    protected val rxDispatcher: IDispatcher<Observable<Action>, Unit>
        get() = getRxDispatcher()

    private var isDestroyed: Boolean = false

    private val states: MutableList<State<Action>> by lazy {
        mutableListOf()
    }

    init {
        initDispatcher(parentStore?.dispatcher)
    }

    private fun initDispatcher(dispatcher: IDispatcher<Action, Action>?) {
        val middlewareList = mutableListOf<Middleware<Action, Action>>()
        this.middlewares?.let {
            middlewareList.addAll(it)
        }
        addMiddleware(middlewareList)
        mDispatcher = Dispatcher.create<Effect<Action>, Action>(this, dispatcher)
            .chain(*middlewareList.toTypedArray())
    }

    abstract fun getSideEffects(): List<SideEffect>

    protected open fun addMiddleware(middlewareList: MutableList<Middleware<Action, Action>>) {}

    override fun onEffect(action: Action) {
        if (isDestroyed) {
            return
        }
        requireSideEffects().let {
            if (it.isEmpty()) {
                states.forEach { state ->
                    state.setState(action)
                }
            } else {
                var found = false
                for (i in it.indices) {
                    if (it[i].invoke(action)) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    states.forEach { state ->
                        state.setState(action)
                    }
                }
            }
        }
    }

    fun addState(state: State<Action>) {
        states.add(state)
    }

    open fun destroy() {
        isDestroyed = true
        sideEffects = null
        dispatcher.onCleared()
        mFlowDispatcher?.onCleared()
        mFlowDispatcher?.onCleared()
    }

    private fun requireSideEffects(): List<SideEffect> {
        if (sideEffects == null) {
            sideEffects = getSideEffects()
        }
        return sideEffects!!
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
}