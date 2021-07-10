package me.laotang.carry.mvvm.demo.ui.view.binding_adapter

import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.onDestroyScope

@BindingAdapter("onScrollState")
fun RecyclerView.addOnScrollStateListener(changed: Command<Int>) {
    if (this.context is LifecycleOwner) {
        this.scrollStateChanges()
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe {
                changed.execute(it)
            }
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}


@BindingAdapter("fixedSize")
fun RecyclerView.fixedSize(fixedSize: Boolean) {
    this.setHasFixedSize(fixedSize)
}

