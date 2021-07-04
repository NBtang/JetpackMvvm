package me.laotang.carry.mvvm.demo.ui.view

import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.functions.Consumer
import me.laotang.carry.core.json.JsonConverter
import me.laotang.carry.mvvm.demo.R
import me.laotang.carry.mvvm.demo.BR
import me.laotang.carry.mvvm.demo.databinding.ActivityMainBinding
import me.laotang.carry.mvvm.demo.ui.action.MainViewActionCreator
import me.laotang.carry.mvvm.demo.ui.store.MainViewModel
import me.laotang.carry.mvvm.view.BaseDataBindActivity
import me.laotang.carry.mvvm.view.DataBindingConfig
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

    private val viewModel by viewModels<MainViewModel>()

    private val listenerHandler: ListenerHandler by lazy {
        ListenerHandler(viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            //liveData响应生命周期不一定及时，通过view.post来确保liveData的订阅有效
            binding.root.post {
                viewModel.dispatcher.dispatch(MainViewActionCreator.load(0))
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_main

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(BR.vm, viewModel)
            .addBindingParam(BR.listener, listenerHandler)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    /**
     * MainActivity的UI响应事件（如点击），单独拎出来，通过dataBinding实现绑定，交互的数据由viewModel层提供
     * MainActivity处理其他和model层不交互的UI事件
     * 进一步保证view层的UI显示由viewModel中的数据来驱动
     */
    class ListenerHandler(private val store: MainViewModel) {
        val userInfoRequestConsumer: Consumer<Unit> by lazy {
            Consumer<Unit> {
                val lastIdQueried = store.lastIdQueried.toIntOrNull()
                if (lastIdQueried == null) {
                    "Id不能为空".toasty()
                    return@Consumer
                }
                store.dispatcher.dispatch(MainViewActionCreator.load(lastIdQueried))
            }
        }
    }
}