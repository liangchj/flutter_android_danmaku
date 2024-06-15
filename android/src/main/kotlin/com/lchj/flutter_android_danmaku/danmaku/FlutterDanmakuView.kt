package com.lchj.flutter_android_danmaku.danmaku

import android.content.Context
import android.view.View
import com.lchj.flutter_android_danmaku.FlutterDanmakuConstant
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class FlutterDanmakuView(
    private var context: Context?,
    messenger: BinaryMessenger,
    viewId: Int,
    args: Map<String, Any>?
) :
    PlatformView, MethodChannel.MethodCallHandler  {
    private var args: Map<String, Any> = args ?: hashMapOf()

    private var methodChannel: MethodChannel = MethodChannel(messenger, FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_METHOD_CHANNEL)

    init {
        Log.d(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "FlutterDanmakuView viewId: $viewId")
        methodChannel.setMethodCallHandler(this)
    }

    override fun getView(): View {
        val view: View?
        try {
            view = FlutterDanmakuViewUtils.getView(context, args)
        } catch (e: Exception) {
            Log.d(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "FlutterDanmakuView 创建弹幕VIEW报错: $e")
            sendMessageToFlutter("AndroidViewCreateFail","报错：$e")
            return View(context)
        }
        Log.d(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "FlutterDanmakuView getView: ${view == null}, view: $view")
        sendMessageToFlutter("AndroidViewCreateSuccess",view != null)
        return view ?: View(context)
    }

    private fun sendMessageToFlutter(method: String, msg: Any) {
        methodChannel.invokeMethod(method, msg)
    }

    override fun dispose() {
        FlutterDanmakuViewUtils.dispose()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                val m: Map<String, Any> = mapOf("method" to "getPlatformVersion", "flag" to true, "msg" to "Android ${android.os.Build.VERSION.RELEASE}")
                result.success(mapOf("method" to "getPlatformVersion", "flag" to true, "msg" to "Android ${android.os.Build.VERSION.RELEASE}"))
            }
            "sendDanmaku" -> { // 发送弹幕
                try {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "sendDanmaku读取到的参数，${call.arguments}")
                    val isLive: Boolean = call.argument<Boolean>("isLive") as Boolean
                    val danmakuText: String? = call.argument<String>("danmakuText")
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "sendDanmaku读取到的内容，$danmakuText")
                    val timeStr: String? = call.argument<String>("time")
                    var time: Long? = null
                    if (!timeStr.isNullOrEmpty()) {
                        try {
                            time = timeStr.toLongOrNull()
                        } catch (e: Exception) {
                            Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "sendDanmaku时读取指定时间转换失败: $e")
                        }
                    }
                    val danmakuType: Int? = call.argument<Int>("danmakuType")
                    val padding: Int? = call.argument<Int>("padding")
                    val textSize: Float? = call.argument<Double>("textSize")?.toFloat()
                    val textColor: Int? = call.argument<Int>("textColor")
                    val textShadowColor: Int? = call.argument<Int>("textShadowColor")
                    val underlineColor: Int? = call.argument<Int>("underlineColor")
                    val borderColor: Int? = call.argument<Int>("borderColor")
                    if (!danmakuText.isNullOrEmpty()) {
                        Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "sendDanmaku发送内容不为空，$danmakuText")
                        FlutterDanmakuViewUtils.sendDanmaku(isLive, danmakuText, time, danmakuType,
                            padding, textSize, textColor, textShadowColor, underlineColor, borderColor)
                    }
                    result.success(mapOf("method" to "sendDanmaku", "flag" to true, "msg" to ""))
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "sendDanmaku error: $e")
                    result.success(mapOf("method" to "sendDanmaku", "flag" to false, "msg" to "发送弹幕失败：$e"))
                }
            }
            "danmakuCurrentTime" -> { // 获取当前弹幕时间
                try {
                    val time: Long? = FlutterDanmakuViewUtils.danmakuCurrentTime()
                    result.success(mapOf("method" to "danmakuCurrentTime", "flag" to true, "msg" to (time ?: 0)))
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "danmakuCurrentTime error: $e")
                    result.success(mapOf("method" to "danmakuCurrentTime", "flag" to false, "msg" to "获取当前弹幕时间失败：$e"))
                }
            }
            "startDanmaku" -> { // 启动弹幕
                try {
                    val timeStr: String? = call.argument<String>("time")
                    var time: Long? = null
                    if (!timeStr.isNullOrEmpty()) {
                        try {
                            time = timeStr.toLongOrNull()
                        } catch (e: Exception) {
                            Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "startDanmaku时读取指定时间转换失败: $e")
                        }
                    }
                    FlutterDanmakuViewUtils.startDanmaku(time)
                    result.success(mapOf("method" to "startDanmaku", "flag" to true, "msg" to ""))
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "startDanmaku error: $e")
                    result.success(mapOf("method" to "startDanmaku", "flag" to false, "msg" to "启动弹幕失败：$e"))
                }
            }
            "pauseDanmaKu" -> { // 暂停弹幕
                try {
                    FlutterDanmakuViewUtils.pauseDanmaKu()
                    result.success(mapOf("method" to "pauseDanmaKu", "flag" to true, "msg" to ""))
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "pauseDanmaKu error: $e")
                    result.success(mapOf("method" to "pauseDanmaKu", "flag" to false, "msg" to "暂停弹幕失败：$e"))
                }
            }
            "resumeDanmaku" -> { // 继续弹幕
                try {
                    FlutterDanmakuViewUtils.resumeDanmaku()
                    result.success(mapOf("method" to "resumeDanmaku", "flag" to true, "msg" to ""))
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "resumeDanmaku error: $e")
                    result.success(mapOf("method" to "resumeDanmaku", "flag" to false, "msg" to "继续弹幕失败：$e"))
                }
            }
            "danmaKuSeekTo" -> { // 跳转弹幕
                try {
                    val timeStr: String? = call.argument<String>("time")
                    var time: Long? = null
                    if (!timeStr.isNullOrEmpty()) {
                        try {
                            time = timeStr.toLongOrNull()
                        } catch (e: Exception) {
                            Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "danmaKuSeekTo时读取指定时间转换失败: $e")
                        }
                    }
                    if (time == null) {
                        result.success(mapOf("method" to "danmaKuSeekTo", "flag" to false, "msg" to "跳转弹幕失败：无法获取跳转时间"))
                    } else {
                        FlutterDanmakuViewUtils.danmaKuSeekTo(time)
                        result.success(mapOf("method" to "danmaKuSeekTo", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "danmaKuSeekTo error: $e")
                    result.success(mapOf("method" to "danmaKuSeekTo", "flag" to false, "msg" to "跳转弹幕失败：$e"))
                }
            }
            "setDanmaKuVisibility" -> { // 显示/隐藏弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setDanmaKuVisibility", "flag" to false, "msg" to "设置显示/隐藏弹幕失败：无法获取设置的显示状态"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmaKuVisibility(visible)
                        result.success(mapOf("method" to "setDanmaKuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmaKuVisibility error: $e")
                    result.success(mapOf("method" to "setDanmaKuVisibility", "flag" to false, "msg" to "设置显示/隐藏弹幕失败：$e"))
                }
            }
            "setDanmakuAlphaRatio" -> { // 设置弹幕透明度（百分比）
                try {
                    val danmakuAlphaRatio: Float? = call.argument<Double>("danmakuAlphaRatio")?.toFloat()
                    if (danmakuAlphaRatio == null) {
                        result.success(mapOf("method" to "setDanmakuAlphaRatio", "flag" to false, "msg" to "设置弹幕透明度失败：无法获取设置弹幕透明度值"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmakuAlphaRatio(danmakuAlphaRatio)
                        result.success(mapOf("method" to "setDanmakuAlphaRatio", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmakuAlphaRatio error: $e")
                    result.success(mapOf("method" to "setDanmakuAlphaRatio", "flag" to false, "msg" to "设置弹幕透明度失败：$e"))
                }
            }
            "setDanmakuDisplayArea" -> { // 设置弹幕显示区域
                try {
                    val area: Float? = call.argument<Double>("area")?.toFloat()
                    if (area == null) {
                        result.success(mapOf("method" to "setDanmakuDisplayArea", "flag" to false, "msg" to "设置弹幕显示区域失败：无法获取设置弹幕显示区域值"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmakuDisplayArea(area)
                        result.success(mapOf("method" to "setDanmakuDisplayArea", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmakuDisplayArea error: $e")
                    result.success(mapOf("method" to "setDanmakuDisplayArea", "flag" to false, "msg" to "设置弹幕显示区域失败：$e"))
                }
            }
            "setDanmakuFontSize" -> { // 设置字体大小（百分比）
                try {
                    val fontSizeRatio: Float? = call.argument<Double>("fontSizeRatio")?.toFloat()
                    if (fontSizeRatio == null) {
                        result.success(mapOf("method" to "setDanmakuFontSize", "flag" to false, "msg" to "设置字体大小失败：无法获取设置字体大小值"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmakuFontSize(fontSizeRatio)
                        result.success(mapOf("method" to "setDanmakuFontSize", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmakuFontSize error: $e")
                    result.success(mapOf("method" to "setDanmakuFontSize", "flag" to false, "msg" to "设置字体大小失败：$e"))
                }
            }
            "setDanmakuSpeed" -> { // 设置滚动速度
                try {
                    //播放速度
                    val playSpeed: Float? = call.argument<Double>("playSpeed")?.toFloat()
                    if (playSpeed == null) {
                        result.success(mapOf("method" to "setDanmakuSpeed", "flag" to false, "msg" to "设置滚动速度失败：无法获取设置滚动速度值"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmakuSpeed(playSpeed)
                        result.success(mapOf("method" to "setDanmakuSpeed", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmakuSpeed error: $e")
                    result.success(mapOf("method" to "setDanmakuSpeed", "flag" to false, "msg" to "设置滚动速度失败：$e"))
                }
            }
            "setDanmakuStroke" -> { // 设置弹幕描边
                try {
                    val stroke: Float? = call.argument<Double>("stroke")?.toFloat()
                    if (stroke == null) {
                        result.success(mapOf("method" to "setDanmakuStroke", "flag" to false, "msg" to "设置弹幕描边失败：无法获取设置弹幕描边值"))
                    } else {
                        FlutterDanmakuViewUtils.setDanmakuStroke(stroke)
                        result.success(mapOf("method" to "setDanmakuStroke", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDanmakuStroke error: $e")
                    result.success(mapOf("method" to "setDanmakuStroke", "flag" to false, "msg" to "设置弹幕描边失败：$e"))
                }
            }
            "setDuplicateMergingEnabled" -> { // 设置是否启用合并重复弹幕
                try {
                    val flag: Boolean? = call.argument<Boolean>("flag")
                    if (flag == null) {
                        result.success(mapOf("method" to "setDuplicateMergingEnabled", "flag" to false, "msg" to "设置是否启用合并重复弹幕失败：无法获取设置是否启用合并重复弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setDuplicateMergingEnabled(flag)
                        result.success(mapOf("method" to "setDuplicateMergingEnabled", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setDuplicateMergingEnabled error: $e")
                    result.success(mapOf("method" to "setDuplicateMergingEnabled", "flag" to false, "msg" to "设置是否启用合并重复弹幕失败：$e"))
                }
            }
            "setFixedTopDanmakuVisibility" -> { // 设置是否显示顶部固定弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setFixedTopDanmakuVisibility", "flag" to false, "msg" to "设置是否显示顶部固定弹幕失败：无法获取设置是否显示顶部固定弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setFixedTopDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setFixedTopDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setFixedTopDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setFixedTopDanmakuVisibility", "flag" to false, "msg" to "设置是否显示顶部固定弹幕失败：$e"))
                }
            }
            /*"setRollDanmakuVisibility" -> { // 设置是否显示滚动弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setRollDanmakuVisibility", "flag" to false, "msg" to "设置是否显示滚动弹幕失败：无法获取设置是否显示滚动弹幕值"))
                    } else {
                        DanmakuViewUtils.setRollDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setRollDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(DanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setRollDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setRollDanmakuVisibility", "flag" to false, "msg" to "设置是否显示滚动弹幕失败：$e"))
                }
            }*/
            "setL2RDanmakuVisibility" -> { // 设置是否显示从左向右滚动弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setL2RDanmakuVisibility", "flag" to false, "msg" to "设置是否显示从左向右滚动弹幕失败：无法获取设置是否显示从左向右滚动弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setL2RDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setL2RDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setL2RDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setL2RDanmakuVisibility", "flag" to false, "msg" to "设置是否显示从左向右滚动弹幕失败：$e"))
                }
            }
            "setR2LDanmakuVisibility" -> { // 设置是否显示从右向左滚动弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setR2LDanmakuVisibility", "flag" to false, "msg" to "设置是否显示从右向左滚动弹幕失败：无法获取设置是否显示从右向左滚动弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setR2LDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setR2LDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setR2LDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setR2LDanmakuVisibility", "flag" to false, "msg" to "设置是否显示从右向左滚动弹幕失败：$e"))
                }
            }
            "setFixedBottomDanmakuVisibility" -> { // 设置是否显示底部固定弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setFixedBottomDanmakuVisibility", "flag" to false, "msg" to "设置是否显示底部固定弹幕失败：无法获取设置是否显示底部固定弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setFixedBottomDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setFixedBottomDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setFixedBottomDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setFixedBottomDanmakuVisibility", "flag" to false, "msg" to "设置是否显示底部固定弹幕失败：$e"))
                }
            }
            "setSpecialDanmakuVisibility" -> { // 设置是否显示特殊弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setSpecialDanmakuVisibility", "flag" to false, "msg" to "设置是否显示特殊弹幕失败：无法获取设置是否显示特殊弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setSpecialDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setSpecialDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setSpecialDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setSpecialDanmakuVisibility", "flag" to false, "msg" to "设置是否显示特殊弹幕失败：$e"))
                }
            }
            "setColorsDanmakuVisibility" -> { // 是否显示彩色弹幕
                try {
                    val visible: Boolean? = call.argument<Boolean>("visible")
                    if (visible == null) {
                        result.success(mapOf("method" to "setColorsDanmakuVisibility", "flag" to false, "msg" to "设置是否显示彩色弹幕失败：无法获取设置是否显示彩色弹幕值"))
                    } else {
                        FlutterDanmakuViewUtils.setColorsDanmakuVisibility(visible)
                        result.success(mapOf("method" to "setColorsDanmakuVisibility", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setColorsDanmakuVisibility error: $e")
                    result.success(mapOf("method" to "setColorsDanmakuVisibility", "flag" to false, "msg" to "设置是否显示彩色弹幕失败：$e"))
                }
            }
            "setAllowOverlap" -> { // 设置是否允许重叠
                try {
                    val flag: Boolean? = call.argument<Boolean>("flag")
                    if (flag == null) {
                        result.success(mapOf("method" to "setAllowOverlap", "flag" to false, "msg" to "设置是否允许重叠失败：无法获取设置是否允许重叠值"))
                    } else {
                        FlutterDanmakuViewUtils.setAllowOverlap(flag)
                        result.success(mapOf("method" to "setAllowOverlap", "flag" to true, "msg" to ""))
                    }
                } catch (e: Exception) {
                    Log.e(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_LOG_TAG, "setAllowOverlap error: $e")
                    result.success(mapOf("method" to "setAllowOverlap", "flag" to false, "msg" to "设置是否允许重叠失败：$e"))
                }
            }
            else -> {
                result.notImplemented()
            }
        }
    }


}