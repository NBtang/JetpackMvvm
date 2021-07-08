package me.laotang.carry.mvvm.store.redux

typealias SideEffect<A> = (action: A) -> Unit
typealias Reducer<A, S> = (action: A, state: S) -> S

interface SideMatch<A, S> {
    fun effect(action: A): Boolean
    fun reducer(action: A, state: S): S
}

abstract class BaseSideMatch<A, S, M> : SideMatch<A, S> {

    private val sideEffectMatchers: MutableList<Pair<M,SideEffect<A>>> = mutableListOf()
    private val reducerMatchers: MutableList<Pair<M,Reducer<A,S>>> = mutableListOf()

    protected open fun putReducer(matcher: M, reducer: Reducer<A, S>) {
        reducerMatchers.add(Pair(matcher,reducer))
    }

    protected open fun putSideEffect(matcher: M, sideEffect: SideEffect<A>) {
        sideEffectMatchers.add(Pair(matcher,sideEffect))
    }

    protected abstract fun match(matcher: M, action: A): Boolean

    override fun reducer(action: A, state: S): S {
        for (i in reducerMatchers.indices) {
            if (match(reducerMatchers[i].first, action)) {
                return reducerMatchers[i].second.invoke(action, state)
            }
        }
        return state
    }

    override fun effect(action: A): Boolean {
        var match = false
        for (i in sideEffectMatchers.indices) {
            if (match(sideEffectMatchers[i].first, action)) {
                match = true
                sideEffectMatchers[i].second.invoke(action)
                break
            }
        }
        return match
    }
}

class MatchClassBase<A, S> :
    BaseSideMatch<A, S, Class<*>>() {

    fun <RA : A> `when`(
        actionClass: Class<RA>,
        reducer: Reducer<RA, S>
    ): MatchClassBase<A, S> {
        putReducer(actionClass, reducer as Reducer<A, S>)
        return this
    }

    fun <RA : A> `when`(
        actionClass: Class<RA>,
        effect: SideEffect<RA>
    ): MatchClassBase<A, S> {
        putSideEffect(actionClass, effect as SideEffect<A>)
        return this
    }

    override fun match(matcher: Class<*>, action: A): Boolean {
        return matcher.isInstance(action);
    }
}

fun <A, S> matchClass(): MatchClassBase<A, S> {
    return MatchClassBase()
}