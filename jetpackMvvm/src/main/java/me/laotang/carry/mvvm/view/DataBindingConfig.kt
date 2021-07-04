package me.laotang.carry.mvvm.view

import android.util.SparseArray
import androidx.lifecycle.ViewModel


class DataBindingConfig(
    val vmVariableId: Int,
    val storeViewModel: ViewModel
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