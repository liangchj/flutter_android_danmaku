package com.lchj.flutter_android_danmaku.danmaku

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.lchj.flutter_android_danmaku.FlutterDanmakuConstant
import com.lchj.flutter_android_danmaku.danmaku.akDanmaku.FlutterAKDanmakuView
import com.lchj.flutter_android_danmaku.danmaku.danmakuFlameMaster.FlutterDanmakuFlameMasterView
import io.flutter.Log
import org.apache.commons.collections4.MapUtils

enum class AndroidDanmakuType(name: String) {
    DANMAKU_FLAME_MASTER("danmakuFlameMaster"),
    AK_DANMAKU("akDanmaku")
}
@SuppressLint("StaticFieldLeak")
object FlutterDanmakuViewUtils {
//    private var biliDanmakuView : BiliDanmakuView? = null
    // AndroidView
//    private var danmakuView : View? = null
    private var danmakuView : IFlutterDanmakuView? = null
    // 弹幕地址
    private var danmakuPath : String? = null

    /**
     * 获取弹幕VIEW
     */
    fun getView(context: Context?, args: Map<String, Any>): View? {
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "getView，获取弹幕VIEW")
        // 是否重新创建
        val rebuild: Boolean = MapUtils.getBoolean(args, "rebuild", false)
        val newDanmakuPath = MapUtils.getString(args, "danmakuPath", "")

        val androidDanmakuType: String = MapUtils.getString(args, "androidDanmakuType", AndroidDanmakuType.DANMAKU_FLAME_MASTER.name);

        // 弹幕路径为空时返回空的view
        if (newDanmakuPath.isEmpty()) {
            dispose()
            return null
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuPath: $newDanmakuPath")
        // 直接重绘/还未创建view/弹幕地址不一致
        if (rebuild || danmakuView == null || danmakuPath != newDanmakuPath) {
            dispose() // 先将之前创建的清除
            danmakuPath = newDanmakuPath
            Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "newDanmakuPath是否赋值给danmakuPath成功: $danmakuPath")
            try {
                if (androidDanmakuType == AndroidDanmakuType.DANMAKU_FLAME_MASTER.name) {
                    danmakuView = FlutterDanmakuFlameMasterView(context, newDanmakuPath, args)
                } else if (androidDanmakuType == AndroidDanmakuType.AK_DANMAKU.name) {
                    danmakuView = FlutterAKDanmakuView(context, newDanmakuPath, args)
                }
            } catch (e: Exception) {
                Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuView创建失败: $e")
            }
            Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuView创建是否成功: ${danmakuView == null}")
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuView?.getView(): ${danmakuView?.getView()}")
        return danmakuView?.getView()
    }

    /**
     * 销毁弹幕VIEW
     */
    fun dispose() {
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "销毁view")
        if (danmakuView != null) {
            danmakuView?.dispose()
        }
        danmakuView  = null
        danmakuPath  = null
    }


    /**
     * 开始弹幕
     */
    fun startDanmaku(position: Long?) {
        try {
            danmakuView?.startDanmaku(position)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "startDanmaku 失败：$e")
        }
    }
    /**
     * 暂停弹幕
     */
    fun pauseDanmaKu() {
        try {
            danmakuView?.pauseDanmaKu()
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "pauseDanmaKu 失败：$e")
        }
    }

    /**
     * 继续弹幕
     */
    fun resumeDanmaku() {
        try {
            danmakuView?.resumeDanmaku()
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "resumeDanmaku 失败：$e")
        }
    }

    /**
     * 发送弹幕
     */
    fun sendDanmaku(isLive: Boolean, text: String, time: Long?, danmakuType: Int?,
                    padding: Int?, textSize: Float?, textColor: Int?,
                    textShadowColor: Int?, underlineColor: Int?, borderColor: Int?) {
        try {
            danmakuView?.sendDanmaku(isLive, text, time, danmakuType, padding,
                textSize, textColor, textShadowColor, underlineColor, borderColor)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "sendDanmaku 失败：$e")
        }
    }

    /**
     * 获取当前弹幕时间
     */
    fun danmakuCurrentTime(): Long? {
        var currentTime: Long? = null
        try {
            currentTime = danmakuView?.danmakuCurrentTime()
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmakuCurrentTime 失败：$e")
        }
        return currentTime
    }

    /**
     * 弹幕跳转
     */
    fun danmaKuSeekTo(position: Long) {
        try {
            danmakuView?.danmaKuSeekTo(position)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "danmaKuSeekTo 失败：$e")
        }
    }

    /**
     * 显示或隐藏
     */
    fun setDanmaKuVisibility(visible: Boolean) {
        try {
            danmakuView?.setDanmaKuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmaKuVisibility 失败：$e")
        }
    }

    /**
     * 设置弹幕透明的（百分比）
     */
    fun setDanmakuAlphaRatio(danmakuAlphaRatio: Float) {
        try {
            danmakuView?.setDanmakuAlphaRatio(danmakuAlphaRatio)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuAlphaRatio 失败：$e")
        }
    }
    /**
     * 设置显示区域
     */
    fun setDanmakuDisplayArea(area: Float) {
        try {
            danmakuView?.setDanmakuDisplayArea(area)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuDisplayArea 失败：$e")
        }
    }
    /**
     * 设置弹幕文字大小（百分比）
     */
    fun setDanmakuFontSize(fontSizeRatio: Float) {
        try {
            danmakuView?.setDanmakuFontSize(fontSizeRatio)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuFontSize 失败：$e")
        }
    }

    /**
     * 设置弹幕滚动速度
     */
    fun setDanmakuSpeed(danmakuSpeed: Float) {
        try {
            danmakuView?.setDanmakuSpeed(danmakuSpeed)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuSpeed 失败：$e")
        }
    }
    /**
     * 设置弹幕描边
     */
    fun setDanmakuStroke(stroke: Float) {
        try {
            danmakuView?.setDanmakuStroke(stroke)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDanmakuStroke 失败：$e")
        }
    }

    /**
     * 设置是否启用合并重复弹幕
     */
    fun setDuplicateMergingEnabled(merge: Boolean) {
        try {
            danmakuView?.setDuplicateMergingEnabled(merge)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setDuplicateMergingEnabled 失败：$e")
        }
    }


    /**
     * 设置是否显示顶部固定弹幕
     */
    fun setFixedTopDanmakuVisibility(visible: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "FlutterDanmakuViewUtils -> setFixedTopDanmakuVisibility 进入设置是否显示顶部固定弹幕，visible：$visible")
        try {
            danmakuView?.setFixedTopDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setFixedTopDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示滚动弹幕
     */
//    fun setRollDanmakuVisibility(visible: Boolean) {
//        try {
//            danmakuView?.setRollDanmakuVisibility(visible)
//        } catch (e: Exception) {
//            Log.e(DanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setRollDanmakuVisibility 失败：$e")
//        }
//    }
    /**
     * 设置是否显示从左向右滚动弹幕
     */
    fun setL2RDanmakuVisibility(visible: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "FlutterDanmakuViewUtils -> setL2RDanmakuVisibility 进入设置是否显示从左向右滚动弹幕，visible：$visible")
        try {
            danmakuView?.setL2RDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setL2RDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示从右向左滚动弹幕
     */
    fun setR2LDanmakuVisibility(visible: Boolean) {
        try {
            danmakuView?.setR2LDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setR2LDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示底部固定弹幕
     */
    fun setFixedBottomDanmakuVisibility(visible: Boolean) {
        try {
            danmakuView?.setFixedBottomDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setFixedBottomDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 设置是否显示特殊弹幕
     */
    fun setSpecialDanmakuVisibility(visible: Boolean) {
        try {
            danmakuView?.setSpecialDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setSpecialDanmakuVisibility 失败：$e")
        }
    }

    /**
     * 是否显示彩色弹幕
     */
    fun setColorsDanmakuVisibility(visible: Boolean) {
        try {
            danmakuView?.setColorsDanmakuVisibility(visible)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setColorsDanmakuVisibility 失败：$e")
        }
    }
    /**
     * 设置是否允许重叠
     */
    fun setAllowOverlap(flag: Boolean) {
        try {
            danmakuView?.setAllowOverlap(flag)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setAllowOverlap 失败：$e")
        }
    }

    /**
     * 设置最大显示行数
     * 设置null取消行数限制
     */
    fun setMaximumLines(lines: Int?) {
        try {
            danmakuView?.setMaximumLines(lines)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setMaximumLines 失败：$e")
        }
    }

    /**
     * 设置同屏弹幕密度 -1自动 0无限制  n 同屏最大显示n个弹幕
     *
     * @param maxSize
     */
    fun setMaximumVisibleSizeInScreen(maxSize: Int) {
        try {
            danmakuView?.setMaximumVisibleSizeInScreen(maxSize)
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_UTILS_LOG_TAG, "setMaximumVisibleSizeInScreen 失败：$e")
        }
    }
}