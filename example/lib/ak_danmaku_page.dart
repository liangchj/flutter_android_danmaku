import 'package:flutter/material.dart';
import 'package:flutter_android_danmaku/flutter_android_danmaku.dart';
import 'package:get/get.dart';

class AKDanmakuPage extends StatefulWidget {
  const AKDanmakuPage({super.key});

  @override
  State<AKDanmakuPage> createState() => _AKDanmakuPageState();
}

class _AKDanmakuPageState extends State<AKDanmakuPage> {
  late DanmakuViewController controller;
  var _fixedTopShow = false.obs;
  var _l2RShow = false.obs;
  @override
  Widget build(BuildContext context) {
    debugPrint("页面重绘或构建");
    return Scaffold(
      body: Column(
        children: [
          Padding(
            padding: EdgeInsets.only(bottom: 20),
          ),
          // TextButton(
          //     onPressed: () {
          //       controller.danmaKuSeekTo(3000);
          //     },
          //     child: const Text("弹幕跳转")),
          // TextButton(
          //     onPressed: () {
          //       debugPrint("时间：${controller.danmakuCurrentTime()}");
          //     },
          //     child: const Text("获取当前时间")),
          // TextButton(
          //     onPressed: () {
          //       controller.setDanmakuAlphaRatio(0.5);
          //     },
          //     child: const Text("透明的")),
          TextButton(
              onPressed: () {
                controller.sendDanmaku("发送弹幕，默认样式");
              },
              child: const Text("发送弹幕（默认样式）")),
          TextButton(
              onPressed: () {
                controller.sendDanmaku("发送弹幕，指定样式",
                    textColor: 9022215, borderColor: 2236962, danmakuType: 7);
              },
              child: const Text("发送弹幕（指定样式）")),
          Row(
            children: [
              Obx(
                () => TextButton(
                    onPressed: () {
                      controller
                          .setFixedTopDanmakuVisibility(!_fixedTopShow.value);
                      _fixedTopShow(!_fixedTopShow.value);
                    },
                    child: Text("${_fixedTopShow.value ? '隐藏' : '显示'}顶部固定弹幕")),
              ),
              Obx(
                () => TextButton(
                    onPressed: () {
                      controller.setL2RDanmakuVisibility(!_l2RShow.value);
                      _l2RShow(!_l2RShow.value);
                    },
                    child: Text("${_l2RShow.value ? '隐藏' : '显示'}左向右滚动弹幕")),
              ),
            ],
          ),
          Row(
            children: [
              TextButton(
                  onPressed: () {
                    controller.setDanmakuDisplayArea(0.25);
                  },
                  child: const Text("1/4显示区域")),
              TextButton(
                  onPressed: () {
                    controller.setDanmakuDisplayArea(0.5);
                  },
                  child: const Text("半屏显示区域")),
              TextButton(
                  onPressed: () {
                    controller.setDanmakuDisplayArea(0.75);
                  },
                  child: const Text("3/4显示区域")),
            ],
          ),
          TextButton(
              onPressed: () {
                controller.setDanmakuDisplayArea(1.0);
              },
              child: const Text("全屏显示区域")),
          Container(
            color: Colors.amberAccent,
            child: AspectRatio(
              aspectRatio: 16 / 10.0,
              child: FlutterAndroidDanmakuView(
                onViewCreated: (c) {
                  controller = c;
                },
                danmakuOptions: DanmakuOptions(
                    androidDanmakuType: AndroidDanmakuType.akDanmaku,
                    // danmakuPath: "/storage/sdcard0/1/1.xml",
                    danmakuPath: "/storage/emulated/0/1.xml",
                    isStart: true,
                    isShowFPS: true,
                    isShowCache: true,),
              ),
            ),
          )
        ],
      ),
    );
  }
}
