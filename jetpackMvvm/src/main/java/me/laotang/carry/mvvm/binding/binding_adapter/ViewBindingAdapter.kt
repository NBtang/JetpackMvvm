package me.laotang.carry.mvvm.binding.binding_adapter

import android.view.View
import android.view.View.*
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding3.view.clicks
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.onDestroyScope
import java.util.concurrent.TimeUnit

@BindingAdapter("app:visibleOrGone")
fun View.setVisibleOrGone(show: Boolean) {
    visibility = if (show) VISIBLE else GONE
}

@BindingAdapter("app:visible")
fun View.setVisible(show: Boolean) {
    visibility = if (show) VISIBLE else INVISIBLE
}

@BindingAdapter(value = ["app:clicks", "app:throttle"], requireAll = false)
fun View.setOnClick(block: Command<Unit>, throttle: Long?) {
    if (this.context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(throttle ?: 1, TimeUnit.SECONDS)
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe {
                block.execute(Unit)
            }
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}