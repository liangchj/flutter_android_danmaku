package com.lchj.flutter_android_danmaku.danmaku

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FlutterDanmakuViewFactory (private val messenger : BinaryMessenger) : PlatformViewFactory(
    StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        return FlutterDanmakuView(context, messenger, viewId, args as Map<String, Any>?)
    }
}