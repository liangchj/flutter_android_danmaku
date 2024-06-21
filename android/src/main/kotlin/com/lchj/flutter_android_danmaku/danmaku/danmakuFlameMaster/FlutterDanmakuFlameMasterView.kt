package com.lchj.flutter_android_danmaku.danmaku.danmakuFlameMaster

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.lchj.flutter_android_danmaku.FlutterDanmakuConstant
import io.flutter.Log
import master.flame.danmaku.controller.DrawHandler
import master.flame.danmaku.controller.IDanmakuView
import master.flame.danmaku.danmaku.loader.ILoader
import master.flame.danmaku.danmaku.loader.IllegalDataException
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.model.BaseDanmaku
import master.flame.danmaku.danmaku.model.DanmakuTimer
import master.flame.danmaku.danmaku.model.Duration
import master.flame.danmaku.danmaku.model.IDanmakus
import master.flame.danmaku.danmaku.model.IDisplayer
import master.flame.danmaku.danmaku.model.android.DanmakuContext
import master.flame.danmaku.danmaku.model.android.DanmakuFactory
import master.flame.danmaku.danmaku.model.android.Danmakus
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.ui.widget.DanmakuView
import org.apache.commons.collections4.MapUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * 哔哩哔哩弹幕VIEW（烈焰弹幕使）
 */
class FlutterDanmakuFlameMasterView(
    context: Context?,
    private var danmakuPath: String?,
    args: Map<String, Any>
) : com.lchj.flutter_android_danmaku.danmaku.IFlutterDanmakuView {

    private var mDanmakuView : DanmakuView
    //创建弹幕上下文
    private val mContext : DanmakuContext = DanmakuContext.create()
    // 弹幕解析器
    private var mParser: BaseDanmakuParser? = null

    // 弹幕配置

    // 解析完是否直接启动
    private var isStart: Boolean = true
    // 是否显示FPS
    private var isShowFPS: Boolean = false
    // 是否显示缓存信息
    private var isShowCache: Boolean = false

    // 设置是否允许重叠
    private var allowOverlap: Boolean = true

    // 设置描边样式
    private var danmakuStyleStroke: Float = 1.0f
    // 弹幕透明度
    private var danmakuAlphaRatio : Float = 1.0f
    // 显示区域
    private var danmakuDisplayArea: Float = 1.0f
    // 弹幕字号（百分比）
    private var danmakuFontSizeRatio: Float = 1.0f
    // 弹幕速度
    private var danmakuSpeed: Float = 1.0f

    /**
     * 弹幕显示隐藏设置
     */
    // 是否显示顶部弹幕
    private var fixedTopDanmakuVisibility: Boolean = true
    // 是否显示底部弹幕
    private var fixedBottomDanmakuVisibility: Boolean = true
    // 是否显示滚动弹幕
//    private var rollDanmakuVisibility: Boolean = true
    private var l2RDanmakuVisibility: Boolean = true
    private var r2LDanmakuVisibility: Boolean = true
    // 是否显示特殊弹幕
    private var specialDanmakuVisibility: Boolean = true
    // 是否启用合并重复弹幕
    private var duplicateMergingEnable: Boolean = false

    // 是否显示彩色弹幕
    private var colorsDanmakuVisibility: Boolean = true

    // 同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕
    private var maxNumInScreen: Int = -1
    // 最大显示行数
    private var maxLinesLimit: Int? = null

    private var mViewWidth: Float = 0f
    private var mViewHeight: Float = 0f

    // 启动位置
    private var startPosition: Long? = null

    init {
        isStart = MapUtils.getBoolean(args, "isStart", isStart)
        isShowFPS = MapUtils.getBoolean(args, "isShowFPS", isShowFPS)
        isShowCache = MapUtils.getBoolean(args, "isShowCache", isShowCache)
        allowOverlap = MapUtils.getBoolean(args, "allowOverlap", allowOverlap)
        danmakuStyleStroke = MapUtils.getFloat(args, "danmakuStyleStroke", danmakuStyleStroke)
        danmakuAlphaRatio = MapUtils.getFloat(args, "danmakuAlphaRatio", danmakuAlphaRatio)
        danmakuDisplayArea = MapUtils.getFloat(args, "danmakuDisplayArea", danmakuDisplayArea)
        danmakuFontSizeRatio = MapUtils.getFloat(args, "danmakuFontSizeRatio", danmakuFontSizeRatio)
        danmakuSpeed = MapUtils.getFloat(args, "danmakuSpeed", danmakuSpeed)

        maxNumInScreen = MapUtils.getInteger(args, "maxNumInScreen", maxNumInScreen)
        maxLinesLimit = MapUtils.getInteger(args, "maxNumInScreen", maxLinesLimit)

        duplicateMergingEnable = MapUtils.getBoolean(args, "duplicateMergingEnabled", duplicateMergingEnable)
        fixedTopDanmakuVisibility = MapUtils.getBoolean(args, "fixedTopDanmakuVisibility", fixedTopDanmakuVisibility)
        l2RDanmakuVisibility = MapUtils.getBoolean(args, "l2RDanmakuVisibility", l2RDanmakuVisibility)
        r2LDanmakuVisibility = MapUtils.getBoolean(args, "R2LDanmakuVisibility", r2LDanmakuVisibility)
        fixedBottomDanmakuVisibility = MapUtils.getBoolean(args, "fixedBottomDanmakuVisibility", fixedBottomDanmakuVisibility)
        specialDanmakuVisibility = MapUtils.getBoolean(args, "specialDanmakuVisibility", specialDanmakuVisibility)
        colorsDanmakuVisibility = MapUtils.getBoolean(args, "colorsDanmakuVisibility", colorsDanmakuVisibility)

        startPosition = MapUtils.getLong(args, "startPosition", startPosition)

        mDanmakuView = DanmakuView(context)
        setSetting(context)
    }

    override fun getView(): View {
        return mDanmakuView
    }

    /**
     * 销毁view
     */
    override fun dispose() {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "销毁view")
        try {
            mDanmakuView.release()
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "销毁view失败")
        }
    }

    /**
     * 弹幕设置
     */
    private fun setSetting(context: Context?) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "弹幕设置")
        val maxLInesPair: Map<Int, Int>? = if (maxLinesLimit == null) null else mapOf(
            BaseDanmaku.TYPE_SCROLL_RL to maxLinesLimit!!,
            BaseDanmaku.TYPE_SCROLL_LR to maxLinesLimit!!,
            BaseDanmaku.TYPE_FIX_TOP to maxLinesLimit!!,
            BaseDanmaku.TYPE_FIX_BOTTOM to maxLinesLimit!!
        )

        // 设置是否禁止重叠
        val overlappingEnablePair : Map<Int, Boolean> = hashMapOf(BaseDanmaku.TYPE_SCROLL_RL to allowOverlap, BaseDanmaku.TYPE_FIX_TOP to allowOverlap)

        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, danmakuStyleStroke) // 设置描边样式
            .setDanmakuTransparency(danmakuAlphaRatio) // 透明的
            .setDuplicateMergingEnabled(duplicateMergingEnable) // 设置是否启用合并重复弹幕
            .setFTDanmakuVisibility(fixedTopDanmakuVisibility) // 是否显示顶部弹幕
            .setFBDanmakuVisibility(fixedBottomDanmakuVisibility) // 是否显示底部弹幕
            .setL2RDanmakuVisibility(l2RDanmakuVisibility) // 是否显示左右滚动弹幕
            .setR2LDanmakuVisibility(r2LDanmakuVisibility) // 是否显示右左滚动弹幕
            .setSpecialDanmakuVisibility(specialDanmakuVisibility) // 是否显示特殊弹幕
            // 设置弹幕滚动速度
            .setScrollSpeedFactor(danmakuSpeed) // 设置弹幕滚动速度系数,只对滚动弹幕有效
            .setScaleTextSize(danmakuFontSizeRatio) // 弹幕字号
            //设置缓存绘制填充器，默认使用SimpleTextCacheStuffer只支持纯文字显示,
            // 如果需要图文混排请设置SpannedCacheStuffer 如果需要定制其他样式请扩展SimpleTextCacheStuffer|SpannedCacheStuffer
            .setCacheStuffer(SpannedCacheStuffer(), null)
            .setMaximumLines(maxLInesPair) // 设置最大显示行数
            .setMaximumLines(null) // 设置最大显示行数
            .preventOverlapping(overlappingEnablePair) // 设置防弹幕重叠
            .setMaximumVisibleSizeInScreen(maxNumInScreen) // 同屏弹幕数量 -1 按绘制效率自动调整 0 无限制 n 同屏最大显示n个弹幕

        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView not null")
        //mParser = createParser(context!!.openFileInput("C:\\Users\\lcj\\Desktop\\danmu.json"))
        var inStream: InputStream? = null
        if (!danmakuPath.isNullOrEmpty()) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "弹幕库中的danmakuPath不为空")
            //打开文件
            val file = danmakuPath?.let { File(it) }
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "弹幕库中的danmakuPath是否是文件或存在：file.exists():${file?.exists()}, file.isFile: ${file?.isFile}")
            if (file != null && file.exists() && file.isFile) {
                inStream = FileInputStream(file)
            }
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "inStream: ${inStream?.toString()}")
        if (inStream != null) {
            mParser = createParser(inStream)
        }

        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "xml："+context!!.resources)
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mParser：$mParser")
        try {
            mDanmakuView.setCallback(object : DrawHandler.Callback {
                override fun updateTimer(timer: DanmakuTimer) {}
                override fun drawingFinished() {}
                override fun danmakuShown(danmaku: BaseDanmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                override fun prepared() {
                    Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "准备完成， width：${mDanmakuView.layoutParams.width}, height: ${mDanmakuView.layoutParams.height}")
                    mViewWidth = mDanmakuView.layoutParams.width.toFloat()
                    mViewHeight = mDanmakuView.layoutParams.height.toFloat()
                    if (danmakuDisplayArea != 1.0f) {
                        mDanmakuView.layoutParams = FrameLayout.LayoutParams(mViewWidth.toInt(), (mViewHeight * danmakuDisplayArea).toInt())
                    }
                    if (isStart) {
                        try {
                            mDanmakuView.start(startPosition ?: 0L)
                        } catch (e1: Exception) {
                            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView.setCallback中启动报错：$e1")
                        }
                    }
                    Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView start")
                }

            })
        } catch (e: Exception) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView.setCallback报错：$e")
        }
        try {
            mDanmakuView.onDanmakuClickListener = object : IDanmakuView.OnDanmakuClickListener {
                override fun onDanmakuClick(danmakus: IDanmakus): Boolean {
                    Log.d(
                        FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG,
                        "onDanmakuClick: danmakus size: ${danmakus.size()}"
                    )
                    val latest = danmakus.last()
                    if (null != latest) {
                        Log.d(
                            FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG,
                            "onDanmakuClick: text of latest danmaku: ${latest.text}"
                        )
                        return true
                    }
                    return false
                }

                override fun onDanmakuLongClick(danmakus: IDanmakus): Boolean {
                    return false
                }

                override fun onViewClick(view: IDanmakuView): Boolean {
                    Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "点击了弹幕内容：")
                    return false
                }
            }
        } catch (e : Exception) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "IDanmakuView.OnDanmakuClickListener报错：$e")
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView prepare")
        mDanmakuView.prepare(mParser, mContext)
        if (isShowCache) {
            try {
                mDanmakuView.showFPS(true)
            } catch (e: Exception) {
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView.showFPS报错：$e")
            }
        }
        if (isShowCache) {
            try {
                mDanmakuView.enableDanmakuDrawingCache(true)
            } catch (e: Exception) {
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "mDanmakuView.enableDanmakuDrawingCache报错：$e")
            }
        }
    }

    /**
     * 创建解析器
     */
    private fun createParser(stream : InputStream) : BaseDanmakuParser{
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "createParser")
        if (stream == null) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "stream is null")
            return object : BaseDanmakuParser() {
                override fun parse(): Danmakus {
                    return Danmakus()
                }
            }
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "stream not null，准备读取xml")
        val loader : ILoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "loader加载：$loader")
        try {
            loader.load(stream)
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "读取xml")
        } catch (e : IllegalDataException) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "解析失败")
            e.printStackTrace()
        }
        val parser : BaseDanmakuParser = DanmakuFlameMasterParser()
        val dataSource = loader.dataSource
        parser.load(dataSource)
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "parser:$parser")
        return parser
    }

    /**
     * 启动弹幕
     */
    override fun startDanmaku(position: Long?) {
        if (mDanmakuView.isPrepared) {
            try {
                if (position == null) {
                    mDanmakuView.start()
                } else {
                    mDanmakuView.start(position)
                }
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "startDanmaku error: $e")
            }
        }
    }

    /**
     * 暂停弹幕
     */
    override fun pauseDanmaKu() {
        if (mDanmakuView.isPrepared && !mDanmakuView.isPaused) {
            try {
                mDanmakuView.pause()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "pauseDanmaKu error: $e")
            }
        }
    }

    /**
     * 继续弹幕
     */
    override fun resumeDanmaku() {
        if (mDanmakuView.isPrepared && mDanmakuView.isPaused) {
            try {
                mDanmakuView.resume()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "resumeDanmaku error: $e")
            }
        }
    }

    /**
     * 发送弹幕
     */
    override fun sendDanmaku(isLive: Boolean, text: String, time: Long?, danmakuType: Int?,
                             padding: Int?, textSize: Float?, textColor: Int?,
                             textShadowColor: Int?, underlineColor: Int?, borderColor: Int?) {
        var danmakuMode: Int = BaseDanmaku.TYPE_SCROLL_RL
        if (danmakuType != null && (danmakuType == BaseDanmaku.TYPE_FIX_TOP || danmakuType == BaseDanmaku.TYPE_FIX_BOTTOM
                    || danmakuType == BaseDanmaku.TYPE_SCROLL_RL || danmakuType == BaseDanmaku.TYPE_SPECIAL)) {
            danmakuMode = danmakuType
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "sendDanmaku 最终发送类型: $danmakuMode")
        val danmaku = mContext.mDanmakuFactory.createDanmaku(danmakuMode) ?: return
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "sendDanmaku 创建发送弹幕Item成功: $danmaku")
        danmaku.text = text
        danmaku.padding = padding ?: 5
        danmaku.priority = 0 // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = isLive
        danmaku.time = time ?: (mDanmakuView.currentTime + 1200)
        danmaku.textSize = textSize ?: (25f * (mParser!!.displayer.density - 0.6f))
        danmaku.textColor = textColor ?: Color.RED
        danmaku.textShadowColor = textShadowColor ?: Color.WHITE
        danmaku.underlineColor = underlineColor ?: Color.GREEN;
        danmaku.borderColor = borderColor ?: Color.GREEN
        if (danmaku.duration == null) {
            danmaku.duration = Duration((DanmakuFactory.COMMON_DANMAKU_DURATION * mContext.scrollSpeedFactor).toLong())
        }
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "sendDanmaku 执行发送弹幕: $danmaku, ${danmaku.duration.value}, ${danmaku.time}")
        mDanmakuView.addDanmaku(danmaku)
    }

    /**
     * 获取当前弹幕时间
     */
    override fun danmakuCurrentTime() : Long {
        var currentTime: Long? = null
        if (mDanmakuView.isPrepared) {
            currentTime =  mDanmakuView.currentTime
        }
        return currentTime ?: 0
    }

    /**
     * 弹幕跳转
     */
    override fun danmaKuSeekTo(position: Long) {
        if (mDanmakuView.isPrepared) {
            try {
                mDanmakuView.seekTo(position)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "danmaKuSeekTo error: $e")
            }
        }
    }
    /**
     * 显示或隐藏
     */
    override fun setDanmaKuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                if (visible) {
                    if (!mDanmakuView.isShown) {
                        mDanmakuView.show()
                    }
                } else {
                    if (mDanmakuView.isShown) {
                        mDanmakuView.hide()
                    }
                }
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmaKuVisibility error: $e")
            }
        }
    }

    /***
     * 设置弹幕透明的
     */
    override fun setDanmakuAlphaRatio(danmakuAlphaRatio: Float) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.setDanmakuTransparency(danmakuAlphaRatio)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuAlphaRatio error: $e")
            }
        }
    }

    /**
     * 设置显示区域
     */
    override fun setDanmakuDisplayArea(area: Float) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "进入设置显示区域 area: $area，mDanmakuView.layoutParams.width：${mDanmakuView.layoutParams.width}，mDanmakuView.layoutParams.height：${mDanmakuView.layoutParams.height}")
        if (mDanmakuView.isPrepared) {
            try {
                mDanmakuView.layoutParams = FrameLayout.LayoutParams(mViewWidth.toInt(), (mViewHeight * area).toInt())
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "设置显示区域 error: $e")
            }
        }
    }

    /**
     * 设置弹幕文字大小（百分比）
     */
    override fun setDanmakuFontSize(fontSizeRatio: Float) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuScaleTextSize fontSize: ${danmakuFontSizeRatio}, mDanmakuView.isPrepared:${mDanmakuView.isPrepared}")
        if (mDanmakuView.isPrepared) {
            try {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuScaleTextSize fontSizeRatio: $fontSizeRatio")
                mContext.setScaleTextSize(fontSizeRatio)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuScaleTextSize error: $e")
            }
        }
    }

    /**
     * 设置弹幕滚动速度
     */
    override fun setDanmakuSpeed(danmakuSpeed: Float) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.setScrollSpeedFactor(danmakuSpeed)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuSpeed error: $e")
            }
        }
    }

    /**
     * 设置弹幕描边
     */
    override fun setDanmakuStroke(stroke: Float) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, stroke) // 设置描边样式
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDanmakuStroke error: $e")
            }
        }
    }

    /**
     * 设置是否启用合并重复弹幕
     */
    override fun setDuplicateMergingEnabled(merge: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDuplicateMergingEnabled，进入设置是否启用合并重复弹幕，merge：$merge")
        if (mDanmakuView.isPrepared) {
            try {
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDuplicateMergingEnabled，执行设置是否启用合并重复弹幕，merge：$merge")
                mContext.isDuplicateMergingEnabled = merge
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setDuplicateMergingEnabled error: $e")
            }
        }
    }

    /**
     * 设置是否显示顶部固定弹幕
     */
    override fun setFixedTopDanmakuVisibility(visible: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setFixedTopDanmakuVisibility，进入设置是否显示顶部固定弹幕，visible：$visible")
        if (mDanmakuView.isPrepared) {
            try {
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setFixedTopDanmakuVisibility，执行设置是否显示顶部固定弹幕，visible：$visible")
                mContext.ftDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setFTDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示底部固定弹幕
     */
    override fun setFixedBottomDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.fbDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setFBDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示滚动弹幕
     */
//    override fun setRollDanmakuVisibility(visible: Boolean) {
//        Log.d(DanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setL2RDanmakuVisibility visible: $visible, isPrepared: ${mDanmakuView.isPrepared}")
//        if (mDanmakuView.isPrepared) {
//            Log.d(DanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setL2RDanmakuVisibility entry")
//            try {
//                mContext.L2RDanmakuVisibility = visible
//                mContext.R2LDanmakuVisibility = visible
//                mDanmakuView.invalidate()
//            } catch (e: Exception) {
//                Log.e(DanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setL2RDanmakuVisibility error: $e")
//            }
//        }
//    }

    /**
     * 设置是否显示从左向右滚动弹幕
     */
    override fun setL2RDanmakuVisibility(visible: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setL2RDanmakuVisibility 进入设置是否显示从左向右滚动弹幕，visible: $visible, isPrepared: ${mDanmakuView.isPrepared}，mContext: $mContext")
        if (mDanmakuView.isPrepared) {
            try {
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "ssetL2RDanmakuVisibility，执行设置是否显示从左向右滚动弹幕，visible: $visible, mContext.L2RDanmakuVisibility: ${mContext.L2RDanmakuVisibility}")
                mContext.L2RDanmakuVisibility = visible
                mDanmakuView.invalidate()
                Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "ssetL2RDanmakuVisibility，执行设置是否显示从左向右滚动弹幕之后，mContext.L2RDanmakuVisibility: ${mContext.L2RDanmakuVisibility}")
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setL2RDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示从右向左滚动弹幕
     */
    override fun setR2LDanmakuVisibility(visible: Boolean) {
        Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setR2LDanmakuVisibility visible: $visible, isPrepared: ${mDanmakuView.isPrepared}")
        if (mDanmakuView.isPrepared) {
            Log.d(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setR2LDanmakuVisibility entry")
            try {
                mContext.R2LDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setR2LDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示特殊弹幕
     */
    override fun setSpecialDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.SpecialDanmakuVisibility = visible
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setSpecialDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 是否显示彩色弹幕
     */
    override fun setColorsDanmakuVisibility(visible: Boolean) {
        if (mDanmakuView.isPrepared) {
            try {
                if (visible) {
                    mContext.setColorValueWhiteList(16777215, 16646914, 16740868, 16755202, 16765698, 16776960, 10546688, 52480, 104601,
                        4351678, 9022215, 13369971, 2236962, 10197915)
                } else {
                    mContext.setColorValueWhiteList(16777215)
                }
                mDanmakuView.invalidate()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }
    /**
     * 设置是否允许重叠
     */
    override fun setAllowOverlap(flag: Boolean) {
        if (mDanmakuView.isPrepared) {
            // 设置是否禁止重叠
            val overlappingEnablePair : Map<Int, Boolean> = hashMapOf(BaseDanmaku.TYPE_SCROLL_RL to flag, BaseDanmaku.TYPE_FIX_TOP to flag)
            try {
                mContext.preventOverlapping(overlappingEnablePair)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置最大显示行数
     * 设置null取消行数限制
     * pairs – map  设置null取消行数限制 K = (BaseDanmaku.TYPE_SCROLL_RL|BaseDanmaku.TYPE_SCROLL_LR|BaseDanmaku.TYPE_FIX_TOP|BaseDanmaku.TYPE_FIX_BOTTOM) V = 最大行数
     */
    override fun setMaximumLines(lines: Int?) {
        if (mDanmakuView.isPrepared) {
            try {
                if (lines == null) {
                    mContext.setMaximumLines(null);
                } else {
                    val limitMap: Map<Int, Int> = mapOf(
                        BaseDanmaku.TYPE_SCROLL_RL to lines,
                        BaseDanmaku.TYPE_SCROLL_LR to lines,
                        BaseDanmaku.TYPE_FIX_TOP to lines,
                        BaseDanmaku.TYPE_FIX_BOTTOM to lines
                    );
                    mContext.setMaximumLines(limitMap)
                }
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setMaximumLines error: $e")
            }
        }
    }

    /**
     * 设置同屏弹幕密度 -1自动 0无限制  n 同屏最大显示n个弹幕
     *
     * @param maxSize
     */
    override fun setMaximumVisibleSizeInScreen(maxSize: Int) {
        if (mDanmakuView.isPrepared) {
            try {
                mContext.setMaximumVisibleSizeInScreen(maxSize)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.DANMAKU_FLAME_MASTER_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }

}