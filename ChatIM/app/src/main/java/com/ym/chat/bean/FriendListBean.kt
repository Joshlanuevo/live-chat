package com.ym.chat.bean

import com.chad.library.adapter.base.entity.node.BaseNode
import com.ym.chat.rxhttp.ApiUrl
import com.ym.chat.utils.PatternUtils
import com.ym.chat.utils.PinyinUtils
import com.ym.chat.utils.TimeUtils
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.Unique
import java.io.Serializable

/**
 * @version V1.0
 * @createAuthor sai
 * @createDate  2021/11/24 15:24
 * @updateAuthor
 * @updateDate
 * @company CG
 * @description 好友列表
 * @copyright copyright(c)2021 CG Technology Co., Ltd. Inc. All rights reserved.
 */
@Entity
data class FriendListBean(
    @Id
    var dbId: Long = 0,
    var info: String = "",
    var code: String = "",
    var address: String = "",
    var gender: String = "",
    var mobile: String = "",
    var name: String = "",
    var sign: String = "",
    var email: String? = "",
    var status: String? = "",
    var username: String = "",
    var remark: String = "",
    var messageNotice: String = "Y",//是否通知消息:是 Y,否 N
    var createTime: String = "",
    var updateTime: String = "",
    var displayHead: String = "",//是 Y,否 N
    var headText: String = "",//层级头像标识文字
    var levelHeadUrl: String = "",//层级头像标识图片
    @Unique
    var friendMemberId: String = "",
    var allowSpeak: String = "",//是否禁言 N禁言 Y不禁言
    var black: String = "",//是否拉黑 N不拉黑 Y拉黑
    var groupId: String = "",
    var memberId: String = "",
    var memberLevelCode: String = "",//System
    var role: String = ""//这个字段是查看群成员列表 查看资料 上 使用
) : Serializable, BaseNode() {

    var headUrl: String = ""
        get() {
            try {
                val host = ApiUrl.baseApiUrl
                val lastIndex1 = field.lastIndexOf("/")
                val lastIndex2 = field.lastIndexOf("/", lastIndex1 - 1)
                val fileName = field.substring(lastIndex2)
                return host + ApiUrl.suffix + fileName
            } catch (e: Exception) {
                e.printStackTrace()
                return field
            }
        }
    var id: String = ""
        get() = friendMemberId

    override val childNode: MutableList<BaseNode>?
        get() = null

    @Transient
    var isSelect = false //是否选择了好友

    @Transient
    var isShowCheck = true //是否显示右边筛选按钮

    @Transient
    var firstChar: String = ""
        get() {
            val char = PinyinUtils.getPinyinFirstLetter(name)?.uppercase() ?: "#"
            return if (PatternUtils.isEnglish(char)) char else "#"
        }

    @Transient
    var fullChar: String = ""
        get() {
            return PinyinUtils.ccs2Pinyin(name)?.uppercase() ?: "#"
        }

    @Transient
    var showLine = true

    @Transient
    var searchStr = ""  //搜索的字符串

    var searchToSendMemberId: String = "" //搜索好友 查看消息内容 发送人id
    var searchContent: String = "" //搜索好友查看的内容
    var searchContentId: String = "" //搜索好友查看的内容的id
    var isSearch = false //是否显示搜索好友查看的内容

    @Transient
    var nickname: String = ""
        get() {
            return if (remark != null && remark.isNotEmpty()) {
                remark
            } else {
                if (name != null && name.isNotEmpty()) {
                    name
                } else {
                    username
                }
            }
        }

    @Transient
    var createTimestamp: Long = 0
        get() {
            return TimeUtils.getCollectDateTimestamp(createTime)
        }
}