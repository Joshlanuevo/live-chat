package com.ym.base.util.component

import android.content.Context
import android.content.SharedPreferences
import android.os.Parcelable
import android.util.Base64
import com.tencent.mmkv.MMKV
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
/***
 * MMKV和SharedPreferences都进行了封装，可以选择使用
 * 改用MMKVUtils 2021年4月10日14:11:29 by case
 */
@Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils"))
object SpUtil {

    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv"))
    var mmkv: MMKV? = null

    init {
        mmkv = MMKV.defaultMMKV()
    }

    @JvmName("encode1")
    fun encode(key: String, value: Any?) {
        when (value) {
            is String -> mmkv?.encode(key, value)
            is Float -> mmkv?.encode(key, value)
            is Boolean -> mmkv?.encode(key, value)
            is Int -> mmkv?.encode(key, value)
            is Long -> mmkv?.encode(key, value)
            is Double -> mmkv?.encode(key, value)
            is ByteArray -> mmkv?.encode(key, value)
            is Nothing -> return
        }
    }

    fun <T : Parcelable> encode(key: String, t: T?) {
        if(t ==null){
            return
        }
        mmkv?.encode(key, t)
    }

    fun encode(key: String, sets: Set<String>?) {
        if(sets ==null){
            return
        }
        mmkv?.encode(key, sets)
    }

    fun decodeInt(key: String): Int? {
        return mmkv?.decodeInt(key, 0)
    }

    fun decodeDouble(key: String): Double? {
        return mmkv?.decodeDouble(key, 0.00)
    }

    fun decodeLong(key: String): Long? {
        return mmkv?.decodeLong(key, 0L)
    }

    fun decodeBoolean(key: String): Boolean {
        return mmkv?.decodeBool(key, false) == true
    }

    fun decodeFloat(key: String): Float? {
        return mmkv?.decodeFloat(key, 0F)
    }

    fun decodeByteArray(key: String): ByteArray? {
        return mmkv?.decodeBytes(key)
    }

    fun decodeString(key: String): String? {
        return mmkv?.decodeString(key, "")
    }

    fun <T : Parcelable> decodeParcelable(key: String, tClass: Class<T>): T? {
        return mmkv?.decodeParcelable(key, tClass)
    }

    fun decodeStringSet(key: String): Set<String>? {
        return mmkv?.decodeStringSet(key, Collections.emptySet())
    }

    fun removeKey(key: String) {
        mmkv?.removeValueForKey(key)
    }

    fun clearAll() {
        mmkv?.clearAll()
    }


    private const val FILE_NAME = "spUtils"

    /**
     * SP保存数据的方法，将不同类型的数据保存到文件中
     *
     * @param context
     * @param key
     * @param object
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.encode"))
    fun put(context: Context, key: String?, `object`: Any?) {
        if (`object` == null) return
        val sp =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        if (`object` is String) {
            editor.putString(key, `object` as String?)
        } else if (`object` is Int) {
            editor.putInt(key, (`object` as Int?)!!)
        } else if (`object` is Boolean) {
            editor.putBoolean(key, (`object` as Boolean?)!!)
        } else if (`object` is Float) {
            editor.putFloat(key, (`object` as Float?)!!)
        } else if (`object` is Long) {
            editor.putLong(key, (`object` as Long?)!!)
        } else {
            editor.putString(key, `object`.toString())
        }
        SharedPreferencesCompat.apply(editor)
    }

    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     */
    internal object SharedPreferencesCompat {
        private val sApplyMethod = findApplyMethod()

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        private fun findApplyMethod(): Method? {
            try {
                val clz: Class<*> = SharedPreferences.Editor::class.java
                return clz.getMethod("apply")
            } catch (e: NoSuchMethodException) {
            }
            return null
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        fun apply(editor: SharedPreferences.Editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor)
                    return
                }
            } catch (e: IllegalArgumentException) {
            } catch (e: IllegalAccessException) {
            } catch (e: InvocationTargetException) {
            }
            editor.commit()
        }
    }

    /**
     * 得到保存数据的方法，根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.decode"))
    operator fun get(context: Context, key: String?, defaultObject: Any?): Any? {
        val sp = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        if (defaultObject is String) {
            return sp.getString(key, defaultObject as String?)
        } else if (defaultObject is Int) {
            return sp.getInt(key, (defaultObject as Int?)!!)
        } else if (defaultObject is Boolean) {
            return sp.getBoolean(key, (defaultObject as Boolean?)!!)
        } else if (defaultObject is Float) {
            return sp.getFloat(key, (defaultObject as Float?)!!)
        } else if (defaultObject is Long) {
            return sp.getLong(key, (defaultObject as Long?)!!)
        }
        return null
    }

    /**
     * 移除某个key值已经对应的值
     *
     * @param context
     * @param key
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.removeValueForKey"))
    fun remove(context: Context, key: String?) {
        val sp = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.remove(key)
       SharedPreferencesCompat.apply(editor)
    }

    /**
     * 清除所有数据
     *
     * @param context
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.clearAll"))
    fun clear(context: Context) {
        val sp = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.clear()
        SharedPreferencesCompat.apply(editor)
    }

    /**
     * 查询某个key是否已经存在
     *
     * @param context
     * @param key
     * @return
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.containsKey"))
    fun contains(context: Context, key: String?): Boolean {
        val sp = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        return sp.contains(key)
    }

    /**
     * 返回所有的键值对
     *
     * @param context
     * @return
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.allKeys"))
    fun getAll(context: Context): Map<String?, *>? {
        val sp = context.getSharedPreferences(
            FILE_NAME,
            Context.MODE_PRIVATE
        )
        return sp.all
    }

    /**
     * Return the string value in sp.
     *
     * @param key          The key of sp.
     * @return the string value if sp exists or `defaultValue` otherwise
     */
    @Deprecated("Please use MMKVUtils", replaceWith = ReplaceWith("MMKVUtils.mmkv?.decodeString"))
    fun getString(context: Context, key: String): String? {
        val sp =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        return sp.getString(key, "")
    }

    /**
     * 获取保存的序列化的实体
     *
     * @param context
     * @param key
     * @return
     */
    fun getSerializableEntity(context: Context, key: String?): Any? {
        val sharePre =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        try {
            val wordBase64 = sharePre.getString(key, "")
            // 将base64格式字符串还原成byte数组
            if (wordBase64 == null || wordBase64 == "") { // 不可少，否则在下面会报java.io
                // .StreamCorruptedException
                return null
            }
            val objBytes = Base64.decode(wordBase64.toByteArray(), Base64.DEFAULT)
            val bais = ByteArrayInputStream(objBytes)
            val ois = ObjectInputStream(bais)
            // 将byte数组转换成product对象
            val obj = ois.readObject()
            bais.close()
            ois.close()
            return obj
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * 保存序列号的实体类
     *
     * @param context
     * @param key
     * @param object
     */
    fun putSerializableEntity(context: Context, key: String?, `object`: Serializable?) {
        val share =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        if (`object` == null) {
            val editor = share.edit().remove(key)
            return
        }
        val baos = ByteArrayOutputStream()
        var oos: ObjectOutputStream? = null
        var objectStr = ""
        try {
            oos = ObjectOutputStream(baos)
            oos.writeObject(`object`)
            // 将对象放到OutputStream中
            // 将对象转换成byte数组，并将其进行base64编码
            objectStr = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
            baos.close()
            oos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }
        val editor = share.edit()
        // 将编码后的字符串写到base64.xml文件中
        editor.putString(key, objectStr)
        SharedPreferencesCompat.apply(editor)
    }

}
