package com.ym.chat.utils

/**
 * 连接状态，CONNECTING：连接中，SUCCESS：连接成功，CONNECT_FAIL连接失败,CLOSE连接已断开,RECONNECT重连中
 */
enum class ImConnectSatus {
    CONNECTING, SUCCESS, CONNECT_FAIL, CLOSE, RECONNECT,NET_ERROR
}