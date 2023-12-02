package com.ym.chat.utils

import android.media.MediaPlayer
import com.blankj.utilcode.util.Utils
import com.blankj.utilcode.util.VibrateUtils
import com.ym.chat.R
import java.io.IOException

object Mp3Player {
    private var player: MediaPlayer? = null

    /**
     * 播放消息声音
     */
    @Synchronized
    fun playMusic() {
        //震动
        if (AppManager.isMsgShock) {
            VibrateUtils.vibrate(100)
        }
        if (AppManager.isMsgRinging) {
            try {
                if (player == null || player?.isPlaying == false) {
                    player?.release()
                    player = MediaPlayer.create(Utils.getApp(), R.raw.mixin)
                    player?.start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}