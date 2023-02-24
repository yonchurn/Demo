package com.common.base.utils

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.widget.TextView
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil


/**
 * 字符串工具类
 */
@Suppress("deprecation")
object StringUtils {

    /**
     * 格式化
     */
    fun format(format: String, args: Any): String {
        return String.format(Locale.US, format, args)
    }


    /**
     * 获取一段随机数字
     * @param length 数字长度
     * @return 随机数
     */
    fun getRandomNumber(length: Int): String? {
        val random = Random()
        val builder = StringBuilder()
        for (i in 0 until length) {
            builder.append(random.nextInt(10))
        }
        return builder.toString()
    }


    /**
     * 是否有某个前缀
     * @param string 要判断的图片
     * @param prefix 前缀
     * @return 是否
     */
    fun hasPrefix(string: String?, prefix: String): Boolean {
        if (TextUtils.isEmpty(string) || TextUtils.isEmpty(prefix)) return false

        if (string!!.length < prefix.length) return false

        val str = string.substring(0, prefix.length)
        return str == prefix
    }

    /**
     * 判断字符串是否为空
     * @param text 要判断的字符串
     * @return 是否为空
     */
    fun isEmpty(text: CharSequence?): Boolean {
        if (text == null || text.isEmpty()) {
            return true
        }
        return if (text is String) {
            "null" == text
        } else false
    }

    fun isNotEmpty(text: CharSequence?): Boolean {
        return !isEmpty(text)
    }

    fun stringFromBytes(bytes: ByteArray?): String? {
        return stringFromBytes(bytes, Charset.forName("utf-8"))
    }

    /**
     * 通过字节生成字符串
     * @param bytes 字节数组
     * @param charset 字符编码
     * @return 字符串
     */
    fun stringFromBytes(bytes: ByteArray?, charset: Charset): String {
        return if (bytes == null || bytes.isEmpty()) "" else try {
            String(bytes, 0, bytes.size, charset)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * 获取字体行高
     * @param textSize 字体大小 px
     * @return 行高
     */
    fun getFontHeight(textSize: Float): Int {
        val paint = Paint()
        paint.textSize = textSize
        val fm: Paint.FontMetrics = paint.fontMetrics
        return ceil(fm.descent - fm.ascent).toInt()
    }

    fun measureTextHeight(text: CharSequence?, context: Context, textSize: Int, maxWidth: Int): Int {
        return measureTextHeight(text, context, textSize, null, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f)
    }

    fun measureTextHeight(text: CharSequence?, paint: TextPaint?, maxWidth: Int): Int {
        return measureTextHeight(text, null, 0, paint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f)
    }

    /**
     * 计算文字高度
     * @param text 要计算的文字
     * @param context 上下文， 和 paint 必须传一个，传paint就可以不传 context了
     * @param textSize 字体大小，如果paint已传，会忽略这个 px
     * @param textPaint 可用[TextView.getPaint]
     * @param maxWidth 显示文字的最大宽度 px
     * @param textAlignment 文字对齐方式
     * @param lineSpacingMultiplier 行距倍数 [TextView.getLineSpacingMultiplier] default is 1.0f
     * @param lineSpacingExtra 行距额外高度 [TextView.getLineSpacingExtra] default is 0.0f
     * @return 文字高度
     */
    fun measureTextHeight(text: CharSequence?, context: Context?, textSize: Int, textPaint: TextPaint?, maxWidth: Int, textAlignment: Layout.Alignment?, lineSpacingMultiplier: Float, lineSpacingExtra: Float): Int {

        if (TextUtils.isEmpty(text)) return 0

        var paint = textPaint
        var alignment = textAlignment
        if (paint == null) {
            if (context == null) throw NullPointerException("context or paint must pass one")
            val res = context.resources
            paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            paint.density = res.displayMetrics.density
            paint.textSize = textSize.toFloat()
        }
        if (alignment == null) {
            alignment = Layout.Alignment.ALIGN_NORMAL
        }
        val layout : StaticLayout
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val builder = StaticLayout.Builder.obtain(text!!,0, text.length, paint, maxWidth)
            builder.setAlignment(alignment)
            builder.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
            builder.setIncludePad(true)
            layout = builder.build()
        }else{
           layout = StaticLayout(text, paint, maxWidth, alignment, lineSpacingMultiplier, lineSpacingExtra, true)
        }
        return layout.height
    }

    /**
     * 判断是否为纯字母
     */
    fun isAlpha(str: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(str)) false else Pattern.compile("^[A-Za-z]+$").matcher(str!!).matches()
    }

    /**
     * 判断是否为字母或数字组合
     */
    fun isAlphaNumber(str: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(str)) false else Pattern.compile("^[A-Za-z0-9]+$").matcher(str!!).matches()
    }

    /**
     * 判断是否为纯数字
     */
    fun isNumeric(str: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(str)) false else Pattern.compile("^[0-9]+$").matcher(str!!).matches()
    }

    /**
     * 功能：判断字符串是否为日期格式
     *
     * @param str xx
     * @return
     */
    fun isDate(str: String?): Boolean {
        if(TextUtils.isEmpty(str))
            return false
        val pattern = Pattern.compile("^((\\d{2}(([02468][048])|([13579][26]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))(\\s(((0?[0-9])|([1-2][0-3]))\\:([0-5]?[0-9])((\\s)|(\\:([0-5]?[0-9])))))?$")
        val m = pattern.matcher(str!!)
        return m.matches()
    }

    /**
     * 判断是否为手机号
     *
     * @param mobile 手机号
     * @return 返回true则是手机号
     */
    fun isMobile(mobile: CharSequence): Boolean {
        return if (TextUtils.isEmpty(mobile) || mobile.length != 11) false else Pattern.compile("^1[34578]\\d{9}$").matcher(
            mobile
        ).matches()
    }

    /**
     * 判断是否为固话
     *
     * @param tel 固话
     * @return 返回true则是手机号
     */
    fun isTelPhoneNumber(tel: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(tel)) false else Pattern.compile(
            "((\\d{12})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-" +
                    "(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)|(\\d{11})"
        ).matcher(tel!!).matches()
    }

    /**
     * 是否是邮箱
     */
    fun isEmail(email: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(email)) false else Pattern.compile("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}").matcher(email!!).matches()
    }

    /**
     * 是否是 http url
     */
    fun isHttpUrl(url: CharSequence?): Boolean {
        if (TextUtils.isEmpty(url)) return false

        val allCharacter = "[0-9a-zA-Z!\\$&'\\(\\)\\*\\+,\\-\\.:;=\\?@\\[\\]_~]"
        val scheme = "((http[s]?)://)?"; //协议 可选
        val host = "((${allCharacter}+\\.){2,}[a-zA-Z]{2,6}\\b)" //主机
        val path = "[#%/0-9a-zA-Z!\\\$&'\\(\\)\\*\\+,\\-\\.:;=\\?@\\[\\]_~]*" //路径

        val regex = "$scheme$host$path"
        return Pattern.compile(regex).matcher(url!!).matches()
    }

    /*********************************** 身份证验证开始 ****************************************/
    /**
     * 身份证号码验证 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，
     * 八位数字出生日期码，三位数字顺序码和一位数字校验码。 2、地址码(前六位数）
     * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。 3、出生日期码（第七位至十四位）
     * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。 4、顺序码（第十五位至十七位）
     * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。 5、校验码（第十八位数）
     * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0
     * X 9 8 7 6 5 4 3 2
     */

    /*********************************** 身份证验证开始  */
    /**
     * 身份证号码验证 1、号码的结构 公民身份号码是特征组合码，由十七位数字本体码和一位校验码组成。排列顺序从左至右依次为：六位数字地址码，
     * 八位数字出生日期码，三位数字顺序码和一位数字校验码。 2、地址码(前六位数）
     * 表示编码对象常住户口所在县(市、旗、区)的行政区划代码，按GB/T2260的规定执行。 3、出生日期码（第七位至十四位）
     * 表示编码对象出生的年、月、日，按GB/T7408的规定执行，年、月、日代码之间不用分隔符。 4、顺序码（第十五位至十七位）
     * 表示在同一地址码所标识的区域范围内，对同年、同月、同日出生的人编定的顺序号， 顺序码的奇数分配给男性，偶数分配给女性。 5、校验码（第十八位数）
     * （1）十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0, ... , 16 ，先对前17位数字的权求和
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2 （2）计算模 Y = mod(S, 11) （3）通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0
     * X 9 8 7 6 5 4 3 2
     */
    /**
     * 身份证的有效验证
     * @param str 身份证号
     * @return 是否是有效身份证
     */
    fun isIdCard(str: String): Boolean {
        if (isEmpty(str)) return false

        // 号码的长度 15位或18位
        if (str.length != 15 && str.length != 18) {
            return false
        }
        var numberic = ""

        //数字 除最后以外都为数字
        if (str.length == 18) {
            numberic = str.substring(0, 17)
        } else if (str.length == 15) {
            numberic = "${str.substring(0, 6)}19${str.substring(6, 15)}"
        }

        //身份证15位号码都应为数字 18位号码除最后一位外，都应为数字
        if (!isNumeric(numberic)) {
            return false
        }

        //出生年月是否有效
        val strYear = numberic.substring(6, 10) // 年份
        val strMonth = numberic.substring(10, 12) // 月份
        val strDay = numberic.substring(12, 14) // 月份

        //身份证生日无效
        if (!isDate("$strYear-$strMonth-$strDay")) {
            return false
        }

        val gc = GregorianCalendar()
        val s = DateUtils.YMdDateFormat
        try { //身份证生日不在有效范围

            val date = s.parse("$strYear-$strMonth-$strDay")
            val time = if(date != null) date.time else 0

            if (gc.get(Calendar.YEAR) - strYear.toInt() > 150 || gc.time.time - time < 0) {
                return false
            }

            //身份证月份无效
            if (strMonth.toInt() > 12 || strMonth.toInt() == 0) {
                return false
            }

            //身份证日期无效
            if (strDay.toInt() > 31 || strDay.toInt() == 0) {
                return false
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false
        } catch (e: ParseException) {
            e.printStackTrace()
            return false
        }

        //地区码时候有效
        val h = getAreaCode()
        if (h[numberic.substring(0, 2)] == null) { //身份证地区编码错误
            return false
        }

        //判断最后一位的值
        var last = 0

        val arr = arrayOf(
            7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7,
            9, 10, 5, 8, 4, 2
        )

        for (i in 0..16) {
            last += numberic[i].toInt() * arr[i]
        }
        val modValue = last % 11
        val codes = arrayOf(
            "1", "0", "X", "9", "8", "7", "6", "5", "4",
            "3", "2"
        )

        val strVerifyCode = codes[modValue]
        numberic += strVerifyCode
        if (str.length == 18) {
            if (numberic != str) { //身份证无效，不是合法的身份证号码
                return false
            }
        } else {
            return true
        }
        return true
    }

    /**
     * 功能：设置地区编码
     *
     * @return Hashtable 对象
     */
    private fun getAreaCode(): Hashtable<String, String> {
        val hashtable = Hashtable<String, String>()
        hashtable["11"] = "北京"
        hashtable["12"] = "天津"
        hashtable["13"] = "河北"
        hashtable["14"] = "山西"
        hashtable["15"] = "内蒙古"
        hashtable["21"] = "辽宁"
        hashtable["22"] = "吉林"
        hashtable["23"] = "黑龙江"
        hashtable["31"] = "上海"
        hashtable["32"] = "江苏"
        hashtable["33"] = "浙江"
        hashtable["34"] = "安徽"
        hashtable["35"] = "福建"
        hashtable["36"] = "江西"
        hashtable["37"] = "山东"
        hashtable["41"] = "河南"
        hashtable["42"] = "湖北"
        hashtable["43"] = "湖南"
        hashtable["44"] = "广东"
        hashtable["45"] = "广西"
        hashtable["46"] = "海南"
        hashtable["50"] = "重庆"
        hashtable["51"] = "四川"
        hashtable["52"] = "贵州"
        hashtable["53"] = "云南"
        hashtable["54"] = "西藏"
        hashtable["61"] = "陕西"
        hashtable["62"] = "甘肃"
        hashtable["63"] = "青海"
        hashtable["64"] = "宁夏"
        hashtable["65"] = "新疆"
        hashtable["71"] = "台湾"
        hashtable["81"] = "香港"
        hashtable["82"] = "澳门"
        hashtable["91"] = "国外"
        return hashtable
    }

    /*********************************** 身份证验证结束 ****************************************/
}