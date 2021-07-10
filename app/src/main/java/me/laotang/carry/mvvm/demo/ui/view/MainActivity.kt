package me.laotang.carry.mvvm.demo.ui.view

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import me.laotang.carry.mvvm.binding.Command
import me.laotang.carry.mvvm.demo.BR
import me.laotang.carry.mvvm.demo.R
import me.laotang.carry.mvvm.demo.databinding.ActivityMainBinding
import me.laotang.carry.mvvm.demo.databinding.ItemUserBinding
import me.laotang.carry.mvvm.demo.model.entity.User
import me.laotang.carry.mvvm.demo.ui.store.MainViewModel
import me.laotang.carry.mvvm.demo.ui.view.adapter.*
import me.laotang.carry.mvvm.view.BaseDataBindActivity
import me.laotang.carry.mvvm.binding.DataBindingConfig
import me.laotang.carry.mvvm.binding.command
import me.laotang.carry.util.globalEntryPoint
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * 通过dataBinding实现UI的数据驱动
 * viewModel作为Store使用，view层的数据要与viewModel中保持一致，除了某些数据比如滑动到了某个位置等状态
 * 可以通过viewModel中的数据，恢复view层的UI
 */
@AndroidEntryPoint
class MainActivity : BaseDataBindActivity<ActivityMainBinding>() {

    private val storeViewModel by viewModels<MainViewModel>()

    private lateinit var mAdapter: BaseQuickAdapter<User, BaseViewHolder>

    private val listenerHandler: ListenerHandler by lazy {
        ListenerHandler(storeViewModel, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //快速创建dataBindingAdapter，支持加载更多
        mAdapter = quickAdapter<User>(R.layout.item_user)
            .asDataBinding<User, ItemUserBinding>(
                dateVariableId = BR.user,
                config = DataBindingConfig(BR.listener, listenerHandler)
            )
            .loadMore {
                listenerHandler.loadUser(loadMore = true)
            }

        val layoutManager = FlexboxLayoutManager(this).apply {
            justifyContent = JustifyContent.SPACE_BETWEEN
        }
        binding.recyclerView.let {
            it.layoutManager = layoutManager
            it.adapter = mAdapter
        }

        //订阅user的load数据
        storeViewModel.usersLiveData.observe(this, {
            mAdapter.loadMoreComplete()
            binding.refreshLayout.isRefreshing = false
            mAdapter.setDiffNewData(it.toMutableList())
        })

        //activity第一次onCreate,请求后台
        if (savedInstanceState == null) {
            //liveData响应生命周期不一定及时，通过view.post来确保liveData的订阅有效
            binding.root.post {
                listenerHandler.loadUser(loadMore = false)
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    override fun getDataBindingConfig(): DataBindingConfig<ViewModel> {
        return DataBindingConfig<ViewModel>(BR.vm, storeViewModel)
            .addBindingParam(BR.listener, listenerHandler)
    }

    /**
     * MainActivity的UI响应事件（如点击），单独拎出来，通过dataBinding实现绑定，交互的数据由viewModel层提供
     * 处理和model层有交互的事件更加简便，进一步保证view层的UI显示由viewModel中的数据来驱动
     * xml中删除view也不会保持
     */
    class ListenerHandler(private val storeViewModel: MainViewModel, activity: Activity) {

        private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)

        private var isScrolling = false

        //下拉刷新
        val onRefresh: Command<Unit> by command {
            loadUser(loadMore = false, showLoading = false)
        }

        //RecyclerView glide滑动优化
        val onScrollState: Command<Int> by command { state ->
            activityWeakReference.get()?.let { context ->
                val imageLoader = context.globalEntryPoint.imageLoader()
                if (state == RecyclerView.SCROLL_STATE_DRAGGING || state == RecyclerView.SCROLL_STATE_SETTLING) {
                    isScrolling = true
                    imageLoader.pause()
                } else if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isScrolling) {
                        imageLoader.resume()
                    }
                    isScrolling = false
                }
            }
        }

        //头像点击
        val onItemClick:Command<User> by command(hookEnable = true,name = "头像点击") {
            Timber.d(it.toString())
        }

        //获取用户列表
        fun loadUser(loadMore: Boolean, showLoading: Boolean = true) {
            storeViewModel.loadUsers(loadMore = loadMore, showLoading = showLoading)
        }
    }
}