package me.laotang.carry.mvvm.binding.binding_adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.util.autoDisposable
import me.laotang.carry.util.onDestroyScope
import java.util.concurrent.TimeUnit

@BindingAdapter("textStr")
fun TextView.setText(any: Any?) {
    text = any?.toString() ?: ""
}


@BindingAdapter("textChanges")
fun TextView.setTextChanges(command: Command<CharSequence>) {
    if (this.context is LifecycleOwner) {
        this.textChanges()
            .debounce(200, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .autoDisposable((context as LifecycleOwner).onDestroyScope())
            .subscribe {
                command.execute(it)
            }
    } else {
        throw AssertionError("Activity 未实现 LifecycleOwner")
    }
}
