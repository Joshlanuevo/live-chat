package com.ym.chat.bean

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/30 20:03
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
data class UpdateResultBean(
    val code: String,
    val msg: String,
    //节点数据
    val data: UpdateVersionBean
)