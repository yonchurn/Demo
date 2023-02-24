package com.common.base.tab

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.common.base.R
import com.common.base.utils.SizeUtils
import com.common.base.widget.BadgeValueTextView

/**
 * 标签栏按钮
 */
class TabBarItem : RelativeLayout {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    val imageView: ImageView by lazy { findViewById(R.id.imageView) }
    val textView: TextView by lazy { findViewById(R.id.textView) }

    //角标
    val badgeValueTextView: BadgeValueTextView by lazy{
        val view = BadgeValueTextView(context)

        val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.topMargin = SizeUtils.pxFormDip(-3f, context)
        params.addRule(RIGHT_OF, imageView.id)
        params.leftMargin = SizeUtils.pxFormDip(-5f, context)

        addView(view, params)
        view
    }

    //当前角标
    var badgeValue: String? = null
    set(value) {
        if(value != field){
            field = value
            badgeValueTextView.text = field
        }
    }

    //是否选中
    var checked = false
    set(value) {
        if(value != field){
            field = value
            imageView.isSelected = field
            textView.isSelected = field
        }
    }

    //设置图片和按钮的间隔
    fun setImageTextPadding(padding: Int) {
        val params = textView.layoutParams as LayoutParams
        params.topMargin = padding
    }
}