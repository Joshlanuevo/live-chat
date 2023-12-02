package com.ym.chat.bean

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import java.io.Serializable

data class CollectBean(
    var current: Int = 0,
    var pages: Int = 0,
    var size: Int = 0,
    var total: Int = 0,
    var searchCount: Boolean = false,
    var records: MutableList<RecordBean>? = null,
) : Serializable

@Entity
data class RecordBean(
    @Id
    var dbId: Long = 0,
    var content: String = "",
    @Unique
    var createTime: String = "",
    var createTimeBegin: String = "",
    var createTimeEnd: String = "",
    var favoriteTypeId: String = "",
    var id: String = "",
    var memberId: String = "",
    var type: String = "",
    var updateTimeBegin: String = "",
    var updateTimeEnd: String = "",
    var sendState:Int = 999
) : Serializable {
    var isUpload: Boolean = false
    var isPlaying: Boolean = false
    var audioDownLoadProgress = 0
    var fileUploadProgress = 0
    var localPath: String? = null
    var servicePath: String? = null
    var isGif = false //是否是发送表情gif图
    var downloadProcess: Float = 0f//下载进度
}

/**
 * 显示的日期
 */
data class RecordDateBean(
    var createTime: String = ""
) : Serializable