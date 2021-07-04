package me.laotang.carry.mvvm.binding_adapter

import android.view.View
import android.view.View.*
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.functions.Consumer
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.clickObserver
import me.laotang.carry.util.onDestroyScope
import java.util.concurrent.TimeUnit

@BindingAdapter("visibleOrGone")
fun View.setVisibleOrGone(show: Boolean) {
    visibility = if (show) VISIBLE else GONE
}

@BindingAdapter("visible")
fun View.setVisible(show: Boolean) {
    visibility = if (show) VISIBLE else INVISIBLE
}


@BindingAdapter(value = ["click", "throttle"], requireAll = false)
fun View.setOnClick(consumer: Consumer<Unit>, throttle: Long?) {
    if (this.context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(throttle ?: 1, TimeUnit.SECONDS)
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe(consumer)
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}