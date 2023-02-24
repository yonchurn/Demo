package com.common.base.viewholder

import android.util.SparseArray
import android.view.View
import androidx.annotation.IdRes
import com.common.base.R

/**
 * 视图存储器,主要用于保存视图的子视图,不需要每次获取的时候都通过 findViewById来获取
 */
object ViewHolder {

    /**
     * 通过id获取视图
     * @param rootView 根视图
     * @param id 视图id
     * @param <T> 泛型,返回值必须是 View或者其子类
     * @return 子视图
    </T> */
    @Suppress("unchecked_cast")
    operator fun <T : View> get(rootView: View, @IdRes id: Int): T {

        //保存子视图的集合
        var holder = rootView.getTag(R.id.view_holder_tag_key) as SparseArray<View>?
        if (holder == null) {

            //创建集合并关联rootView
            holder = SparseArray<View>()
            rootView.setTag(R.id.view_holder_tag_key, holder)
        }
        var childView: View? = holder[id]
        if (childView == null) {

            //获取子类并保存
            childView = rootView.findViewById(id)
            holder.put(id, childView)
        }
        return childView as T
    }
}