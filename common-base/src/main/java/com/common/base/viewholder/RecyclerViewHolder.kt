package com.common.base.viewholder

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * recyclerView holder
 */
@Suppress("unchecked_cast")
class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    constructor(binding: ViewDataBinding): this(binding.root){
        this.binding = binding
    }

    lateinit var binding: ViewDataBinding
        private set

    //保存view的集合
    private var mViews: SparseArray<View>? = null

    /**
     * 通过id 获取view
     * @param id view的id
     * @param <T> 视图类型
     * @return 对应的view </T>
     * */
    fun <T : View> getView(@IdRes id: Int): T {
        var view: T? = null
        if (mViews == null) {
            mViews = SparseArray()
        } else {
            view = mViews!![id] as T?
        }
        if (view == null) {
            view = itemView.findViewById<View>(id) as T
            mViews!!.put(id, view)
        }
        return view
    }
}