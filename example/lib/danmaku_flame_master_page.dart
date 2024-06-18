
import 'package:flutter/material.dart';
import 'package:flutter_android_danmaku/flutter_android_danmaku.dart';

class DanmakuFlameMasterPage extends StatefulWidget {
  const DanmakuFlameMasterPage({super.key});

  @override
  State<DanmakuFlameMasterPage> createState() => _DanmakuFlameMasterPageState();
}

class _DanmakuFlameMasterPageState extends State<DanmakuFlameMasterPage> {
  late DanmakuViewController controller;
  @override
  Widget build(BuildContext context) {
    debugPrint("页面重绘或构建");
    return Scaffold(
      body: Column(
        children: [
          Padding(padding: EdgeInsets.only(bottom: 20),),
          TextButton(onPressed: () {
            controller.danmaKuSeekTo(3000);
          }, child: const Text("弹幕跳转")),
          TextButton(onPressed: () {
            debugPrint("时间：${controller.danmakuCurrentTime()}");
          }, child: const Text("获取当前时间")),
          TextButton(onPressed: () {
            controller.setDanmakuAlphaRatio(0.5);
          }, child: const Text("透明的")),
          TextButton(onPressed: () {
            controller.sendDanmaku("发送弹幕，默认样式");
          }, child: const Text("发送弹幕（默认样式）")),
          TextButton(onPressed: () {
            controller.sendDanmaku("发送弹幕，指定样式", textColor: 9022215, borderColor: 2236962, danmakuType: 7);
          }, child: const Text("发送弹幕（指定样式）")),
          Container(
            color: Colors.amberAccent,
            child: AspectRatio(aspectRatio: 16 / 10.0, child: FlutterAndroidDanmakuView(
              onViewCreated: (c) {
                controller = c;
              },
              danmakuOptions: DanmakuOptions(
                  // danmakuPath: "/storage/sdcard0/1/1.xml",
                  danmakuPath: "/storage/emulated/0/1.xml",
                isStart: true,
                isShowFPS: true,
                isShowCache: true
              ),
            ),),
          )
        ],
      ),
    );
  }
}
