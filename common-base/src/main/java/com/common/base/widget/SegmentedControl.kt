package com.common.base.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.widget.LinearLayoutCompat
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.drawable.BaseDrawable
import com.common.base.drawable.CornerBorderDrawable
import com.common.base.utils.SizeUtils
import com.common.base.utils.ViewUtils


/**
 * 分段选择器 类似iOS的 UISegmentedControl
 */

class SegmentedControl : LinearLayoutCompat {

    //按钮信息
    var buttonTitles: Array<CharSequence>? = null
    set(value) {
        field = value
        initItems()
    }

    //主题颜色
    @ColorInt
    var tintColor = Color.WHITE
    set(value) {
        if(value != field){
            field = value
            _backgroundDrawable.borderColor = tintColor
            if (_items != null) {
                for (item in _items!!) {
                    item.selectedBackgroundDrawable.backgroundColor = tintColor
                    item.normalBackgroundDrawable.color = tintColor
                }
            }
        }
    }

    //字体大小 sp
    var textSize = 14f
    set(value) {
        if(value != field){
            field = value
            if(_items != null){
                for (item in _items!!) {
                    item.textSize = field
                }
            }
        }
    }

    //文字颜色
    @ColorInt
    var textColor = Color.WHITE
        set(value) {
            if(value != field){
                field = value
                if(_items != null){
                    for (item in _items!!) {
                        if(item.position != selectedPosition){
                            item.setTextColor(field)
                        }
                    }
                }
            }
        }
    
    @ColorInt
    var selectedTextColor = Color.BLUE
        set(value) {
            if(value != field){
                field = value
                if(_items != null && _items!!.isNotEmpty() && selectedPosition < _items!!.size){
                    _items!![selectedPosition].setTextColor(field)
                }
            }
        }

    //圆角 px
    var cornerRadius = 0
    set(value) {
        if(value != field){
            field = value
            _backgroundDrawable.setCornerRadius(cornerRadius)
            if (_items != null) {
                for (i in 0 until _items!!.size) {
                    val item = _items!![i]
                    setItemCornerRadius(item, i)
                }
            }
        }
    }

    //边框 px
    var borderWidth = 0
    set(value) {
        if(value != field){
            field = value
            _backgroundDrawable.borderWidth = borderWidth
            if (_items != null) {
                for (item in _items!!) {
                    item.normalBackgroundDrawable.lineWidth = field
                }
            }
        }
    }

    //items
    private var _items: ArrayList<SegmentedItem>? = null

    //背景
    private var _backgroundDrawable = CornerBorderDrawable()

    //点击回调
    var onItemClickListener: ((position: Int) -> Unit)? = null

    //当前选中的
    var selectedPosition = 0
    private set

    //item的 padding
    private var _itemPaddingLeft = 0
    private var _itemPaddingTop = 0
    private var _itemPaddingRight = 0
    private var _itemPaddingBottom = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){

        orientation = HORIZONTAL
        cornerRadius = SizeUtils.pxFormDip(5f, context)
        _itemPaddingLeft = SizeUtils.pxFormDip(10f, context)
        _itemPaddingTop = SizeUtils.pxFormDip(5f, context)
        _itemPaddingRight = SizeUtils.pxFormDip(10f, context)
        _itemPaddingBottom = SizeUtils.pxFormDip(5f, context)
        borderWidth = SizeUtils.pxFormDip(1f, context)

        _backgroundDrawable.borderWidth = borderWidth
        _backgroundDrawable.setCornerRadius(cornerRadius)
        _backgroundDrawable.borderColor = tintColor

        ViewUtils.setBackground(_backgroundDrawable, this)
    }

    //设置item 的padding
    fun setItemPadding(left: Int, top: Int, right: Int, bottom: Int, itemIndex: Int) {
        if (_items != null && itemIndex < _items!!.size && itemIndex >= 0) {
            val item = _items!![itemIndex]
            item.setPadding(left, top, right, bottom)
        }
    }

    /**
     * 设置选中
     * @param position 选中的位置
     * @param click 是否是点击，需要回调
     */
    fun setSelectedPosition(position: Int, click: Boolean) {
        if (selectedPosition != position) {
            if (selectedPosition >= 0 && selectedPosition < _items!!.size) {
                val item = _items!![selectedPosition]
                item.setTextColor(textColor)
                item.isSelected = false
            }
            selectedPosition = position
            val item = _items!![selectedPosition]
            item.isSelected = true
            item.setTextColor(selectedTextColor)
            if (click && onItemClickListener != null) {
                onItemClickListener!!(selectedPosition)
            }
        }
    }

    //设置选中
    fun setSelectedPosition(position: Int) {
        setSelectedPosition(position, false)
    }


    //设置item圆角
    private fun setItemCornerRadius(item: SegmentedItem, position: Int) {
        if (buttonTitles!!.size > 1) {
            if (position == 0) {
                item.selectedBackgroundDrawable.leftTopCornerRadius = cornerRadius
                item.selectedBackgroundDrawable.leftBottomCornerRadius = cornerRadius
            } else if (position == buttonTitles!!.size - 1) {
                item.selectedBackgroundDrawable.rightTopCornerRadius = cornerRadius
                item.selectedBackgroundDrawable.rightBottomCornerRadius = cornerRadius
            }
        } else {
            item.selectedBackgroundDrawable.setCornerRadius(cornerRadius)
        }
    }

    //创建UI
    private fun initItems() {
        removeAllViews()
        if (buttonTitles == null || buttonTitles!!.isEmpty()) return

        selectedPosition = 0
        val context: Context = context
        _items = ArrayList(buttonTitles!!.size)
        val count = buttonTitles!!.size

        for (i in 0 until count) {
            val item = SegmentedItem(context)
            item.position = i
            item.text = buttonTitles!![i]
            item.textSize = textSize
            item.selectedBackgroundDrawable.backgroundColor = tintColor

            setItemCornerRadius(item, i)
            item.isSelected = i == selectedPosition
            item.normalBackgroundDrawable.lineWidth = borderWidth
            item.normalBackgroundDrawable.color = tintColor
            item.setPadding(_itemPaddingLeft, _itemPaddingTop, _itemPaddingRight, _itemPaddingBottom)

            if (count > 2) {
                item.normalBackgroundDrawable.exist = i < count - 1
            }
            item.setTextColor(if (item.isSelected) selectedTextColor else textColor)
            val params =
                LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT)
            params.weight = 1f
            item.layoutParams = params
            addView(item)
            _items!!.add(item)
            item.setOnClickListener(object : OnSingleClickListener() {
                override fun onSingleClick(v: View) {
                    val segmentedItem = v as SegmentedItem
                    setSelectedPosition(segmentedItem.position, true)
                }
            })
        }
    }

    //分段选择 item
    private class SegmentedItem(context: Context) : FontTextView(context) {

        //正常drawable
        var normalBackgroundDrawable = NormalBackgroundDrawable()

        //选中
        var selectedBackgroundDrawable = CornerBorderDrawable()

        //下标
        var position = 0
        
        init {
            gravity = Gravity.CENTER
            val stateListDrawable = StateListDrawable()
            selectedBackgroundDrawable.backgroundColor = Color.WHITE

            //state_selected 和 state_pressed一起会冲突
            stateListDrawable.addState(intArrayOf(android.R.attr.state_selected), selectedBackgroundDrawable)

            normalBackgroundDrawable = NormalBackgroundDrawable()
            stateListDrawable.addState(intArrayOf(), normalBackgroundDrawable)

            ViewUtils.setBackground(stateListDrawable, this)
        }
    }

    //正常背景
    private class NormalBackgroundDrawable : BaseDrawable() {
        
        //是否存在右边线条
        var exist = false
            set(value) {
                if (field != value) {
                    field = value
                    invalidateSelf()
                }
            }
        
        //颜色
        var color = 0
            set(value) {
                if (field != value) {
                    field = value
                    invalidateSelf()
                }
            }
        
        //线条宽度
        var lineWidth = 0
            set(value) {
                if (field != value) {
                    field = value
                    invalidateSelf()
                }
            }

        override fun copy(): BaseDrawable {
            val drawable = NormalBackgroundDrawable()
            drawable.exist = exist
            drawable.color = color
            drawable.lineWidth = lineWidth
            return drawable
        }

        override fun draw(canvas: Canvas) {
            if (exist) {
                paint.color = color
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = lineWidth.toFloat()

                val right = rectF.right - lineWidth / 2
                canvas.drawLine(right, 0f, right, rectF.bottom, paint)
            }
        }
    }

    //回调
    interface OnItemClickListener {
        //点击item
        fun onItemClick(position: Int)
    }
}