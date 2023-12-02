/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.ym.chat.utils.audio;

import android.net.Uri;

public interface IAudioPlayListener {
    void onStart(Uri var1);

    void onStop(Uri var1);

    void onComplete(Uri var1);
}