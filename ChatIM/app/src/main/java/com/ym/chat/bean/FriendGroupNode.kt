package com.ym.chat.bean

import com.chad.library.adapter.base.entity.node.BaseExpandNode
import com.chad.library.adapter.base.entity.node.BaseNode

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/21 16:54
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
class FriendGroupNode : BaseExpandNode() {
    override val childNode: MutableList<BaseNode>?
        get() = childList

    var childList: MutableList<BaseNode> = mutableListOf()
    var title: String = ""
    var size: Int = 0
    var type = -1//1:我管理的群组，2：我加入的群组，3：联系人
}