package com.ym.chat.bean

/**
 * 上传文件结果
 */
data class UploadResultBean(
    val code: Int,
    val msg: String,
    val data: DataResult,
) {
    data class DataResult(
        val filePath: String,
        val thumbnail: String,
        val fileType: String,
        val fileName: String,
        val fileSuffix: String,
    )
}