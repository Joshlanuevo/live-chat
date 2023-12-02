/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ym.base.rxhttp.utils

import android.text.TextUtils
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * ================================================
 * 处理字符串的工具类
 *
 *
 * Created by JessYan on 2016/3/16
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object CharacterHandler {
    private const val KEY_LOG_UTILS = "logUtilsGson"

    /**
     * json 格式化
     */
    fun jsonFormat(jsonStr : String?) : String? {
        return jsonStr?.trim()?.let {
            try {
                when {
                    it.startsWith("{") -> {
                        toJson(it)
                        //JSONObject jsonObject = new JSONObject(json);
                        //message = jsonObject.toString(4);
                    }
                    it.startsWith("[") -> {
                        toJson(it)
                        //JSONArray jsonArray = new JSONArray(json);
                        //message = jsonArray.toString(4);
                    }
                    else -> {
                        "Error json content\n$it"
                    }
                }
            } catch (e : JsonSyntaxException) {
                //} catch (JSONException e) {
                "Error json content\n$it"
            }
        } ?: "Empty/Null json content"
    }

    fun toJson(json : String) : String? {
        var gson4LogUtils = GsonUtils.getGson(KEY_LOG_UTILS)
        if (gson4LogUtils == null) {
            gson4LogUtils = GsonBuilder().setPrettyPrinting().serializeNulls().create()
            GsonUtils.setGson(KEY_LOG_UTILS,gson4LogUtils)
        }
        return gson4LogUtils.toJson(JsonParser.parseString(json))
    }

    /**
     * xml 格式化
     */
    fun xmlFormat(xml : String?) : String? {
        if (TextUtils.isEmpty(xml)) {
            return "Empty/Null xml content"
        }
        val message : String?
        message = try {
            val xmlInput : Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT,"yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2")
            transformer.transform(xmlInput,xmlOutput)
            xmlOutput.writer.toString().replaceFirst(">".toRegex(),">\n")
        } catch (e : TransformerException) {
            xml
        }
        return message
    }

}