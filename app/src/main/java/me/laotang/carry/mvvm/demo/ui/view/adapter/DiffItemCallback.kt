package me.laotang.carry.mvvm.demo.ui.view.adapter

import androidx.recyclerview.widget.DiffUtil
import me.laotang.carry.mvvm.entity.DataEntity

/**
 * DataEntity为确定是kotlin的data类才去实现
 */
class DiffDataItemCallback<T : DataEntity> : DiffUtil.ItemCallback<T>() {
    /**
     * 判断是否是同一个item
     * 使用的是kotlin的Data类，直接比较即可
     */
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    /**
     * 当是同一个item时，再判断内容是否发生改变
     */
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.equals(newItem)
    }
}