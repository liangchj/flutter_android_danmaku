package com.lchj.flutter_android_danmaku.danmaku

import android.view.View
import master.flame.danmaku.danmaku.model.android.DanmakuContext

// 弹幕view默认有获取和销毁方法
interface IFlutterDanmakuView {
    fun getView(): View
    fun dispose()

    /**
     * 初始化弹幕
     */

    /**
     * 开始弹幕
     */
    fun startDanmaku(position: Long?)

    /**
     * 暂停弹幕
     */
    fun pauseDanmaKu()

    /**
     * 继续弹幕
     */
    fun resumeDanmaku()

    /**
     * 发送弹幕
     */
    fun sendDanmaku(
        isLive: Boolean, text: String, time: Long?, danmakuType: Int?,
        padding: Int?, textSize: Float?, textColor: Int?,
        textShadowColor: Int?, underlineColor: Int?, borderColor: Int?
    )



    /**
     * 获取当前弹幕时间
     */
    fun danmakuCurrentTime(): Long

    /**
     * 弹幕跳转
     */
    fun danmaKuSeekTo(position: Long)

    /**
     * 显示或隐藏
     */
    fun setDanmaKuVisibility(visible: Boolean)

    /***
     * 设置弹幕透明度（百分比）
     */
    fun setDanmakuAlphaRatio(danmakuAlphaRatio: Float)

    /**
     * 设置显示区域
     */
    fun setDanmakuDisplayArea(area: Float)

    /**
     * 设置弹幕文字大小（百分比）
     */
    fun setDanmakuFontSize(fontSizeRatio: Float)

    /**
     * 设置弹幕滚动速度
     */
    fun setDanmakuSpeed(danmakuSpeed: Float)

    /**
     * 设置弹幕描边
     */
    fun setDanmakuStroke(stroke: Float)


    /**
     * 设置是否启用合并重复弹幕
     */
    fun setDuplicateMergingEnabled(merge: Boolean)

    /**
     * 设置是否显示顶部固定弹幕
     */
    fun setFixedTopDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示滚动弹幕
     */
//    fun setRollDanmakuVisibility(visible: Boolean)
    /**
     * 设置是否显示从左向右滚动弹幕
     */
    fun setL2RDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示从右向左滚动弹幕
     */
    fun setR2LDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示底部固定弹幕
     */
    fun setFixedBottomDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否显示特殊弹幕
     */
    fun setSpecialDanmakuVisibility(visible: Boolean)

    /**
     * 是否显示彩色弹幕
     */
    fun setColorsDanmakuVisibility(visible: Boolean)

    /**
     * 设置是否允许重叠
     */
    fun setAllowOverlap(flag: Boolean)

    /**
     * 设置最大显示行数
     * 设置null取消行数限制
     */
    fun setMaximumLines(lines: Int?)

    /**
     * 设置同屏弹幕密度 -1自动 0无限制  n 同屏最大显示n个弹幕
     *
     * @param maxSize
     */
    fun setMaximumVisibleSizeInScreen(maxSize: Int)
}