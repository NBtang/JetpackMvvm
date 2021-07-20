package me.laotang.carry.mvvm.binding.binding_adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding3.widget.textChanges
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.onDestroyScope

@BindingAdapter("app:textStr")
fun TextView.setText(any: Any?) {
    text = any?.toString() ?: ""
}


@BindingAdapter("app:textChanges")
fun TextView.setTextChanges(command: Command<CharSequence>) {
    if (this.context is LifecycleOwner) {
        this.textChanges()
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe {
                command.execute(it)
            }
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}
