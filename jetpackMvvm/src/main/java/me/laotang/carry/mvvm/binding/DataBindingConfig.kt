package me.laotang.carry.mvvm.binding

import android.util.SparseArray


class DataBindingConfig<T>(
    val variableId: Int,
    val value: T
) {
    val bindingParams: SparseArray<Any> = SparseArray<Any>()

    fun addBindingParam(
        variableId: Int,
        any: Any
    ): DataBindingConfig<T> {
        if (bindingParams.indexOfKey(variableId) < 0) {
            bindingParams.put(variableId, any)
        }
        return this
    }
}