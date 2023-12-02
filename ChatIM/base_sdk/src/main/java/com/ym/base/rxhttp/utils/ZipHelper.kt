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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import java.util.zip.*

/**
 * ================================================
 * 处理压缩和解压的工具类
 *
 *
 * Created by JessYan on 10/05/2016
 * [Contact me](mailto:jess.yan.effort@gmail.com)
 * [Follow me](https://github.com/JessYanCoding)
 * ================================================
 */
object ZipHelper {
    /**
     * zlib decompress 2 String
     */
    fun decompressToStringForZlib(
      bytesToDecompress : ByteArray,
      charsetName : Charset = Charsets.UTF_8
    ) : String? {
        val bytesDecompressed = decompressForZlib(bytesToDecompress)
        var returnValue : String? = null
        try {
            returnValue = String(bytesDecompressed!!,0,bytesDecompressed.size,charsetName)
        } catch (uee : UnsupportedEncodingException) {
            uee.printStackTrace()
        }
        return returnValue
    }

    /**
     * zlib decompress 2 byte
     */
    fun decompressForZlib(bytesToDecompress : ByteArray) : ByteArray? {
        var returnValues : ByteArray? = null
        val inflater = Inflater()
        val numberOfBytesToDecompress = bytesToDecompress.size
        inflater.setInput(bytesToDecompress,0,numberOfBytesToDecompress)
        var numberOfBytesDecompressedSoFar = 0
        val bytesDecompressedSoFar : MutableList<Byte> = ArrayList()
        try {
            while (!inflater.needsInput()) {
                val bytesDecompressedBuffer = ByteArray(numberOfBytesToDecompress)
                val numberOfBytesDecompressedThisTime = inflater.inflate(bytesDecompressedBuffer)
                numberOfBytesDecompressedSoFar += numberOfBytesDecompressedThisTime
                for (b in 0 until numberOfBytesDecompressedThisTime) {
                    bytesDecompressedSoFar.add(bytesDecompressedBuffer[b])
                }
            }
            returnValues = ByteArray(bytesDecompressedSoFar.size)
            for (b in returnValues.indices) {
                returnValues[b] = bytesDecompressedSoFar[b]
            }
        } catch (dfe : DataFormatException) {
            dfe.printStackTrace()
        }
        inflater.end()
        return returnValues
    }

    /**
     * zlib compress 2 byte
     */
    fun compressForZlib(bytesToCompress : ByteArray?) : ByteArray {
        val deflater = Deflater()
        deflater.setInput(bytesToCompress)
        deflater.finish()
        val bytesCompressed = ByteArray(Short.MAX_VALUE.toInt())
        val numberOfBytesAfterCompression = deflater.deflate(bytesCompressed)
        val returnValues = ByteArray(numberOfBytesAfterCompression)
        System.arraycopy(bytesCompressed,0,returnValues,0,numberOfBytesAfterCompression)
        return returnValues
    }

    /**
     * zlib compress 2 byte
     */
    fun compressForZlib(stringToCompress : String) : ByteArray? {
        var returnValues : ByteArray? = null
        try {
            returnValues = compressForZlib(stringToCompress.toByteArray(Charsets.UTF_8))
        } catch (uee : UnsupportedEncodingException) {
            uee.printStackTrace()
        }
        return returnValues
    }

    /**
     * gzip compress 2 byte
     *
     * @throws Exception
     */
    fun compressForGzip(string : String) : ByteArray? {
        var os : ByteArrayOutputStream? = null
        var gos : GZIPOutputStream? = null
        try {
            os = ByteArrayOutputStream(string.length)
            gos = GZIPOutputStream(os)
            gos.write(string.toByteArray(Charsets.UTF_8))
            return os.toByteArray()
        } catch (e : Exception) {
            e.printStackTrace()
        } finally {
            closeQuietly(gos)
            closeQuietly(os)
        }
        return null
    }

    /**
     * gzip decompress 2 string
     *
     * @throws Exception
     */
    fun decompressForGzip(compressed : ByteArray,charsetName : Charset = Charsets.UTF_8) : String? {
        val BUFFER_SIZE = compressed.size
        var gis : GZIPInputStream? = null
        var `is` : ByteArrayInputStream? = null
        try {
            `is` = ByteArrayInputStream(compressed)
            gis = GZIPInputStream(`is`,BUFFER_SIZE)
            val string = StringBuilder()
            val data = ByteArray(BUFFER_SIZE)
            var bytesRead : Int
            while (gis.read(data).also { bytesRead = it } != -1) {
                string.append(String(data,0,bytesRead,charsetName))
            }
            return string.toString()
        } catch (e : Exception) {
            e.printStackTrace()
        } finally {
            closeQuietly(gis)
            closeQuietly(`is`)
        }
        return null
    }

    fun closeQuietly(closeable : Closeable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown : RuntimeException) {
                throw rethrown
            } catch (ignored : Exception) {
            }
        }
    }
}
