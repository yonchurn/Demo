package com.common.base.dialog

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.common.base.R
import com.common.base.base.activity.ActivityLifeCycleManager
import com.common.base.base.widget.OnSingleClickListener
import com.common.base.drawable.CornerBorderDrawable
import com.common.base.utils.SizeUtils
import com.common.base.utils.StringUtils
import com.common.base.utils.ViewUtils

/**
 * 信息弹窗fragment
 */
class AlertDialogFragment(style: AlertStyle = AlertStyle.ALERT,
                          title: String? = null,
                          subtitle: String? = null,
                          icon: Drawable? = null,
                          cancelButtonTitle: String? = null,
                          buttonTitles: Array<String>?): BaseDialogFragment(), View.OnClickListener{

    //弹窗样式
    private var _style = AlertStyle.ALERT

    //弹窗属性
    private var _props: AlertProps? = null
    var alertProps: AlertProps?
        set(value) {
            _props = value
        }
        get() = _props
    private val props: AlertProps
        get() = _props!!

    //内容视图
    private var _contentView: View? = null

    //标题
    var _title: String? = null

    //副标题
    private var _subtitle: String? = null

    //图标
    private var _icon: Drawable? = null

    //按钮信息
    private var _buttonTitles: Array<String>? = null

    //点击按钮后弹窗是否消失
    var shouldDismissAfterClickItem = true

    //点击某个按钮 从左到右，从上到下
    var onItemClick: ((position: Int) -> Unit)? = null

    //适配器
    var adapter: AlertDialogAdapter? = null

    //是否需要计算内容高度 当内容或者按钮数量过多时可设置，防止内容显示不完
    var shouldMeasureContentHeight = false

    private val logoImageView: ImageView by lazy { findViewById(R.id.logoImageView) }
    private val titleTextView: TextView by lazy { findViewById(R.id.titleTextView) }
    private val subtitleTextView: TextView by lazy { findViewById(R.id.subtitleTextView) }
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recyclerView) }

    private val scrollContainer: LinearLayout by lazy { findViewById(R.id.scrollContainer) }
    private val scrollView: ScrollView by lazy { findViewById(R.id.scrollView) }
    private val divider: View by lazy { findViewById(R.id.divider) }

    //actionSheet专用
    private val topTransparentView: View by lazy { findViewById(R.id.topTransparentView) }
    private val topContainer: View by lazy { findViewById(R.id.topContainer) }
    private val cancelTextView: TextView by lazy { findViewById(R.id.cancelTextView) }
    private var cancelButtonTitle: String? = null

    init {

        _style = style
        _title = title
        _subtitle = subtitle
        _icon = icon
        _buttonTitles = buttonTitles

        this.cancelButtonTitle = cancelButtonTitle
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(_props == null){
            _props = AlertProps.build(context)
        }
    }

    override fun getContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(_contentView == null){
            _contentView = inflater.inflate(if (_style == AlertStyle.ALERT) R.layout.alert_dialog else R.layout.action_sheet_dialog, container, false)
            initViews()
        }

        return _contentView!!
    }

    ///获取子视图
    private fun <T : View> findViewById(resId: Int): T {
        return _contentView!!.findViewById(resId)
    }

    //初始化视图
    private fun initViews() {

        if (_buttonTitles.isNullOrEmpty() && _style == AlertStyle.ALERT) {
            if (cancelButtonTitle == null) {
                cancelButtonTitle = getString(R.string.cancel)
            }
            _buttonTitles = arrayOf(cancelButtonTitle!!)
        }

        if (_icon != null) {
            logoImageView.setImageDrawable(_icon)
            logoImageView.setPadding(0, props.contentVerticalSpace, 0, 0)
        } else {
            logoImageView.visibility = View.GONE
        }

        if (_title == null) {
            titleTextView.visibility = View.GONE
        } else {
            titleTextView.setTextColor(props.titleColor)
            titleTextView.textSize = props.titleSize
            titleTextView.text = _title
            titleTextView.setPadding(0, props.contentVerticalSpace, 0, 0)
        }

        if (_subtitle == null) {
            subtitleTextView.visibility = View.GONE
        } else {
            subtitleTextView.setTextColor(props.subtitleColor)
            subtitleTextView.textSize = props.subtitleSize
            subtitleTextView.text = _subtitle
            subtitleTextView.setPadding(0, props.contentVerticalSpace, 0, 0)
        }

        //actionSheet 样式不一样
        if (_style == AlertStyle.ACTION_SHEET) {

            if (cancelButtonTitle != null) {
                cancelTextView.apply {
                    setOnClickListener(this@AlertDialogFragment)
                    textSize = props.buttonTextSize
                    setTextColor(props.buttonTextColor)
                    setPadding(
                        props.buttonLeftRightPadding, props.buttonTopBottomPadding,
                        props.buttonLeftRightPadding, props.buttonTopBottomPadding
                    )
                }
                setBackgroundSelector(cancelTextView)
            } else {
                cancelTextView.visibility = View.GONE
            }

            setBackground(topContainer)
            topTransparentView.setOnClickListener(this)
            val has = hasTopContent()

            //隐藏顶部分割线 没有按钮也隐藏
            if (!has || _buttonTitles == null || _buttonTitles!!.isEmpty()) {
                divider.visibility = View.GONE
            }
            val top = if (has) props.contentPadding - props.contentVerticalSpace else 0
            val bottom = if (has) props.contentPadding else 0
            scrollContainer.setPadding(0, top, 0, bottom)
        } else {
            scrollContainer.setPadding(0, props.contentPadding - props.contentVerticalSpace, 0, props.contentPadding)
            _contentView!!.setBackgroundColor(props.backgroundColor)
            setBackground(_contentView!!)
        }

        if (shouldMeasureContentHeight || props.contentMinHeight > 0) {
            measureContentHeight()
        }
        val spanCount = if (_buttonTitles?.size != 2 || _style == AlertStyle.ACTION_SHEET) 1 else 2
        val layoutManager = GridLayoutManager(context, spanCount)
        layoutManager.orientation = GridLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager

        if (_buttonTitles != null && _buttonTitles!!.size > 1) { //添加分割线
            recyclerView.addItemDecoration(ItemDecoration())
        }

        recyclerView.adapter = Adapter()

        setStyle(STYLE_NORMAL, R.style.Theme_dialog_noTitle_noBackground)

        //设置弹窗大小
        val window = dialog?.window
        if(window != null){
            val layoutParams = window.attributes
            layoutParams.gravity = if (_style == AlertStyle.ALERT) Gravity.CENTER else Gravity.BOTTOM
            layoutParams.width = getContentViewWidth()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
            when (_style) {
                AlertStyle.ACTION_SHEET -> {
                    window.decorView.setPadding(props.dialogPadding, props.dialogPadding, props.dialogPadding, props.dialogPadding)
                    window.setWindowAnimations(R.style.action_sheet_animate)
                    isCancelable = cancelButtonTitle != null
                    dialog?.setCanceledOnTouchOutside(cancelButtonTitle != null)
                }
                AlertStyle.ALERT -> {
                    window.decorView.setPadding(0, props.dialogPadding, 0, props.dialogPadding)
                    isCancelable = false
                    dialog?.setCanceledOnTouchOutside(false)
                }
            }
            window.attributes = layoutParams
        }
    }

    //计算内容高度
    private fun measureContentHeight() { //按钮内容高度

        var buttonContentHeight = 0

        //顶部内容高度
        var topContentHeight = 0

        //取消按钮高度 STYLE_ALERT 为 0
        var cancelButtonHeight = 0

        //图标高度
        if (_icon != null) {
            topContentHeight += _icon!!.intrinsicHeight
            topContentHeight += props.contentVerticalSpace
        }

        val contentWidth = getContentViewWidth()
        if (_title != null || _subtitle != null) { //标题高度
            if (_title != null) {
                topContentHeight += props.contentVerticalSpace
                val params = subtitleTextView.layoutParams as LinearLayout.LayoutParams
                val maxWidth = contentWidth - params.leftMargin - params.rightMargin
                topContentHeight += StringUtils.measureTextHeight(_title, titleTextView.paint, maxWidth)
            }

            //副标题高度
            if (_subtitle != null) {
                topContentHeight += props.contentVerticalSpace
                val params = subtitleTextView.layoutParams as LinearLayout.LayoutParams
                val maxWidth = contentWidth - params.leftMargin - params.rightMargin
                topContentHeight += StringUtils.measureTextHeight(_subtitle, subtitleTextView.paint, maxWidth)
            }
        }

        //内容高度不够
        if (_icon != null || _title != null || _subtitle != null) {
            if (topContentHeight < props.contentMinHeight) {
                val res = props.contentMinHeight - topContentHeight
                val top = props.contentPadding - props.contentVerticalSpace + res / 2
                val bottom = props.contentPadding + res / 2
                scrollContainer.setPadding(0, top, 0, bottom)
            }
        }

        val maxWidth = contentWidth - props.buttonLeftRightPadding * 2
        when (_style) {
            AlertStyle.ACTION_SHEET -> {
                if (hasTopContent()) {
                    topContentHeight += props.contentPadding * 2 - props.contentVerticalSpace
                }
                if (_buttonTitles != null && _buttonTitles!!.isNotEmpty()) {

                    val textView = View.inflate(context, R.layout.alert_button_item, null) as TextView
                    textView.textSize = props.buttonTextSize
                    var i = 0
                    while (i < _buttonTitles!!.size) {
                        val title = _buttonTitles!![i]
                        buttonContentHeight += StringUtils.measureTextHeight(title, textView.paint, maxWidth)
                        + props.buttonTopBottomPadding * 2 + props.dividerHeight
                        i++
                    }
                    buttonContentHeight -= props.dividerHeight
                }

                //取消按钮高度
                cancelButtonHeight += StringUtils.measureTextHeight(cancelTextView.text, cancelTextView.paint, maxWidth)
                + props.buttonTopBottomPadding * 2 + props.dialogPadding
            }
            AlertStyle.ALERT -> {
                topContentHeight += props.contentPadding * 2 - props.contentVerticalSpace
                val textView = View.inflate(context, R.layout.alert_button_item, null) as TextView
                textView.textSize = props.buttonTextSize

                if(_buttonTitles != null){
                    var i = 0
                    while (i < _buttonTitles!!.size) {
                        val title = _buttonTitles!![i]
                        buttonContentHeight += StringUtils.measureTextHeight(
                            title, textView.paint,
                            maxWidth
                        ) + props.buttonTopBottomPadding * 2 + props.dividerHeight
                        if (_buttonTitles!!.size <= 2) break
                        i++
                    }
                }
                if (buttonContentHeight > 0) {
                    buttonContentHeight -= props.dividerHeight
                }
            }
        }
        var maxHeight = SizeUtils.getWindowHeight(requireContext()) - props.dialogPadding * 2 -
                    cancelButtonHeight - scrollContainer.paddingBottom - scrollContainer.paddingTop
        if (divider.visibility == View.VISIBLE) {
            maxHeight -= props.dividerHeight
        }
        if (topContentHeight + buttonContentHeight > maxHeight) { //内容太多了
            when(maxHeight / 2){
                in (buttonContentHeight + 1) until topContentHeight -> {

                    setScrollViewHeight(maxHeight - buttonContentHeight)
                }
                in (topContentHeight + 1) until buttonContentHeight -> {

                    setRecyclerViewHeight(maxHeight - topContentHeight)
                }
                else -> {
                    setRecyclerViewHeight(maxHeight / 2)
                    setScrollViewHeight(maxHeight / 2)
                }
            }
        }
    }

    //设置scrollview
    private fun setScrollViewHeight(height: Int) {
        val params = scrollView.layoutParams
        params.height = height
        scrollView.layoutParams = params
    }

    //设置recyclerView
    private fun setRecyclerViewHeight(height: Int) {
        val params = recyclerView.layoutParams
        params.height = height
        recyclerView.layoutParams = params
    }

    override fun onClick(v: View?) {
        if (v == cancelTextView || v == topTransparentView) {
            if (cancelButtonTitle != null) {
                dismiss()
            }
        }
    }

    //获取内容视图宽度
    private fun getContentViewWidth(): Int {
        return when (_style) {
            AlertStyle.ALERT -> SizeUtils.pxFormDip(280f, requireContext())
            AlertStyle.ACTION_SHEET -> SizeUtils.getWindowWidth(requireContext())
        }
    }

    //是否有头部内容
    private fun hasTopContent(): Boolean {
        return _title != null || _subtitle != null || _icon != null
    }

    //设置背景
    private fun setBackground(view: View?) {
        val drawable = CornerBorderDrawable()
        drawable.setCornerRadius(props.cornerRadius)
        drawable.backgroundColor = props.backgroundColor
        drawable.attachView(view)
    }

    //设置点击效果
    private fun setBackgroundSelector(view: View): Array<CornerBorderDrawable> {
        val stateListDrawable = StateListDrawable()

        val drawablePressed = CornerBorderDrawable()
        drawablePressed.setCornerRadius(props.cornerRadius)
        drawablePressed.backgroundColor = props.selectedBackgroundColor
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), drawablePressed)

        val drawable = CornerBorderDrawable()
        drawable.setCornerRadius(props.cornerRadius)
        drawable.backgroundColor = props.backgroundColor
        stateListDrawable.addState(intArrayOf(), drawable)

        view.isClickable = true
        ViewUtils.setBackground(stateListDrawable, view)
        return arrayOf(drawablePressed, drawable)
    }

    //按钮列表适配器
    private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val itemView = View.inflate(context, R.layout.alert_button_item, null)
            val holder = ViewHolder(itemView, setBackgroundSelector(itemView))
            holder.itemView.setOnClickListener(object : OnSingleClickListener() {

                override fun onSingleClick(v: View) {

                    if (shouldDismissAfterClickItem) {
                        if(onItemClick != null){
                            addOnDismissHandler{
                                onItemClick!!(
                                    holder.bindingAdapterPosition
                                )
                            }
                        }
                        dismiss()
                    } else {
                        if (onItemClick != null) {
                            onItemClick!!(
                                holder.bindingAdapterPosition
                            )
                        }
                    }
                }
            })
            holder.textView.textSize = props.buttonTextSize
            holder.textView.setPadding(
                props.buttonLeftRightPadding, props.buttonTopBottomPadding,
                props.buttonLeftRightPadding, props.buttonTopBottomPadding
            )
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            holder.textView.text = _buttonTitles!![position]

            var color = props.buttonTextColor
            var backgroundColor = props.backgroundColor
            var pressedBackgroundColor = props.selectedBackgroundColor
            var enable = true

            //刷新UI
            if (adapter != null) {
                if (!adapter!!.shouldEnable(this@AlertDialogFragment, position)) {

                    color = props.buttonDisableTextColor
                    enable = false
                } else if (adapter!!.shouldDestructive(this@AlertDialogFragment, position)) {

                    color = props.destructiveButtonTextColor
                    backgroundColor = props.destructiveButtonBackgroundColor
                    pressedBackgroundColor = props.destructiveButtonSelectedBackgroundColor
                }
            }

            holder.itemView.isEnabled = enable
            holder.textView.setTextColor(color)
            holder.drawable.backgroundColor = backgroundColor
            holder.drawablePressed.backgroundColor = pressedBackgroundColor

            //设置点击效果
            if (_style == AlertStyle.ACTION_SHEET || _buttonTitles!!.size != 2) { //垂直

                if (_buttonTitles!!.size == 1 && _style == AlertStyle.ALERT && !hasTopContent()) {

                    holder.drawablePressed.setCornerRadius(props.cornerRadius)
                    holder.drawable.setCornerRadius(props.cornerRadius)

                } else {
                    if (position == 0 && !hasTopContent() && _style == AlertStyle.ACTION_SHEET) {

                        holder.drawablePressed.setCornerRadius(props.cornerRadius, 0, props.cornerRadius, 0)
                        holder.drawable.setCornerRadius(props.cornerRadius, 0, props.cornerRadius, 0)
                    } else if (position == _buttonTitles!!.size - 1) {

                        holder.drawablePressed.setCornerRadius(0, props.cornerRadius, 0, props.cornerRadius)
                        holder.drawable.setCornerRadius(0, props.cornerRadius, 0, props.cornerRadius)
                    } else {
                        holder.drawablePressed.setCornerRadius(0)
                        holder.drawable.setCornerRadius(0)
                    }
                }
            } else { //水平
                if (position == 0) {

                    holder.drawablePressed.setCornerRadius(0, props.cornerRadius, 0, 0)
                    holder.drawable.setCornerRadius(0, props.cornerRadius, 0, 0)
                } else {

                    holder.drawablePressed.setCornerRadius(0, 0, 0, props.cornerRadius)
                    holder.drawable.setCornerRadius(0, 0, 0, props.cornerRadius)
                }
            }
        }

        override fun getItemCount(): Int {
            return if (_buttonTitles != null) _buttonTitles!!.size else 0
        }
    }

    //弹窗按钮
    private class ViewHolder(itemView: View, drawables: Array<CornerBorderDrawable>) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView
        get(){
            return itemView as TextView
        }
        var drawable: CornerBorderDrawable = drawables[1]
        var drawablePressed: CornerBorderDrawable = drawables[0]

        init {
            drawablePressed.setCornerRadius(0)
            drawable.setCornerRadius(0)
        }
    }

    //按钮分割线
    private inner class ItemDecoration : RecyclerView.ItemDecoration() {
        ///分割线
        var divider = ColorDrawable(ContextCompat.getColor(context!!, R.color.divider_color))

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDraw(c, parent, state)

            //绘制按钮分割线
            val count = parent.childCount
            for (i in 0 until count) {
                val child: View = parent.getChildAt(i)
                val position = parent.getChildAdapterPosition(child)
                if (position < _buttonTitles!!.size - 1) { //垂直排列
                    if (_style == AlertStyle.ACTION_SHEET || _buttonTitles!!.size != 2) {
                        divider.setBounds(
                            0,
                            child.bottom,
                            child.right,
                            child.bottom + props.dividerHeight
                        )
                    } else { //水平排列
                        divider.setBounds(
                            child.right,
                            0,
                            child.right + props.dividerHeight,
                            child.bottom
                        )
                    }
                    divider.draw(c)
                }
            }
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            //设置item的偏移量 大小为item+分割线
            val position = parent.getChildAdapterPosition(view)
            if (position < _buttonTitles!!.size - 1) {
                if (_style == AlertStyle.ACTION_SHEET || _buttonTitles!!.size != 2) { //垂直排列
                    outRect.bottom = props.dividerHeight
                } else { //水平
                    outRect.right = props.dividerHeight
                }
            }
        }
    }

    //弹窗适配器
    interface AlertDialogAdapter {

        //该按钮是否具有警示意义 从左到右，从上到下
        fun shouldDestructive(fragment: AlertDialogFragment, position: Int): Boolean{
            return false
        }

        //该按钮是否可以点击 从左到右，从上到下
        fun shouldEnable(fragment: AlertDialogFragment, position: Int): Boolean
    }
}