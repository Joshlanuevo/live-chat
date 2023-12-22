package com.ym.chat.utils

import com.ym.chat.BuildConfig

/**
 * @version V1.0
 * @createAuthor
 * @createDate  2022/10/11 08:23
 * @updateAuthor
 * @updateDate
 * @description
 * @copyright copyright(c)2022 Technology Co., Ltd. Inc. All rights reserved.
 */
object IntExt {
    fun Int.toGroupMemberSize(): Int {
        return if (this <= 50) {
            this
        } else if (this in 51..100) {
            this + 20000
        } else {
            this + 50000
        }
    }
}