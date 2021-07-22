package me.laotang.carry.mvvm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import me.laotang.carry.mvvm.binding.DataBindingConfig

abstract class BaseDataBindFragment<T : ViewDataBinding> : Fragment() {

    protected lateinit var binding: T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBindingConfig: DataBindingConfig = getDataBindingConfig()
        binding = DataBindingUtil.inflate(inflater, layoutId(), container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.setVariable(
            dataBindingConfig.variableId,
            dataBindingConfig.value
        )
        val bindingParams = dataBindingConfig.bindingParams
        for (i in 0 until bindingParams.size()) {
            binding.setVariable(bindingParams.keyAt(i), bindingParams.valueAt(i))
        }
        return binding.root
    }

    abstract fun layoutId(): Int

    abstract fun getDataBindingConfig(): DataBindingConfig

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}