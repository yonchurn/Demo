package com.common.base.app

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.common.base.api.forEachObject
import com.common.base.api.intValue
import com.common.base.api.stringValue
import com.common.base.language.zawgyi

/**
 * 手机号帮助类
 */
object MobileHelper {

    //手机号规则
    private var mMobileOperatorBeans: List<MobileOperatorBean>? = null
    val mobileOperatorBeans: List<MobileOperatorBean>
        get() {
            if (mMobileOperatorBeans.isNullOrEmpty()) {
                mMobileOperatorBeans = arrayListOf(
                    MobileOperatorBean(
                        "MPT",
                        arrayListOf(
                            MobileRuleBean("0950", 9),
                            MobileRuleBean("0951", 9),
                            MobileRuleBean("0952", 9),
                            MobileRuleBean("0953", 9),
                            MobileRuleBean("0954", 9),
                            MobileRuleBean("0955", 9),
                            MobileRuleBean("0956", 9),
                            MobileRuleBean("0920", 9),
                            MobileRuleBean("0921", 9),
                            MobileRuleBean("0922", 9),
                            MobileRuleBean("0923", 9),
                            MobileRuleBean("0924", 9),
                            MobileRuleBean("0941", 10),
                            MobileRuleBean("0943", 10),
                            MobileRuleBean("0925", 11),
                            MobileRuleBean("0926", 11),
                            MobileRuleBean("0940", 11),
                            MobileRuleBean("0941", 11),
                            MobileRuleBean("0942", 11),
                            MobileRuleBean("0943", 11),
                            MobileRuleBean("0944", 11),
                            MobileRuleBean("0945", 11),
                            MobileRuleBean("0989", 11),
                        )
                    ),
                    MobileOperatorBean(
                        "Ooredoo",
                        arrayListOf(
                            MobileRuleBean("0995", 11),
                            MobileRuleBean("0996", 11),
                            MobileRuleBean("0997", 11),
                        )
                    ),
                    MobileOperatorBean(
                        "Telenor",
                        arrayListOf(
                            MobileRuleBean("0975", 11),
                            MobileRuleBean("0976", 11),
                            MobileRuleBean("0977", 11),
                            MobileRuleBean("0978", 11),
                            MobileRuleBean("0979", 11),
                        )
                    ),
                    MobileOperatorBean(
                        "Mytel",
                        arrayListOf(
                            MobileRuleBean("0966", 11),
                            MobileRuleBean("0967", 11),
                            MobileRuleBean("0968", 11),
                            MobileRuleBean("0969", 11),
                        )
                    ),
                    MobileOperatorBean(
                        "Mectel",
                        arrayListOf(
                            MobileRuleBean("0930", 10),
                            MobileRuleBean("0931", 10),
                            MobileRuleBean("0932", 10),
                            MobileRuleBean("0933", 10),
                            MobileRuleBean("0934", 11),
                            MobileRuleBean("0936", 10),
                        )
                    ),
                    MobileOperatorBean(
                        "CDMA",
                        arrayListOf(
                            MobileRuleBean("0963", 9),
                            MobileRuleBean("0964", 9),
                            MobileRuleBean("0965", 9),
                            MobileRuleBean("0968", 9),
                            MobileRuleBean("0983", 9),
                            MobileRuleBean("0985", 9),
                            MobileRuleBean("0986", 9),
                            MobileRuleBean("0987", 9),
                            MobileRuleBean("0947", 10),
                            MobileRuleBean("0949", 10),
                            MobileRuleBean("0973", 10),
                            MobileRuleBean("0991", 10),
                        )
                    ),
                )
            }
            return mMobileOperatorBeans!!
        }

    //保存手机号规则
    fun saveMobileRules(array: JSONArray?) {
        if (!array.isNullOrEmpty() && mMobileOperatorBeans.isNullOrEmpty()) {
            val list = ArrayList<MobileOperatorBean>()
            array.forEachObject {
                list.add(MobileOperatorBean(it))
            }
            mMobileOperatorBeans = list
        }
    }
}

/**
 * 手机运营商信息
 */
class MobileOperatorBean {

    //运营商名称
    val name: String

    //规则
    val rules: List<MobileRuleBean>

    constructor(json: JSONObject) {
        name = json.stringValue("operatorName").zawgyi()
        val array = json.getJSONArray("ruleVOS")
        val list = ArrayList<MobileRuleBean>()
        array?.forEachObject {
            list.add(
                MobileRuleBean(
                    it.stringValue("mobilePrefix"),
                    it.intValue("mobileLength")
                )
            )
        }
        rules = list
    }

    constructor(name: String, rules: List<MobileRuleBean>) {
        this.name = name
        this.rules = rules
    }
}

/**
 * 号码规则信息
 * @param prefix 号码前缀
 * @param length 号码长度
 */
class MobileRuleBean(val prefix: String, val length: Int)