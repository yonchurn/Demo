package com.common.base.menu

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.common.base.R
import com.common.base.base.constant.Position
import com.common.base.drawable.DrawableUtils
import com.common.base.extension.setOnSingleListener
import com.common.base.utils.SizeUtils
import com.common.base.widget.FontTextView

/**
 * 按钮信息
 */
class ConditionEntity(val title: String, val icon: Drawable, val selectedIcon: Drawable) {

    //显示的标题
    var displayTitle: String? = null
        get() = field ?: title
}

/**
 * 菜单按钮
 */
class ConditionMenuBarItem: FrameLayout {

    val textView by lazy {
        val textView = FontTextView(context)
        textView.maxLines = 1
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.compoundDrawablePadding = SizeUtils.pxFromSp(5f, context)
        textView.gravity = Gravity.CENTER
        textView
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        addView(textView, params)
    }
}

/**
 * 条件菜单
 */
class ConditionMenuBar: LinearLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setWillNotDraw(false)
        onColorChange()
    }

    //按钮
     var items: List<ConditionMenuBarItem>? = null

    //字号
    var textSize = 14f
        set(value) {
            if (value != field) {
                field = value
                items?.also {
                    for (i in it.indices) {
                        val item = it[i]
                        item.textView.textSize = field
                    }
                }
            }
        }

    //颜色
    var color = ContextCompat.getColor(context, R.color.black_text_color)
        set(value) {
            if (value != field) {
                field = value
                onColorChange()
            }
        }

    //选中颜色
    var selectedColor = ContextCompat.getColor(context, R.color.theme_color)
        set(value) {
            if (value != field) {
                field = value
                onColorChange()
            }
        }

    //指示器颜色
    var indicatorColor = ContextCompat.getColor(context, R.color.theme_color)


    private lateinit var colorStateList: ColorStateList

    //间隔
    var spacing = SizeUtils.pxFromSp(10f, context)
        set(value) {
            if (field != value) {
                field = value
                items?.also {
                    for (i in it.indices) {
                        val item = it[i]
                        val params = item.layoutParams as LayoutParams
                        if (i != it.size - 1) {
                            params.marginEnd = field
                        }
                    }
                }
            }
        }


    //选中的下标
    var selectedPosition = Position.NO_POSITION
        set(value) {
            if (value != field) {
                settingItem(field, false)
                field = value
                settingItem(field, true)
            }
        }

    //回调
    var callback: Callback? = null

    //按钮信息
    var entities: List<ConditionEntity>? = null
        set(value) {
            if (value != field) {
                field = value
                initItems()
            }
        }

    //初始化按钮
    private fun initItems() {
        removeAllViews()
        if (!entities.isNullOrEmpty()) {
            entities?.also {
                val items = ArrayList<ConditionMenuBarItem>()
                for (i in it.indices) {
                    val item = ConditionMenuBarItem(context)
                    item.tag = i
                    item.setOnSingleListener { view ->
                        val position = view.tag as Int
                        callback?.also { callback ->
                            if (position == selectedPosition) {
                                callback.onSelectHighlightedItem(this, position)
                            } else {
                                selectedPosition = position
                                callback.onSelectItem(this, position)
                            }
                        }
                    }

                    settingItem(item, it[i], i == selectedPosition)

                    val params = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
                    params.weight = 1f
                    if (i != it.size - 1) {
                        params.marginEnd = spacing
                    }
                    addView(item, params)
                    items.add(item)
                }
                this.items = items
            }
        }
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String, position: Int) {
        if (isPositionValid(position)) {
            val entity = entities!![position]
            entity.displayTitle = title
            val item = items!![position]
            item.textView.text = entity.displayTitle
        }
    }

    //取消选中
    fun unselect() {
        selectedPosition = Position.NO_POSITION
    }

    //判断是否有效
    private fun isPositionValid(position: Int): Boolean {
        return !items.isNullOrEmpty() && position >= 0 && position < items!!.size
    }

    private fun settingItem(position: Int, selected: Boolean) {
        if (isPositionValid(position)) {
            settingItem(items!![position], entities!![position], selected)
        }
    }

    //设置按钮
    private fun settingItem(item: ConditionMenuBarItem, entity: ConditionEntity, selected: Boolean) {
        item.textView.apply {
            setTextColor(colorStateList)
            setCompoundDrawables(null, null, getDrawable(entity), null)
            text = entity.displayTitle
        }
        item.isSelected = selected
    }

    //颜色改变了
    private fun onColorChange() {
        val states = arrayOfNulls<IntArray>(2)
        states[0] = intArrayOf(android.R.attr.state_selected)
        states[1] = intArrayOf()
        val colors = intArrayOf(selectedColor, color)
        colorStateList = ColorStateList(states, colors)

        items?.also {
            for (i in it.indices) {
                settingItem(it[i], entities!![i], i == selectedPosition)
            }
        }
    }

    //获取drawable
    private fun getDrawable(entity: ConditionEntity): Drawable {
        val stateListDrawable = StateListDrawable()

        val drawable = DrawableUtils.getTintDrawable(entity.icon, color)
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        drawable.setBounds(0, 0, width, height)

        val selectedDrawable = DrawableUtils.getTintDrawable(entity.selectedIcon, selectedColor)
        selectedDrawable.setBounds(0, 0, width, height)

        stateListDrawable.addState(intArrayOf(android.R.attr.state_selected), selectedDrawable)
        stateListDrawable.addState(intArrayOf(), drawable)

        stateListDrawable.setBounds(0, 0, width, height)

        return stateListDrawable
    }

    val paint by lazy {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = indicatorColor
        paint.strokeWidth = SizeUtils.pxFormDip(3f, context).toFloat()
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE
        paint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isInEditMode || items.isNullOrEmpty())
            return

        //绘制选中的线
        if (selectedPosition != Position.NO_POSITION) {
            val item = items!![selectedPosition]
            val saveCount = canvas.saveCount

            val y = (item.textView.bottom + SizeUtils.pxFormDip(5f, context)).toFloat()
            val x1 = (item.left + item.textView.left).toFloat()

            val drawable = item.textView.compoundDrawables[2]
            val drawableWidth = if (drawable != null) drawable.intrinsicWidth + item.textView.compoundDrawablePadding else 0
            val x2 = (item.left + item.textView.right - drawableWidth).toFloat()

            canvas.drawLine(x1, y, x2, y, paint)
            canvas.restoreToCount(saveCount)
        }
    }

    /**
     * 回调
     */
    interface Callback {

        //选中某个按钮
        fun onSelectItem(menuBar: ConditionMenuBar, position: Int)

        //重复选中某个
        fun onSelectHighlightedItem(menuBar: ConditionMenuBar, position: Int)
    }
}