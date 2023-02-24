package com.common.base.utils

import android.graphics.Point
import kotlin.math.cos
import kotlin.math.sin


//数学计算工具类
object MathUtils {

    /**
     * 获取圆上的坐标
     * @param center 圆心坐标
     * @param radius 半径
     * @param arc 要获取坐标的弧度 0 - 360
     * @return 坐标
     */
    fun pointInCircle(center: Point, radius: Int, arc: Float): Point {

        val x = (center.x + cos(arc * Math.PI / 180) * radius)
        val y = (center.y + sin(arc * Math.PI / 180) * radius)

        return Point(x.toInt(), y.toInt())
    }
}