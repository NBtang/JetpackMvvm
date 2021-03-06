package me.laotang.carry.mvvm.demo.ui.view.binding_adapter

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.onDestroyScope

@BindingAdapter("onRefresh")
fun SwipeRefreshLayout.onRefresh(block: Command<Unit>) {
    if (this.context is LifecycleOwner) {
        this.refreshes()
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe {
                block.execute(Unit)
            }
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}