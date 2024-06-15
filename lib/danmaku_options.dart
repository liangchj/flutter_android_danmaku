class DanmakuOptions {
  final String? danmakuPath;
  // 解析完是否直接启动
  final bool? isStart;
  // 是否显示FPS
  final bool? isShowFPS;
  // 是否显示缓存信息
  final bool? isShowCache;

  // 设置是否允许重叠
  final bool? allowOverlap;
  // 是否显示顶部弹幕
  final bool? fixedTopDanmakuVisibility;
  // 是否显示底部弹幕
  final bool? fixedBottomDanmakuVisibility;
  // 是否显示从左到右滚动弹幕
  final bool? l2RDanmakuVisibility;
  // 是否显示从右到左滚动弹幕
  final bool? r2LDanmakuVisibility;
  // 是否显示特殊弹幕
  final bool? specialDanmakuVisibility;
  // 是否启用合并重复弹幕
  final bool? duplicateMergingEnable;
  // 是否显示彩色弹幕
  final bool? colorsDanmakuVisibility;

  // 设置描边样式
  final double? danmakuStyleStroke;
  // 弹幕透明度
  final double? danmakuAlphaRatio;
  // 显示区域
  final double? danmakuDisplayArea;
  // 弹幕字号（百分比）
  final double? danmakuFontSizeRatio;
  // 弹幕速度
  final double? danmakuSpeed;

  DanmakuOptions({
    this.danmakuPath,
    this.isStart,
    this.isShowFPS,
    this.isShowCache,
    this.allowOverlap,
    this.fixedTopDanmakuVisibility,
    this.fixedBottomDanmakuVisibility,
    this.l2RDanmakuVisibility,
    this.r2LDanmakuVisibility,
    this.specialDanmakuVisibility,
    this.duplicateMergingEnable,
    this.colorsDanmakuVisibility,
    this.danmakuStyleStroke,
    this.danmakuAlphaRatio,
    this.danmakuDisplayArea,
    this.danmakuFontSizeRatio,
    this.danmakuSpeed,
  });

  Map<String, dynamic> toJson() => {
    "danmakuPath": danmakuPath,
    "isStart": isStart,
    "isShowFPS": isShowFPS,
    "isShowCache": isShowCache,
    "allowOverlap": allowOverlap,
    "fixedTopDanmakuVisibility": fixedTopDanmakuVisibility,
    "fixedBottomDanmakuVisibility": fixedBottomDanmakuVisibility,
    "l2RDanmakuVisibility": l2RDanmakuVisibility,
    "r2LDanmakuVisibility": r2LDanmakuVisibility,
    "specialDanmakuVisibility": specialDanmakuVisibility,
    "duplicateMergingEnable": duplicateMergingEnable,
    "colorsDanmakuVisibility": colorsDanmakuVisibility,
    "danmakuStyleStroke": danmakuStyleStroke,
    "danmakuAlphaRatio": danmakuAlphaRatio,
    "danmakuDisplayArea": danmakuDisplayArea,
    "danmakuFontSizeRatio": danmakuFontSizeRatio,
    "danmakuSpeed": danmakuSpeed,

  };
}
