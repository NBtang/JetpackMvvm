package me.laotang.carry.mvvm.demo.ui.view.adapter

import androidx.core.util.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.listener.OnItemSwipeListener
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import me.laotang.carry.mvvm.binding.DataBindingConfig
import me.laotang.carry.mvvm.entity.DataEntity

/**
 * BaseQuickAdapter代理抽象
 */
abstract class AdapterDelegate<T> {
    protected lateinit var mAdapter: BaseQuickAdapter<T, BaseViewHolder>

    abstract fun convert(holder: BaseViewHolder, item: T)

    open fun init(adapter: BaseQuickAdapter<T, BaseViewHolder>) {
        this.mAdapter = adapter
    }

    open fun onViewRecycled(holder: BaseViewHolder) {}

    open fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {}

    open fun onLoadMore() {}
}

/**
 * 具备DataBinding能力的BaseQuickAdapter代理
 */
abstract class DataBindingAdapterDelegate<T : DataEntity, B : ViewDataBinding>(
    private val dateVariableId: Int,
    private val config: DataBindingConfig? = null
) :
    AdapterDelegate<T>() {

    override fun convert(holder: BaseViewHolder, item: T) {
        val binding = holder.getBinding<B>()
        binding?.setVariable(dateVariableId, item)
        config?.let {
            binding?.setVariable(config.variableId, config.value)
            it.bindingParams.forEach { key, value ->
                binding?.setVariable(key, value)
            }
        }
        convert(binding, item)
    }

    override fun onItemViewHolderCreated(viewHolder: BaseViewHolder, viewType: Int) {
        DataBindingUtil.bind<B>(viewHolder.itemView)
    }

    override fun init(adapter: BaseQuickAdapter<T, BaseViewHolder>) {
        super.init(adapter)
        adapter.setDiffCallback(DiffDataItemCallback())
    }

    open fun convert(binding: B?, item: T) {}

}


interface AdapterDelegator<T> {
    fun create(delegate: AdapterDelegate<T>?): BaseQuickAdapter<T, BaseViewHolder>
}

/**
 * 为代理提供回调的封装类，继承BaseQuickAdapter，复写部分方法，提供回调能力
 */
abstract class BaseModifyAdapter<T>(layoutResId: Int, data: MutableList<T>? = null) :
    BaseQuickAdapter<T, BaseViewHolder>(layoutResId, data) {

    private var delegate: AdapterDelegate<T>? = null

    internal fun setAdapterDelegate(delegate: AdapterDelegate<T>?) {
        this.delegate = delegate
        this.delegate?.init(this)
    }

    override fun onItemViewHolderCreated(
        viewHolder: BaseViewHolder,
        viewType: Int
    ) {
        super.onItemViewHolderCreated(viewHolder, viewType)
        delegate?.onItemViewHolderCreated(viewHolder, viewType)
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        delegate?.onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun convert(holder: BaseViewHolder, item: T) {
        delegate?.convert(holder, item)
    }
}

/**
 * 快速创建BaseQuickAdapter
 * 内置多种常用配置
 */
fun <T> quickAdapter(
    layoutResId: Int,
    data: MutableList<T>? = null,
    animationEnable: Boolean = false,
    block: (BaseQuickAdapter<T, BaseViewHolder>.() -> Unit)? = null
): AdapterDelegator<T> {

    return object : AdapterDelegator<T> {
        override fun create(delegate: AdapterDelegate<T>?): BaseQuickAdapter<T, BaseViewHolder> {
            val loadMoreEnable: Boolean = delegate is LoadMoreModule
            val draggableEnable: Boolean = delegate is DraggableModule
            val upFetchModuleEnable: Boolean = delegate is UpFetchModule

            //通过AdapterDelegate实现的接口，自动创建对应的adapter
            val delegateAdapter = if (loadMoreEnable && draggableEnable && upFetchModuleEnable) {
                //同时支持加载更多，拖拽，向上加载更多
                object : BaseModifyAdapter<T>(layoutResId, data), LoadMoreModule,
                    DraggableModule, UpFetchModule {}
            } else if (loadMoreEnable && draggableEnable) {
                //同时支持加载更多，拖拽
                object : BaseModifyAdapter<T>(layoutResId, data), LoadMoreModule, DraggableModule {}
            } else if (draggableEnable && upFetchModuleEnable) {
                //同时支持拖拽，向上加载更多
                object : BaseModifyAdapter<T>(layoutResId, data), DraggableModule, UpFetchModule {}
            } else if (loadMoreEnable && upFetchModuleEnable) {
                //同时支持加载更多，向上加载更多
                object : BaseModifyAdapter<T>(layoutResId, data), LoadMoreModule, UpFetchModule {}
            } else if (loadMoreEnable) {
                //只支持加载更多
                object : BaseModifyAdapter<T>(layoutResId, data), LoadMoreModule {}
            } else if (draggableEnable) {
                //只支持拖拽
                object : BaseModifyAdapter<T>(layoutResId, data), DraggableModule {}
            } else if (upFetchModuleEnable) {
                //只支持向上加载更多
                object : BaseModifyAdapter<T>(layoutResId, data), UpFetchModule {}
            } else {
                object : BaseModifyAdapter<T>(layoutResId, data) {}
            }

            if (delegateAdapter is LoadMoreModule) {
                //adapter有加载更多模块，设置加载更多配置
                delegateAdapter.loadMoreModule.apply {
                    isAutoLoadMore = true
                    isEnableLoadMoreIfNotFullPage = true
                    enableLoadMoreEndClick = false
                    setOnLoadMoreListener {
                        delegate?.onLoadMore()
                    }
                }
            }

            //adapter有拖拽模块，设置拖拽配置
            if (delegateAdapter is DraggableModule) {
                delegateAdapter.draggableModule.apply {
                    if (delegate is OnItemDragListener) {
                        setOnItemDragListener(delegate)
                    }
                }
                delegateAdapter.draggableModule.apply {
                    if (delegate is OnItemSwipeListener) {
                        setOnItemSwipeListener(delegate)
                    }
                }
            }

            delegateAdapter.animationEnable = animationEnable
            //提供给外部设置配置，配置可覆盖
            block?.invoke(delegateAdapter)
            delegateAdapter.setAdapterDelegate(delegate)

            return delegateAdapter
        }
    }
}


/**
 * 创建DataBindingAdapter
 */
fun <T : DataEntity, B : ViewDataBinding> AdapterDelegator<T>.asDataBinding(
    dateVariableId: Int,
    config: DataBindingConfig? = null,
    loadMoreEnable: Boolean = true,
): BaseQuickAdapter<T, BaseViewHolder> {
    val delegate = if (loadMoreEnable) {
        object : DataBindingAdapterDelegate<T, B>(dateVariableId, config), LoadMoreModule {
        }
    } else {
        object : DataBindingAdapterDelegate<T, B>(dateVariableId, config) {}
    }
    return this.create(delegate)
}

/**
 * 覆盖quickAdapter中的配置
 */
fun <T> BaseQuickAdapter<T, BaseViewHolder>.loadMore(block: () -> Unit): BaseQuickAdapter<T, BaseViewHolder> {
    if (this is LoadMoreModule) {
        this.loadMoreModule.setOnLoadMoreListener(block)
    }
    return this
}

/**
 * 和RecyclerView绑定，并设置渲染等生命周期的回调
 */
fun <T> AdapterDelegator<T>.attach(
    recyclerView: RecyclerView,
    delegate: AdapterDelegate<T>
): BaseQuickAdapter<T, BaseViewHolder> {
    return this.create(delegate).let {
        recyclerView.adapter = it
        return@let it
    }
}

/**
 * 和RecyclerView绑定，并设置渲染等生命周期的回调
 * 函数式回调
 */
fun <T> AdapterDelegator<T>.attach(
    recyclerView: RecyclerView,
    convert: (BaseViewHolder, T) -> Unit,
    onViewRecycled: ((BaseViewHolder) -> Unit)? = null,
): BaseQuickAdapter<T, BaseViewHolder> {
    return this.attach(recyclerView, object : AdapterDelegate<T>() {
        override fun convert(holder: BaseViewHolder, item: T) {
            convert.invoke(holder, item)
        }

        override fun onViewRecycled(holder: BaseViewHolder) {
            onViewRecycled?.invoke(holder)
        }
    })
}


val BaseQuickAdapter<*, *>.isLoading: Boolean
    get() {
        if (this is LoadMoreModule) {
            return this.loadMoreModule.isLoading
        }
        return false
    }

fun BaseQuickAdapter<*, *>.loadMoreComplete() {
    if (this is LoadMoreModule) {
        if (this.loadMoreModule.isLoading) {
            this.loadMoreModule.loadMoreComplete()
        }
    }
}
