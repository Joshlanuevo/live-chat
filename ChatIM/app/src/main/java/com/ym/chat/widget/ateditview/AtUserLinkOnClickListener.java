package com.ym.chat.widget.ateditview;

/**
 * Created by guoshichao on 2021/6/29
 * AtUser点击回调使用
 */
public interface AtUserLinkOnClickListener {

    //超链接点击
    void ulrLinkClick(String str);

    //@用户点击
    void atUserClick(String str);

    //手机号点击
    void phoneClick(String str);

}
