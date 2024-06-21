import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_android_danmaku/flutter_android_danmaku.dart';
import 'package:flutter_android_danmaku_example/ak_danmaku_page.dart';
import 'package:flutter_android_danmaku_example/danmaku_flame_master_page.dart';
import 'package:flutter_android_danmaku_example/permission_utils.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const GetMaterialApp(
    home: MyApp(),
  ));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  // 是否已经申请权限
  bool _requestPermission = false;

  @override
  void initState() {
    if (!_requestPermission) {
      _handleRequestPermission();
    }
    super.initState();
  }

  /// 申请权限
  void _handleRequestPermission() {
    List<Permission> permissionList = [
      Permission.storage,
      Permission.mediaLibrary,
      Permission.manageExternalStorage
    ];
    PermissionUtils.checkPermission(
        permissionList: permissionList,
        onPermissionCallback: (flag) {
          debugPrint("flag: $flag");
          setState(() {
            _requestPermission = flag;
          });
        });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              TextButton(
                  onPressed: () {
                    Get.to(const DanmakuFlameMasterPage());
                    // Navigator.push(
                    //   context,
                    //   MaterialPageRoute(builder: (context) {
                    //     return const DanmakuFlameMasterPage();
                    //   }),
                    // );
                  },
                  child: const Text("烈焰弹幕使")),

              TextButton(
                  onPressed: () {
                    Get.to(const AKDanmakuPage());
                  },
                  child: const Text("akDanmaku")),
            ],
          ),
        ),
      ),
    );
  }
}
