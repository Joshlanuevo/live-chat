package com.ym.chat.ext

import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import coil.clear
import coil.load
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.ym.base.util.save.LoginData
import com.ym.chat.R
import com.ym.chat.bean.FriendInfoBean
import com.ym.chat.bean.FriendListBean
import com.ym.chat.bean.GroupMemberBean
import com.ym.chat.db.ChatDao
import com.ym.chat.utils.AvatarUtil


/**
 * 加载圆形图片
 */
inline fun ImageView.roundLoad(url: String?) {
    if (url?.isNotBlank() == true) {
        this.load(url) {
            crossfade(true)//淡出显示
            crossfade(200)//淡出显示动画时间
            placeholder(R.drawable.ic_mine_header)//占位图
            error(R.drawable.ic_mine_header)//加载错误显示的图片
            transformations(CircleCropTransformation())//显示圆形图片
        }
    } else {
        this.load(R.drawable.ic_mine_header)
    }
}

/**
 * 加载圆形图片
 */
inline fun ImageView.roundLoad(urlId: Int) {
    this.load(urlId) {
        crossfade(true)//淡出显示
        crossfade(200)//淡出显示动画时间
        transformations(CircleCropTransformation())//显示圆形图片
    }
}

inline fun ImageView.roundLoad(url: String?, drawableResId: Int) {
    if (url?.isNotBlank() == true) {
        if (url.lowercase().contains(".gif".lowercase())) {
//            Glide.with(context).asGif().load(url).into(this)
            Glide.with(context).asGif().load(url).placeholder(drawableResId).error(drawableResId)
                .into(this)
        } else {
            this.load(url) {
                placeholder(drawableResId)//占位图
                error(drawableResId)//加载错误显示的图片
//            transformations(CircleCropTransformation())//显示圆形图片
            }
        }
    } else {
        this.load(drawableResId)
    }
}


fun ImageView.loadImg(user: FriendListBean?) {
    if (user?.memberLevelCode == "System") {
        loadImg(
            user?.headUrl,
            user?.remark,
            user?.name,
            user?.username,
            isShowGroupIcon = true,
            drawableResId = R.drawable.ic_system_head
        )
    } else {
        loadImg(user?.headUrl, user?.remark, user?.name, user?.username)
    }
}

fun ImageView.loadImg(user: FriendInfoBean?) {
    loadImg(user?.headUrl, user?.remark, user?.name, user?.username)
}

fun ImageView.loadImg(user: LoginData?) {
    if (user?.memberLevelCode == "System") {
        loadImg(
            user?.headUrl,
            user?.remark,
            user?.name,
            user?.showUserName(),
            isShowGroupIcon = true,
            drawableResId = R.drawable.ic_system_head
        )
    } else {
        loadImg(user?.headUrl, user?.remark, user?.name, user?.showUserName())
    }
}

fun ImageView.loadImg(groupMember: GroupMemberBean?) {
    loadImg(groupMember?.headUrl, groupMember?.nickRemark, groupMember?.name, groupMember?.username)
}

fun ImageView.loadImg(headImgStr: String?, user: LoginData?) {
    loadImg(headImgStr, user?.remark, user?.name, user?.showUserName())
}

fun ImageView.loadImg(
    headImgStr: String?,
    name: String?,
    drawableResId: Int = R.drawable.ic_mine_header_group,
    isShowGroupIcon: Boolean = false
) {
    loadImg(
        headImgStr,
        "",
        name,
        "",
        drawableResId = drawableResId,
        isShowGroupIcon = isShowGroupIcon
    )
}

fun ImageView.loadImg(headImgStr: String?, name: String?, username: String?) {
    loadImg(headImgStr, "", name, username)
}

/**
 * 图片显示封装
 * 如果没有图片地址显示昵称或者用户名的第一个字母
 * @headImgStr 头像Url地址
 * @remark  好友备注
 * @nickname  好友名字
 * @username  好友友聊号
 * @roundAngel  显示圆角的角度
 * @useCache  是否缓存
 */
fun ImageView.loadImg(
    headImgStr: String?,
    remark: String?,
    nickname: String?,
    username: String?,
    roundAngel: Int = 360,//显示头像圆角
    useCache: Boolean = true, //是否缓存
    isShowGroupIcon: Boolean = false, //是否显示群头像
    drawableResId: Int = R.drawable.ic_mine_header //默认头像图片
) {
    this.clear()
    var name = remark
    if (name.isNullOrBlank()) {
        name = nickname
        if (name.isNullOrBlank()) {
            name = username
        }
    }
    var nameFirst = if (name.isNullOrBlank()) {
        ""
    } else {
        name.substring(0, 1).uppercase()
    }
    var bitmapList = mutableListOf<Any>()
    bitmapList.add(nameFirst)

    // 可添加需展示的文字
    var avatar = name?.let {
        AvatarUtil.getBuilder(context)!!
            .setList(bitmapList)
            .setRoundAngel(roundAngel)
            .setBackgroundColor(it)
            .create()
            .toDrawable(context.resources)
    }

    if (headImgStr?.isNotBlank() == true) {
        if (headImgStr.lowercase().contains(".gif".lowercase())) {
            Glide.with(context).asGif().load(headImgStr).placeholder(drawableResId).into(this)
        } else {
            val builder: ImageRequest.Builder.() -> Unit = {
                if (useCache) diskCachePolicy(CachePolicy.ENABLED)
                if (useCache) memoryCachePolicy(CachePolicy.ENABLED)
                if (useCache) networkCachePolicy(CachePolicy.ENABLED)
                placeholder(drawableResId)//占位图
                error(avatar)//链接错误时 显示
//            transformations(CircleCropTransformation())//显示圆形图片
            }
            this.load(headImgStr, builder = builder)
        }
    } else {
        if (isShowGroupIcon) {
            this.load(drawableResId)
        } else {
            this.setImageDrawable(avatar)
        }
    }
}
