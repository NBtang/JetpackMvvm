package me.laotang.carry.mvvm.dispatcher.middleware;

import android.util.Log;

import timber.log.Timber;

public class LogMiddleware<A, R> implements Middleware<A, R> {

    private final String tag;
    private final int priority;

    public LogMiddleware(String tag) {
        this(tag, Log.DEBUG);
    }

    public LogMiddleware(String tag, int priority) {
        this.tag = tag;
        this.priority = priority;
    }

    @Override
    public R dispatch(Next<A, R> next, A action) {
        Timber.tag(tag);
        Timber.log(priority,action.toString());
        return next.next(action);
    }
}
