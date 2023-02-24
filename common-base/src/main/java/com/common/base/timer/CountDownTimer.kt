package com.common.base.timer

import android.os.Handler
import android.os.Looper
import android.os.SystemClock


/**
 * 计时器 当使用 COUNT_DOWN_INFINITE 必须手动停止该计时器
 */
abstract class CountDownTimer(){

     companion object{
        
        //没有倒计时长度限制
        const val COUNT_DOWN_INFINITE = Long.MAX_VALUE

        //倒计时消息
        private const val COUNT_DOWN_MSG_WHAT = 1
    }
    
    //倒计时总时间长度（毫秒），如果为 COUNT_DOWN_INFINITE 则 没有限制，倒计时不会停止 必须自己手动停止
    private var _millisToCountDown: Long = 0

    //倒计时间隔（毫秒）
    private var _millisInterval: Long = 0

    //倒计时停止时间（毫秒）
    private var _millisToStop: Long = 0

    //倒计时是否已取消
    private var _canceled = false

    //是否正在倒计时
    var isExecuting = false
        private set

    //
    private val _handler = Handler(Looper.getMainLooper()){
        if (_canceled) {
            return@Handler true
        }
        if (_millisToCountDown == COUNT_DOWN_INFINITE) {

            //倒计时无时间限制
            triggerTick(COUNT_DOWN_INFINITE)
        } else {

            //倒计时剩余时间
            val millisLeft = _millisToStop - SystemClock.elapsedRealtime()
            if (millisLeft <= 0) {
                //没时间了，倒计时停止
                finish()
            } else if (millisLeft < _millisInterval) {
                //剩余的时间已经不够触发一次倒计时间隔了
                onTick(millisLeft)
                continueTimer(millisLeft)
            } else {
                triggerTick(millisLeft)
            }
        }
        true
    }

    constructor(millisToCountDown: Long, millisInterval: Long) : this() {
        _millisToCountDown = millisToCountDown
        _millisInterval = millisInterval
    }

    //设置不同的值会导致计时器停止
    open fun setMillisInterval(millisInterval: Long) {
        if (_millisInterval != millisInterval) {
            _millisInterval = millisInterval
            stop()
        }
    }

    //设置不同的值会导致计时器停止
    open fun setMillisToCountDown(millisToCountDown: Long) {
        if (_millisToCountDown != millisToCountDown) {
            _millisToCountDown = millisToCountDown
            stop()
        }
    }

    //开始倒计时
    open fun start() {
        if (isExecuting) {
            _handler.removeMessages(COUNT_DOWN_MSG_WHAT)
        }
        _canceled = false
        if (_millisToCountDown <= 0 || _millisInterval <= 0) {
            finish()
            return
        }
        isExecuting = true
        if (_millisToCountDown == COUNT_DOWN_INFINITE) { //倒计时无时间限制
            _handler.sendEmptyMessageDelayed(COUNT_DOWN_MSG_WHAT, _millisInterval)
        } else {
            _millisToStop = SystemClock.elapsedRealtime() + _millisToCountDown
            _handler.sendEmptyMessage(COUNT_DOWN_MSG_WHAT)
        }
    }

    //停止倒计时
    open fun stop() {
        if (_canceled || !isExecuting) return
        _canceled = true
        isExecuting = false
        _handler.removeMessages(COUNT_DOWN_MSG_WHAT)
    }


    //执行完成
    private fun finish() {
        if (!isExecuting) return
        isExecuting = false
        _canceled = false
        onFinish()
    }

    //触发tick
    private fun triggerTick(millisLeft: Long) {
        val lastTickStart = SystemClock.elapsedRealtime()
        onTick(millisLeft)
        var delay =
            lastTickStart + _millisInterval - SystemClock.elapsedRealtime()
        while (delay < 0) { //当触发倒计时 onTick 方法耗时太多，将进行下一个倒计时间隔
            delay += _millisInterval
        }
        continueTimer(delay)
    }

    //继续倒计时
    private fun continueTimer(mills: Long){
        _handler.sendEmptyMessageDelayed(COUNT_DOWN_MSG_WHAT, mills)
    }

    //倒计时结束
    abstract fun onFinish()

    /**
     * 每个间隔触发回调
     * @param millisLeft 倒计时剩余时间（毫秒）倒计时无限制时，为COUNT_DOWN_INFINITE
     */
    abstract fun onTick(millisLeft: Long)
}