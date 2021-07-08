package me.laotang.carry.mvvm.demo.ui.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.functions.Consumer
import me.laotang.carry.core.json.JsonConverter
import me.laotang.carry.mvvm.demo.BR
import me.laotang.carry.mvvm.demo.R
import me.laotang.carry.mvvm.demo.databinding.ActivityMainBinding
import me.laotang.carry.mvvm.demo.databinding.ItemUserBinding
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.ui.store.MainViewActionCreator
import me.laotang.carry.mvvm.demo.ui.store.MainViewModel
import me.laotang.carry.mvvm.demo.util.DiffDataItemCallback
import me.laotang.carry.mvvm.view.BaseDataBindActivity
import me.laotang.carry.mvvm.view.DataBindingConfig
import me.laotang.carry.util.clearLoadImageRequest
import me.laotang.carry.util.toasty
import javax.inject.Inject

/**
 * 通过dataBinding实现UI的数据驱动
 * viewModel作为Store使用，view层的数据要与viewModel中保持一致，除了某些数据比如滑动到了某个位置等状态
 * 可以通过viewModel中的数据，恢复view层的UI
 */
@AndroidEntryPoint
class MainActivity : BaseDataBindActivity<ActivityMainBinding>() {

    @Inject
    lateinit var jsonConverter: JsonConverter

    private val storeViewModel by viewModels<MainViewModel>()

    private lateinit var mAdapter: BaseQuickAdapter<User, BaseViewHolder>

    private val listenerHandler: ListenerHandler by lazy {
        ListenerHandler(storeViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutManager = FlexboxLayoutManager(this).apply {
            justifyContent = JustifyContent.SPACE_BETWEEN
        }

        mAdapter = object :
            BaseQuickAdapter<User, BaseViewHolder>(R.layout.item_user),
            LoadMoreModule {

            override fun onItemViewHolderCreated(
                viewHolder: BaseViewHolder,
                viewType: Int
            ) {
                DataBindingUtil.bind<ItemUserBinding>(viewHolder.itemView);
            }

            override fun convert(holder: BaseViewHolder, item: User) {
                holder.getBinding<ItemUserBinding>()?.let {
                    it.user = item
                    it.executePendingBindings()
                }
            }

            override fun onViewRecycled(holder: BaseViewHolder) {
                super.onViewRecycled(holder)
                holder.getBinding<ItemUserBinding>()?.ivUser?.clearLoadImageRequest()
            }
        }
        mAdapter.loadMoreModule.apply {
            isAutoLoadMore = true
            isEnableLoadMoreIfNotFullPage = true
            enableLoadMoreEndClick = false
            preLoadNumber = 2
            setOnLoadMoreListener {
                listenerHandler.loadUser(true)
            }
        }
        mAdapter.animationEnable = true
        mAdapter.setDiffCallback(DiffDataItemCallback())

        binding.recyclerView.let {
            it.layoutManager = layoutManager
            it.adapter = mAdapter
        }

        //订阅user的load数据
        storeViewModel.usersLiveData.observe(this, {
            if (mAdapter.loadMoreModule.isLoading) {
                mAdapter.loadMoreModule.loadMoreComplete()
            }
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false
            }
            mAdapter.setDiffNewData(it.toMutableList())
        })

        //activity第一次onCreate,请求后台
        if (savedInstanceState == null) {
            //liveData响应生命周期不一定及时，通过view.post来确保liveData的订阅有效
            binding.root.post {
                listenerHandler.loadUser(false)
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(BR.vm, storeViewModel)
            .addBindingParam(BR.listener, listenerHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    /**
     * MainActivity的UI响应事件（如点击），单独拎出来，通过dataBinding实现绑定，交互的数据由viewModel层提供
     * 处理和model层有交互的事件
     * 进一步保证view层的UI显示由viewModel中的数据来驱动
     */
    class ListenerHandler(private val storeViewModel: MainViewModel) {

        //下拉刷新
        val onRefresh = Consumer<Unit> {
            loadUser(loadMore = false, showLoading = false)
        }

        fun loadUser(loadMore: Boolean, showLoading: Boolean = true) {
            val lastIdQueried = storeViewModel.lastIdQueried.toIntOrNull()
            if (lastIdQueried == null) {
                "Id不能为空".toasty()
                return
            }
            storeViewModel.dispatcher.dispatch(
                MainViewActionCreator.load(
                    lastIdQueried,
                    loadMore,
                    showLoading
                )
            )
        }
    }
}