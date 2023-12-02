package com.ym.chat.bean

import com.chad.library.adapter.base.entity.node.BaseNode
import com.ym.chat.db.ChatDao
import com.ym.chat.utils.PatternUtils
import com.ym.chat.utils.PinyinUtils
import com.ym.chat.utils.TimeUtils
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.io.Serializable

/**
 * 群信息
 */
@Entity
data class GroupInfoBean(
    @Id
    var dbId: Long = 0,
    var allowSpeak: String = "",//Y：关闭禁言，N:打开禁言
    val autoSign: String = "",
    val code: String = "",
    val description: String = "",
    val destroyAfterRead: String = "",
    val destroyMessage: String = "",
    val groupFileSize: String = "",
    val createTime: String = "",
    val updateTime: String = "",
    var headUrl: String = "",
    @Index
    var id: String = "",
    val leaveNoticeAdmin: String = "",
    val lookGroupInfo: String = "",
    val lookMember: String = "",
    val memberDisplay: String = "",
    val messageStoreDays: String = "",
    val modifyGroupNickname: String = "",
    var name: String = "",
    val newOwnerId: String = "",
    var notice: String = "",
    val ownerId: String = "",
    val publicSignInfo: String = "",
    val screenshot: String = "",
    val sendBlackboardNews: String = "",
    val sendVisitingCard: String = "",
    val signActivity: String = "",
    val signBeginDate: String = "",
    val signWords: String = "",
    val speechFrequency: String = "",
    val status: String = "",
    var roleType: String = "",//Owner 群主，admin 管理员，Normal 普通群成员
    var messageNotice: String = "",
    var memberAllowSpeak: String = "",
) : BaseNode(), Serializable {

    override val childNode: MutableList<BaseNode>?
        get() = null

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
    var searchStr = ""  //搜索的字符串

    @Transient
    var searchContent: String = "" //搜索群消息 内容

    @Transient
    var searchContentId: String = "" //搜索群消息 内容的id

    @Transient
    var isSearch = false //是否显示搜索好友查看的内容
    var role: Int = 0 //后面赋值


    @Transient
    var createTimestamp: Long = 0
        get() {
            return TimeUtils.getCollectDateTimestamp(createTime)
        }
}

/**
 * 群成员信息
 */
@Entity
data class GroupMemberBean(
    @Id
    var dbId: Long = 0,
    var headUrl: String = "",
    var allowSpeak: String = "",
    var groupId: String = "",
    var memberId: String = "",
    var name: String = "",
    var username: String = "",
    var operatorId: String = "",
    //Owner 群主，admin 管理员，Normal 普通群成员
    var role: String = "",
    var address: String = "",
    var gender: String = "",
    var black: String = "",
    var code: String = "",
    var mobile: String = "",
    var updateTime: String = "",
    var type: String = "",
    var createTime: String = "",
    var messageNotice: String = "",
    var email: String = "",
    var displayHead: String = "",//是 Y,否 N
    var headText: String = "",//层级头像标识文字
    var levelHeadUrl: String = ""//层级头像标识图片
) : Serializable {
    @Index
    var id: String = ""
        get() = memberId

    var nickname: String = ""
        get() {
            var nickName = ChatDao.getFriendDb().getFriendIdByRemark(memberId)
            return if (nickName != null && nickName.isNotEmpty()) {
                nickName
            } else {
                if (name != null && name.isNotEmpty()) {
                    name
                } else {
                    username
                }
            }
        }
    var nickRemark: String = ""
        get() {
            var remarkStr = ChatDao.getFriendDb().getFriendIdByRemark(memberId)
            return if (remarkStr != null && remarkStr.isNotEmpty()) {
                remarkStr
            } else {
                ""
            }
        }

    var atStr = ""//@消息搜索消息
}


/**
 * 好友
 */
data class MemberBean(
    val memberId: String
) : Serializable


/**
 * 批量同意group成员申请
 */
data class BatchModify(
    val groupId: String,
    val id: String,
    val status: String
) : Serializable


data class NewGroupInfoBean(
    var allowSpeak: String = "",//Y：关闭禁言，N:打开禁言
    val autoSign: String = "",
    val code: String = "",
    val description: String = "",
    val destroyAfterRead: String = "",
    val destroyMessage: String = "",
    val groupFileSize: String = "",
    val createTime: String = "",
    val updateTime: String = "",
    var headUrl: String = "",
    var id: String = "",
    val leaveNoticeAdmin: String = "",
    val lookGroupInfo: String = "",
    val lookMember: String = "",
    val memberDisplay: String = "",
    val messageStoreDays: String = "",
    val modifyGroupNickname: String = "",
    var name: String = "",
    val newOwnerId: String = "",
    var notice: String = "",
    val ownerId: String = "",
    val publicSignInfo: String = "",
    val screenshot: String = "",
    val sendBlackboardNews: String = "",
    val sendVisitingCard: String = "",
    val signActivity: String = "",
    val signBeginDate: String = "",
    val signWords: String = "",
    val speechFrequency: String = "",
    val status: String = "",
    var roleType: String = "",
    var messageNotice: String = "",
    var groupMemberVO: GroupMemberBean? = null
)