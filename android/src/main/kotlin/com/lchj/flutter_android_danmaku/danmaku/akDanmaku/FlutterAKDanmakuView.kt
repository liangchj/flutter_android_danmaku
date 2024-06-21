package com.lchj.flutter_android_danmaku.danmaku.akDanmaku

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import com.kuaishou.akdanmaku.DanmakuConfig
import com.kuaishou.akdanmaku.data.DanmakuItemData
import com.kuaishou.akdanmaku.ecs.component.action.Actions
import com.kuaishou.akdanmaku.ecs.component.filter.BlockedTextFilter
import com.kuaishou.akdanmaku.ecs.component.filter.DanmakuDataFilter
import com.kuaishou.akdanmaku.ecs.component.filter.DanmakuFilter
import com.kuaishou.akdanmaku.ecs.component.filter.DanmakuFilters
import com.kuaishou.akdanmaku.ecs.component.filter.DanmakuLayoutFilter
import com.kuaishou.akdanmaku.ecs.component.filter.DuplicateMergedFilter
import com.kuaishou.akdanmaku.ecs.component.filter.GuestFilter
import com.kuaishou.akdanmaku.ecs.component.filter.TextColorFilter
import com.kuaishou.akdanmaku.ecs.component.filter.TypeFilter
import com.kuaishou.akdanmaku.ecs.component.filter.UserIdFilter
import com.kuaishou.akdanmaku.render.SimpleRenderer
import com.kuaishou.akdanmaku.ui.DanmakuView
import com.kuaishou.akdanmaku.ui.DanmakuPlayer
import com.lchj.flutter_android_danmaku.FlutterDanmakuConstant
import org.apache.commons.collections4.MapUtils
import kotlin.random.Random

class FlutterAKDanmakuView (
    context: Context?,
    private var danmakuPath: String?,
    args: Map<String, Any>
) : com.lchj.flutter_android_danmaku.danmaku.IFlutterDanmakuView {

    companion object {
        private const val MSG_CREATE_LOADED_DATA_END = 0 // 创建且加载弹幕结束
        private const val MSG_START = 1 // 启动弹幕
        private const val MSG_UPDATE_DATA = 2 // 加载弹幕
        private const val MSG_LOADED_DATA_SUCCESS = 3 // 加载弹幕成功
        private const val MSG_LOADED_DATA_FAIL = 4 // 加载弹幕失败
    }
    // 是否加载结束
    private var createdAndLoaded: Boolean = false

    private val danmakuView: DanmakuView
    private var danmakuPlayer: DanmakuPlayer
    private val simpleRenderer = SimpleRenderer()

    private val mainHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_CREATE_LOADED_DATA_END -> createdAndLoaded()
                MSG_START -> startDanmaku(null)
                MSG_UPDATE_DATA -> updateDanmakuData()
            }
        }
    }

    // 弹幕配置
    private var config: DanmakuConfig
    // 颜色过滤
    private val colorFilter = TextColorFilter()
    // 数据过滤
    private var dataFilters = emptyMap<Int, DanmakuFilter>()

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

        val that = this
        config = DanmakuConfig().apply {
            bold = danmakuStyleStroke > 1
            alpha = danmakuAlphaRatio
            dataFilter = createDataFilters()
            dataFilters = dataFilter.associateBy { it.filterParams }
            layoutFilter = createLayoutFilters()
            textSizeScale = danmakuFontSizeRatio
            allowOverlap = that.allowOverlap
            timeFactor = danmakuSpeed
            screenPart = danmakuDisplayArea
            preCacheTimeMs = startPosition ?: 100L
        }
        // 是否启用合并重复弹幕
        if (duplicateMergingEnable) {
            (dataFilters[DanmakuFilters.FILTER_TYPE_DUPLICATE_MERGED] as? TypeFilter)?.let { filter ->
                filter.addFilterItem(DanmakuItemData.MERGED_TYPE_MERGED)
                config.updateFilter()
            }
        }
        // 是否显示顶部弹幕
        if (!fixedTopDanmakuVisibility) {
            (dataFilters[DanmakuFilters.FILTER_TYPE_TYPE] as? TypeFilter)?.let { filter ->
                filter.addFilterItem(DanmakuItemData.DANMAKU_MODE_CENTER_TOP)
                config.updateFilter()
            }
        }
        // 是否显示底部弹幕
        if (!fixedBottomDanmakuVisibility) {
            (dataFilters[DanmakuFilters.FILTER_TYPE_TYPE] as? TypeFilter)?.let { filter ->
                filter.addFilterItem(DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM)
                config.updateFilter()
            }
        }
        // 是否显示滚动弹幕
        if (!l2RDanmakuVisibility || !r2LDanmakuVisibility) {
            (dataFilters[DanmakuFilters.FILTER_TYPE_TYPE] as? TypeFilter)?.let { filter ->
                filter.addFilterItem(DanmakuItemData.DANMAKU_MODE_ROLLING)
                config.updateFilter()
            }
        }
        // 是否显示彩色弹幕
        if (!colorsDanmakuVisibility) {
            colorFilter.filterColor.clear()
            colorFilter.filterColor.add(0xFFFFFF)
            config.updateFilter()
        }

        danmakuView = DanmakuView(context)
        danmakuPlayer = DanmakuPlayer(simpleRenderer).also {
            it.bindView(danmakuView)
        }

        if (!danmakuPath.isNullOrEmpty()) {
            mainHandler.sendEmptyMessageDelayed(MSG_UPDATE_DATA, 10)
        } else {
            // 标记弹幕创建结束
            mainHandler.sendEmptyMessage(MSG_CREATE_LOADED_DATA_END)
        }
    }

    override fun getView(): View {
        return danmakuView
    }

    /**
     * 销毁view
     */
    override fun dispose() {
        Log.d(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "销毁view")
        try {
            danmakuPlayer.release()
        } catch (e: Exception) {
            Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "销毁view失败")
        }
    }

    /**
     * 创建结束且加载弹幕文件结束
     */
    private fun createdAndLoaded() {
        if (isStart) {
            startDanmaku(startPosition)
        }
        createdAndLoaded = true
    }

    /**
     * 创建过滤
     */
    private fun createDataFilters(): List<DanmakuDataFilter> =
        listOf(
            TypeFilter(),
            colorFilter,
            UserIdFilter(),
            GuestFilter(),
            BlockedTextFilter { it == 0L },
            DuplicateMergedFilter()
        )

    private fun createLayoutFilters(): List<DanmakuLayoutFilter> = emptyList()

    /**
     * 从文件中加载弹幕数据
     */
    private fun updateDanmakuData() {
        Thread {
            val start: Long = System.currentTimeMillis()
            Log.d(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "开始加载数据, $start")
            var total: Long = 0
            try {
                var dataList: List<DanmakuItemData> = listOf()
                if (!danmakuPath.isNullOrEmpty()) {
                    val suffix: String = danmakuPath!!.split(".").last().lowercase()
                    if (suffix == "xml") {
                        dataList = AKDanmakuParser.biliXmlPullParser(danmakuPath!!)
                    } else if (suffix == "json") {
                        dataList = AKDanmakuParser.akJsonParser(danmakuPath!!)
                    }
                }
                val end: Long = System.currentTimeMillis()
                val takeUpTime: Long = end - start // 解析耗时
                Log.d(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "加载数据结束, $end，耗时: $takeUpTime")
                total = dataList.size.toLong()
                danmakuPlayer.updateData(dataList)
                mainHandler.sendEmptyMessage(MSG_LOADED_DATA_SUCCESS)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "加载数据失败: $e")
                mainHandler.sendEmptyMessage(MSG_LOADED_DATA_FAIL)
            } finally {
                // 标记文件加载结束
                mainHandler.sendEmptyMessage(MSG_CREATE_LOADED_DATA_END)
            }
            Log.d(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "数据已加载(count = $total)")
        }.start()
    }

    /**
     * 启动弹幕
     */
    override fun startDanmaku(position: Long?) {
        if (position != null) {
            danmakuPlayer.seekTo(position)
        }
        danmakuPlayer.start()
    }

    /**
     * 暂停弹幕
     */
    override fun pauseDanmaKu() {
        if (createdAndLoaded) {
            try {
                danmakuPlayer.pause()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "pauseDanmaKu error: $e")
            }
        }
    }

    /**
     * 继续弹幕
     */
    override fun resumeDanmaku() {
        if (createdAndLoaded) {
            try {
                danmakuPlayer.start()
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "resumeDanmaku error: $e")
            }
        }
    }

    /**
     * 发送弹幕
     */
    override fun sendDanmaku(isLive: Boolean, text: String, time: Long?, danmakuType: Int?,
                             padding: Int?, textSize: Float?, textColor: Int?,
                             textShadowColor: Int?, underlineColor: Int?, borderColor: Int?) {
        val danmaku = DanmakuItemData(
            Random.nextLong(),
            time ?: (danmakuPlayer.getCurrentTimeMs() + 500),
            //      "这是我自己发送的内容(*^▽^*)😄",
            // "[2,1,\"1-1\",4.5,\"拒绝跟你们合影\",7,6,8,5,500,0,true,\"黑体\",1]",
            text,
            DanmakuItemData.DANMAKU_MODE_ROLLING,
            textSize?.toInt() ?: 25,
            textColor ?: Color.WHITE,
            9,
            DanmakuItemData.DANMAKU_STYLE_ICON_UP,
            9
        )
        val item = danmakuPlayer.obtainItem(danmaku)
        val sequenceAction = Actions.sequence(
            Actions.rotateBy(360f, 1000L),
            Actions.scaleTo(1.5f, 1.5f, 500L),
            Actions.scaleTo(0.8f, 0.8f, 300L)
        )
        item.addAction(
            Actions.moveBy(0f, 300f, 1735L),
            sequenceAction,
            Actions.sequence(Actions.fadeOut(500L), Actions.fadeIn(300L))
        )
        danmakuPlayer.send(item)
    }

    /**
     * 获取当前弹幕时间
     */
    override fun danmakuCurrentTime() : Long {
        return danmakuPlayer.getCurrentTimeMs()
    }

    /**
     * 弹幕跳转（毫秒）
     */
    override fun danmaKuSeekTo(position: Long) {
        if (createdAndLoaded) {
            try {
                danmakuPlayer.seekTo(position)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "danmaKuSeekTo error: $e")
            }
        }
    }

    /**
     * 显示或隐藏
     */
    override fun setDanmaKuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                config = config.copy(visibility = visible)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDanmaKuVisibility error: $e")
            }
        }
    }

    /***
     * 设置弹幕透明的
     */
    override fun setDanmakuAlphaRatio(danmakuAlphaRatio: Float) {
        if (createdAndLoaded) {
            try {
                config = config.copy(alpha = danmakuAlphaRatio)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDanmakuAlphaRatio error: $e")
            }
        }
    }

    /**
     * 设置显示区域
     */
    override fun setDanmakuDisplayArea(area: Float) {
        if (createdAndLoaded) {
            try {
                config = config.copy(
                    screenPart = area
                )
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "设置显示区域 error: $e")
            }
        }
    }

    /**
     * 设置弹幕文字大小（百分比）
     */
    override fun setDanmakuFontSize(fontSizeRatio: Float) {
        if (createdAndLoaded) {
            try {
                config = config.copy(textSizeScale = fontSizeRatio)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDanmakuScaleTextSize error: $e")
            }
        }
    }

    /**
     * 设置弹幕滚动速度
     */
    override fun setDanmakuSpeed(danmakuSpeed: Float) {
        if (createdAndLoaded) {
            try {
                danmakuPlayer.updatePlaySpeed(danmakuSpeed)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDanmakuSpeed error: $e")
            }
        }
    }

    /**
     * 设置弹幕描边
     */
    override fun setDanmakuStroke(stroke: Float) {
        if (createdAndLoaded) {
            try {
                config = config.copy(bold = stroke > 1)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDanmakuStroke error: $e")
            }
        }
    }

    /**
     * 设置是否启用合并重复弹幕
     */
    override fun setDuplicateMergingEnabled(merge: Boolean) {
        if (createdAndLoaded) {
            try {
                switchTypeFilter(merge, DanmakuItemData.MERGED_TYPE_MERGED)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setDuplicateMergingEnabled error: $e")
            }
        }
    }


    /**
     * 设置是否显示顶部固定弹幕
     */
    override fun setFixedTopDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                switchTypeFilter(visible, DanmakuItemData.DANMAKU_MODE_CENTER_TOP)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setFTDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示底部固定弹幕
     */
    override fun setFixedBottomDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                switchTypeFilter(visible, DanmakuItemData.DANMAKU_MODE_CENTER_BOTTOM)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setFBDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示从左向右滚动弹幕
     */
    override fun setL2RDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                switchTypeFilter(visible, DanmakuItemData.DANMAKU_MODE_ROLLING)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setL2RDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示从右向左滚动弹幕
     */
    override fun setR2LDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                switchTypeFilter(visible, DanmakuItemData.DANMAKU_MODE_ROLLING)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setR2LDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否显示特殊弹幕
     */
    override fun setSpecialDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {

            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setSpecialDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 是否显示彩色弹幕
     */
    override fun setColorsDanmakuVisibility(visible: Boolean) {
        if (createdAndLoaded) {
            try {
                colorFilter.filterColor.clear()
                if (!visible) {
                    colorFilter.filterColor.add(0xFFFFFF)
                }
                config.updateFilter()
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置是否允许重叠
     */
    override fun setAllowOverlap(flag: Boolean) {
        if (createdAndLoaded) {
            try {
                config = config.copy(allowOverlap = flag)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }

    /**
     * 设置最大显示行数
     */
    override fun setMaximumLines(lines: Int?) {
        if (createdAndLoaded) {
            try {

            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setMaximumLines error: $e")
            }
        }
    }

    /**
     * 设置同屏弹幕密度
     *
     * @param maxSize
     */
    override fun setMaximumVisibleSizeInScreen(maxSize: Int) {
        if (createdAndLoaded) {
            try {
                config = config.copy(density = maxSize)
                danmakuPlayer.updateConfig(config)
            } catch (e: Exception) {
                Log.e(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "setColorsDanmakuVisibility error: $e")
            }
        }
    }


    /**
     * 设置过滤类型
     */
    private fun switchTypeFilter(show: Boolean, type: Int) {
        (dataFilters[DanmakuFilters.FILTER_TYPE_TYPE] as? TypeFilter)?.let { filter ->
            if (show) filter.removeFilterItem(type)
            else filter.addFilterItem(type)
            config.updateFilter()
            Log.w(
                FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG,
                "[Controller] updateFilter visibility: ${config.visibility}"
            )
            danmakuPlayer.updateConfig(config)
        }
    }



}