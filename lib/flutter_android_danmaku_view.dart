import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:flutter_android_danmaku/danmaku_options.dart';

typedef OnViewCreated = Function(DanmakuViewController);

class FlutterAndroidDanmakuView extends StatefulWidget {
  const FlutterAndroidDanmakuView(
      {super.key, required this.onViewCreated, this.danmakuOptions});
  final OnViewCreated onViewCreated;
  final DanmakuOptions? danmakuOptions;

  @override
  State<FlutterAndroidDanmakuView> createState() =>
      _FlutterAndroidDanmakuViewState();
}

class _FlutterAndroidDanmakuViewState extends State<FlutterAndroidDanmakuView> {
  late MethodChannel _channel;
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return AndroidView(
      viewType: "ANDROID/FLUTTER_ANDROID_DANMAKU_VIEW_ID",
      creationParams: widget.danmakuOptions?.toJson(),
      onPlatformViewCreated: _onPlatformViewCreated,
      creationParamsCodec: const StandardMessageCodec(),
      hitTestBehavior: PlatformViewHitTestBehavior.transparent,
    );
  }

  void _onPlatformViewCreated(int id) {
    _channel = const MethodChannel('FLUTTER_ANDROID_DANMAKU_METHOD_CHANNEL');
    final controller = DanmakuViewController._(
      _channel,
    );
    widget.onViewCreated(controller);
  }
}

class DanmakuViewController {
  final MethodChannel _channel;
  final StreamController<dynamic> _controller = StreamController<dynamic>();

  DanmakuViewController._(
    this._channel,
  ) {
    _channel.setMethodCallHandler(
      (call) async {
        switch (call.method) {
          case 'AndroidViewCreateSuccess':
            // 从native端获取数据
            final result = call.arguments as bool;
            debugPrint("返回结果:$result");
            _controller.sink.add(result);
            break;
          case 'AndroidViewCreateFail':
            // 从native端获取数据
            final result = call.arguments;
            debugPrint("创建失败:$result");
            _controller.sink.add(result);
            break;
        }
      },
    );
  }
  ResultInfo _handleResult(String method, var result) {
    ResultInfo resultInfo = ResultInfo(flag: false, method: method, msg: "");
    if (result != null) {
      try {
        resultInfo = ResultInfo.fromJson(result);
      } catch (e) {
        debugPrint("$method，获取返回结果转换失败：$e");
        resultInfo.flag = false;
        resultInfo.msg = "获取返回结果转换失败：$e";
      }
    }
    debugPrint("结果：${resultInfo.toJson()}");
    return resultInfo;
  }

  Stream<dynamic> get customDataStream => _controller.stream;
  // 开始弹幕（毫秒）
  Future<ResultInfo> startDanmaku(int? startTime) async {
    var result = await _channel
        .invokeMethod('startDanmaku', {'time': startTime?.toString()});
    return _handleResult("startDanmaku", result);
  }

  // 暂停弹幕
  Future<ResultInfo> pauseDanmaKu() async {
    var result = await _channel.invokeMethod('pauseDanmaKu');
    return _handleResult("pauseDanmaKu", result);
  }

  // 继续弹幕
  Future<ResultInfo> resumeDanmaku() async {
    var result = await _channel.invokeMethod('resumeDanmaku');
    return _handleResult("resumeDanmaku", result);
  }

  // 发送弹幕
  // isLive: Boolean, text: String, time: Long?, danmakuType: Int?,
  //         padding: Int?, textSize: Float?, textColor: Int?,
  //         textShadowColor: Int?, underlineColor: Int?, borderColor: Int?
  Future<ResultInfo> sendDanmaku(String danmakuText,
      {bool isLive = false,
      String? time,
      int? danmakuType,
      int? padding,
      double? textSize,
      int? textColor,
      int? textShadowColor,
      int? underlineColor,
      int? borderColor}) async {
    var result = await _channel.invokeMethod('sendDanmaku', {
      'danmakuText': danmakuText,
      "isLive": isLive,
      "time": time,
      "danmakuType": danmakuType,
      "padding": padding,
      "textSize": textSize,
      "textColor": textColor,
      "textShadowColor": textShadowColor,
      "underlineColor": underlineColor,
      "borderColor": borderColor
    });
    return _handleResult("sendDanmaku", result);
  }

  // 获取当前弹幕时间
  Future<ResultInfo> danmakuCurrentTime() async {
    var result = await _channel.invokeMethod('danmakuCurrentTime');
    return _handleResult("danmakuCurrentTime", result);
  }

  // 弹幕跳转（毫秒）
  Future<ResultInfo> danmaKuSeekTo(int time) async {
    var result =
        await _channel.invokeMethod('danmaKuSeekTo', {'time': time.toString()});
    return _handleResult("danmaKuSeekTo", result);
  }

  // 显示或隐藏
  Future<ResultInfo> setDanmaKuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setDanmaKuVisibility', {'visible': visible});
    return _handleResult("setDanmaKuVisibility", result);
  }

  // 设置弹幕透明度
  Future<ResultInfo> setDanmakuAlphaRatio(double danmakuAlphaRatio) async {
    var result = await _channel.invokeMethod(
        'setDanmakuAlphaRatio', {'danmakuAlphaRatio': danmakuAlphaRatio});
    return _handleResult("setDanmakuAlphaRatio", result);
  }

  // 设置显示区域
  Future<ResultInfo> setDanmakuDisplayArea(double area) async {
    var result =
        await _channel.invokeMethod('setDanmakuDisplayArea', {'area': area});
    return _handleResult("setDanmakuDisplayArea", result);
  }

  // 设置字体大小
  Future<ResultInfo> setDanmakuFontSize(double fontSizeRatio) async {
    var result = await _channel
        .invokeMethod('setDanmakuFontSize', {'fontSizeRatio': fontSizeRatio});
    return _handleResult("setDanmakuFontSize", result);
  }

  // 设置滚动速度
  Future<ResultInfo> setDanmakuSpeed(double danmakuSpeed) async {
    var result = await _channel
        .invokeMethod('setDanmakuSpeed', {'danmakuSpeed': danmakuSpeed});
    return _handleResult("setDanmakuSpeed", result);
  }

  // 设置弹幕描边
  Future<ResultInfo> setDanmakuStroke(double stroke) async {
    var result =
        await _channel.invokeMethod('setDanmakuStroke', {'stroke': stroke});
    return _handleResult("setDanmakuStroke", result);
  }

  // 设置是否启用合并重复弹幕
  Future<ResultInfo> setDuplicateMergingEnabled(bool flag) async {
    var result = await _channel
        .invokeMethod('setDuplicateMergingEnabled', {'flag': flag});
    return _handleResult("setDuplicateMergingEnabled", result);
  }

  // 设置是否显示顶部固定弹幕
  Future<ResultInfo> setFixedTopDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setFixedTopDanmakuVisibility', {'visible': visible});
    return _handleResult("setFixedTopDanmakuVisibility", result);
  }

  // 设置是否显示从左向右滚动弹幕
  Future<ResultInfo> setL2RDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setL2RDanmakuVisibility', {'visible': visible});
    return _handleResult("setL2RDanmakuVisibility", result);
  }

  // 设置是否显示从右向左滚动弹幕
  Future<ResultInfo> setR2LDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setR2LDanmakuVisibility', {'visible': visible});
    return _handleResult("setR2LDanmakuVisibility", result);
  }

  // 设置是否显示底部固定弹幕
  Future<ResultInfo> setFixedBottomDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setFixedBottomDanmakuVisibility', {'visible': visible});
    return _handleResult("setFixedBottomDanmakuVisibility", result);
  }

  // 设置是否显示特殊弹幕
  Future<ResultInfo> setSpecialDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setSpecialDanmakuVisibility', {'visible': visible});
    return _handleResult("setSpecialDanmakuVisibility", result);
  }

  // 是否显示彩色弹幕
  Future<ResultInfo> setColorsDanmakuVisibility(bool visible) async {
    var result = await _channel
        .invokeMethod('setColorsDanmakuVisibility', {'visible': visible});
    return _handleResult("setColorsDanmakuVisibility", result);
  }

  // 设置是否允许重叠
  Future<ResultInfo> setAllowOverlap(bool flag) async {
    var result = await _channel.invokeMethod('setAllowOverlap', {'flag': flag});
    return _handleResult("setAllowOverlap", result);
  }

  // 设置最大显示行数
  // 设置null取消行数限制
  Future<ResultInfo> setMaximumLines({int? lines}) async {
    var result =
        await _channel.invokeMethod('setMaximumLines', {'lines': lines});
    return _handleResult("setMaximumLines", result);
  }

  // 设置同屏弹幕密度 -1自动 0无限制  n 同屏最大显示n个弹幕
  Future<ResultInfo> setMaximumVisibleSizeInScreen(int maxSize) async {
    var result = await _channel
        .invokeMethod('setMaximumVisibleSizeInScreen', {'maxSize': maxSize});
    return _handleResult("setMaximumVisibleSizeInScreen", result);
  }
}

class ResultInfo {
  bool flag;
  final String? method;
  dynamic msg;

  ResultInfo({required this.flag, this.method, this.msg});

  factory ResultInfo.fromJson(dynamic json) =>
      ResultInfo(flag: json["flag"], method: json["method"], msg: json["msg"]);

  Map<String, dynamic> toJson() => {"method": method, "flag": flag, "msg": msg};
}
