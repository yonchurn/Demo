package com.common.base.app

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.common.base.R
import com.common.base.adapter.AbsListViewAdapter
import com.common.base.base.activity.ListActivity
import com.common.base.base.widget.BaseContainer
import com.common.base.extension.setOnSingleListener
import com.common.base.utils.AppUtils
import com.common.base.utils.StringUtils
import com.common.base.utils.ToastUtils
import com.common.base.viewholder.ViewHolder

/**
 * 环境选择
 */
class EnvironmentPickerActivity: ListActivity() {

    //接口地址
    private val domainEditText by lazy {findViewById<EditText>(R.id.domain_edit_text)}

    //uuid
    private val uuidEditText by lazy {findViewById<EditText>(R.id.uuid_edit_text)}

    override fun initialize(
        inflater: LayoutInflater,
        container: BaseContainer,
        saveInstanceState: Bundle?
    ) {
        super.initialize(inflater, container, saveInstanceState)

        setTopView(R.layout.environment_picker_header)
        findViewById<Button>(R.id.confirm_button).setOnClickListener {
            val text = domainEditText.text.toString()
            if (StringUtils.isEmpty(text)) {
                ToastUtils.showToast("地址不能为空")
            }else if (!(text.startsWith("http") || text.startsWith("https"))) {
                ToastUtils.showToast("要以http/https开头")
            }else {
                ApiConfig.customApi = text
                val uuid = uuidEditText.text.toString()
                if (StringUtils.isNotEmpty(uuid)) {
                    AppUtils.setCustomDeviceId(uuid)
                }
                finish(RESULT_OK)
            }
        }
        setBarTitle("环境选择")
        listView.adapter = Adapter()
        initRadioGroup()
    }

    override fun showBackItem(): Boolean {
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ToastUtils.showToast("请选择环境")
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    private inner class Adapter: AbsListViewAdapter() {

        override fun getView(
            position: Int,
            section: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            val view = convertView ?:
            LayoutInflater.from(this@EnvironmentPickerActivity).inflate(R.layout.environment_picker_list_item,
                parent, false)

            val title: String = when(position){
                ApiConfig.Type.IP_TEST.ordinal -> "广州IP测试"
                ApiConfig.Type.DOMAIN_TEST.ordinal -> "广州域名测试"
                ApiConfig.Type.PRE.ordinal -> "缅甸预生产"
                ApiConfig.Type.GRAY.ordinal -> "缅甸灰度"
                ApiConfig.Type.PRO.ordinal -> "缅甸正式"
                else -> ""
            }

            ViewHolder.apply {
                get<TextView>(view, R.id.title).text = title
                get<TextView>(view, R.id.subtitle).text = ApiConfig.apiForIndex(position)
            }

            return view
        }

        override fun numberOfItems(section: Int): Int {
            return ApiConfig.Type.PRO.ordinal + 1
        }

        override fun onItemClick(positionInSection: Int, section: Int, item: View) {
            ApiConfig.setDebugType(positionInSection)
            val uuid = uuidEditText.text.toString()
            if (StringUtils.isNotEmpty(uuid)) {
                AppUtils.setCustomDeviceId(uuid)
            }
            finish(RESULT_OK)
        }
    }

    private fun initRadioGroup(){
        val rbWeb = findViewById<RadioButton>(R.id.rb_web)
        val rbGoogle = findViewById<RadioButton>(R.id.rb_google)
        val rbHuawei = findViewById<RadioButton>(R.id.rb_huawei)
        val rbOppo = findViewById<RadioButton>(R.id.rb_oppo)
        val rbVivo = findViewById<RadioButton>(R.id.rb_vivo)
        val rbXiaomi = findViewById<RadioButton>(R.id.rb_xiaomi)
        rbWeb.setOnSingleListener {
            setCustomChannelAndRadioButton(rbWeb)
        }
        rbGoogle.setOnSingleListener {
            setCustomChannelAndRadioButton(rbGoogle)
        }
        rbHuawei.setOnSingleListener {
            setCustomChannelAndRadioButton(rbHuawei)
        }
        rbOppo.setOnSingleListener {
           setCustomChannelAndRadioButton(rbOppo)
        }
        rbVivo.setOnSingleListener {
            setCustomChannelAndRadioButton(rbVivo)
        }
        rbXiaomi.setOnSingleListener {
            setCustomChannelAndRadioButton(rbXiaomi)
        }
    }

    private fun setCustomChannelAndRadioButton(radioButTon: RadioButton){
        val channel = when(radioButTon.id){
            R.id.rb_web -> PackingChannel.WEB
            R.id.rb_google -> PackingChannel.GOOGLE_PLAY
            R.id.rb_huawei -> PackingChannel.HUA_WEI
            R.id.rb_oppo -> PackingChannel.OPPO
            R.id.rb_vivo -> PackingChannel.VIVO
            R.id.rb_xiaomi -> PackingChannel.XIAO_MI
            else -> null
        }

        AppVersionManager.customChannel = channel

        val llChannel1 = findViewById<LinearLayout>(R.id.ll_channel1)
        val llChannel2 = findViewById<LinearLayout>(R.id.ll_channel2)
        for (i in 0..llChannel1.childCount){
            val child = llChannel1.getChildAt(i)
            if (child is RadioButton){
                child.isChecked = child == radioButTon
            }
        }

        for (i in 0..llChannel2.childCount){
            val child = llChannel2.getChildAt(i)
            if (child is RadioButton){
                child.isChecked = child == radioButTon
            }
        }
    }
}