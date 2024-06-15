package com.lchj.flutter_android_danmaku

import androidx.annotation.NonNull
import com.lchj.flutter_android_danmaku.danmaku.FlutterDanmakuViewFactory
import com.lchj.flutter_android_danmaku.danmaku.FlutterDanmakuViewUtils

import io.flutter.embedding.engine.plugins.FlutterPlugin


/** FlutterAndroidDanmakuPlugin */
class FlutterAndroidDanmakuPlugin: FlutterPlugin {

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    flutterPluginBinding.platformViewRegistry.registerViewFactory(FlutterDanmakuConstant.FLUTTER_ANDROID_DANMAKU_VIEW_ID, FlutterDanmakuViewFactory(flutterPluginBinding.binaryMessenger))
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    FlutterDanmakuViewUtils.dispose()
  }
}
