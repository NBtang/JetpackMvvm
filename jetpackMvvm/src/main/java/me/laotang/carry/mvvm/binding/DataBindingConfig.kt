package me.laotang.carry.mvvm.binding

import android.util.SparseArray


class DataBindingConfig(
    val variableId: Int,
    val value: Any
) {
    val bindingParams: SparseArray<Any> = SparseArray<Any>()

    fun addBindingParam(
        variableId: Int,
        any: Any
    ): DataBindingConfig {
        if (bindingParams.indexOfKey(variableId) < 0) {
            bindingParams.put(variableId, any)
        }
        return this
    }
}