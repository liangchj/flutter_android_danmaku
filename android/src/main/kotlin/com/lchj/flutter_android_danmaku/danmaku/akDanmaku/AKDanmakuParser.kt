package com.lchj.flutter_android_danmaku.danmaku.akDanmaku

import android.util.Log
import com.google.gson.Gson
import com.kuaishou.akdanmaku.data.DanmakuItemData
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import com.google.gson.reflect.TypeToken
import com.lchj.flutter_android_danmaku.FlutterDanmakuConstant

object AKDanmakuParser {
    /**
     * 哔哩哔哩下载弹幕的xml文件
    <?xml version="1.0" encoding="UTF-8"?>
    <i>
    <chatserver>chat.bilibili.com</chatserver>
    <chatid>104211449</chatid>
    <mission>0</mission>
    <maxlimit>3000</maxlimit>
    <state>0</state>
    <real_name>0</real_name>
    <source>k-v</source>
    <d p="181.37900,1,25,16777215,1665842893,0,78695de5,1163933565554070784,11">军用科技公司</d>
    <i>
     */
    fun biliXmlPullParser(danmakuUrl: String) :  List<DanmakuItemData> {
        var inStream: InputStream? = null
        if (danmakuUrl.isNotEmpty()) {
            //打开文件
            val file = File(danmakuUrl)
            if (file.exists() && file.isFile) {
                inStream = FileInputStream(file)
            }
        }
        if (inStream == null) {
            return listOf()
        }
        var dataList = ArrayList<DanmakuItemData>();
        var item: DanmakuItemData? = null
        val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inStream, "UTF-8")
        // 获得事件的类型
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
//                XmlPullParser.START_DOCUMENT -> dataList = ArrayList<DanmakuItemData>()
                XmlPullParser.START_TAG -> if ("d" == parser.name) {
                    // <d p="23.826000213623,1,25,16777215,1422201084,0,057075e9,757076900">我从未见过如此厚颜无耻之猴</d>
                    // 0:时间(弹幕出现时间)
                    // 1:类型(1从右至左滚动弹幕|6从左至右滚动弹幕|5顶端固定弹幕|4底端固定弹幕|7高级弹幕|8脚本弹幕|9：BAS弹幕（pool必须为2）)
                    // 2:字号(18：小|25：标准|36：大)
                    // 3:颜色(十进制RGB888值)
                    // 4:时间戳 ?
                    // 5:弹幕池id(0：普通池|1：字幕池|2：特殊池（代码/BAS弹幕）)
                    // 6:用户hash(用于屏蔽用户和查看用户发送的所有弹幕 也可反查用户id)
                    // 7:弹幕id(唯一 可用于操作参数)
                    // 8:弹幕的屏蔽等级(0-10，低于用户设定等级的弹幕将被屏蔽（新增，下方样例未包含）)
                    // 取出属性值
                    val pValue: String = parser.getAttributeValue("", "p")
                    // parse p value to danmaku
                    val values: Array<String> = pValue.split(",".toRegex()).toTypedArray()
                    if (values.isNotEmpty()) {
                        val time: Long = (parseFloat(values[0]) * 1000).toLong() // 出现时间
                        val type: Int = parseInteger(values[1]) // 弹幕类型
                        val textSize: Int = parseInteger(values[2]) // 字体大小
                        val long1: Long = -0x1000000
                        val long2: Long = -0x1
                        val color: Int = ((long1 or parseLong(values[3])) and long2).toInt() // 颜色
                        // int poolType = parseInteger(values[5]); // 弹幕池类型（忽略
                        val userId: Long = parseLong(values[6])
                        val danmakuId: Long = parseLong(values[7])
                        /*item = DanmakuItemData(
                            danmakuId = danmakuId,
                            position = time,
                            content = decodeXmlString(parser.nextText()),
                            mode = if(type > 5) 0 else (if (type == 2 || type == 3) 1 else type),
                            textSize = textSize,
                            textColor = color,
                            // danmakuStyle = 0,
                            // rank = 0,
                            userId = userId,
                        )*/
                        dataList.add(DanmakuItemData(
                            danmakuId = danmakuId,
                            position = time,
                            content = decodeXmlString(parser.nextText()),
                            mode = if(type > 5) 0 else (if (type == 2 || type == 3) 1 else type),
                            textSize = textSize,
                            textColor = color,
                            // danmakuStyle = 0,
                            // rank = 0,
                            userId = userId,
                        ))
                    }

                }
                XmlPullParser.END_TAG -> if ("d" == parser.name) {
//                    Log.d(LogTagUtils.AK_DANMAKU_LOG_TAG, "end item, $item")
                    /*if (item != null) {
                        dataList.add(item)
                    }*/
                    item = null
                }
            }
            eventType = parser.next()
        }
        return dataList
    }

    /**
     * ak官方提供的json解析方式
     */
    fun akJsonParser(danmakuUrl: String) : List<DanmakuItemData> {
        var dataList: List<DanmakuItemData> = listOf();
        var jsonString = ""
        if (danmakuUrl.isNotEmpty()) {
            //打开文件
            val file = File(danmakuUrl)
            if (file.exists() && file.isFile) {
                jsonString = FileInputStream(file).bufferedReader().use { it.readText() }
            }
        }
        val type = object : TypeToken<List<DanmakuItemData>>() {}.type
        Log.d(FlutterDanmakuConstant.AK_DANMAKU_LOG_TAG, "开始解析数据")
        dataList = Gson().fromJson<List<DanmakuItemData>>(jsonString, type)
        return dataList
    }


    private fun parseFloat(floatStr: String?): Float {
        return try {
            floatStr!!.toFloat()
        } catch (e: NumberFormatException) {
            0.0f
        }
    }

    private fun parseInteger(intStr: String): Int {
        return try {
            intStr.toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun parseLong(longStr: String): Long {
        return try {
            longStr.toLong()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun decodeXmlString(title: String): String {
        var titleStr: String = title
        if (title.contains("&amp;")) {
            titleStr = title.replace("&amp;", "&")
        }
        if (title.contains("&quot;")) {
            titleStr = title.replace("&quot;", "\"")
        }
        if (title.contains("&gt;")) {
            titleStr = title.replace("&gt;", ">")
        }
        if (title.contains("&lt;")) {
            titleStr = title.replace("&lt;", "<")
        }
        return titleStr
    }
}