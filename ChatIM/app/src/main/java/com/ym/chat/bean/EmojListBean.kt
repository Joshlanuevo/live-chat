package com.ym.chat.bean

data class EmojListBean(
    val records: MutableList<EmojBean>
) {
    data class EmojBean(
        val updateTimeEnd: String = "",
        val createTime: String = "",
        val createTimeEnd: String = "",
        val updateTimeBegin: String = "",
        val updateTime: String = "",
        val createTimeBegin: String = "",
        val width: Int = 0,
        val height: Int = 0,
        val id: String = "",
        val title: String = "",
        val url: String = "",
        //是否编辑模式
        var isEditInfo: Boolean = false,
        var isDel: Boolean = false,//是否是显示删除
        var isAddDefault: Boolean = false,
        //是否选中
        var isSelect: Boolean = false
    )
}