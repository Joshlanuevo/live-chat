package com.ym.chat.widget.ateditview;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.os.BuildCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;

import java.util.ArrayList;
import java.util.List;

public class AtUserEditText extends AppCompatEditText {

    private List<OnSelectionChangeListener> onSelectionChangeListeners;

    private OnSelectionChangeListener onSelectionChangeListener;

    public AtUserEditText(Context context) {
        super(context);
    }

    public AtUserEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AtUserEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (onSelectionChangeListener != null) {
            onSelectionChangeListener.onSelectionChange(selStart, selEnd);
        }
        if (onSelectionChangeListeners != null) {
            for (int i = 0; i < onSelectionChangeListeners.size(); i++) {
                onSelectionChangeListeners.get(i).onSelectionChange(selStart, selEnd);
            }
        }
    }

    public void addOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        if (onSelectionChangeListeners == null) {
            onSelectionChangeListeners = new ArrayList<>();
        }
        onSelectionChangeListeners.add(onSelectionChangeListener);
    }

    public void removeOnSelectionChangedListener(OnSelectionChangeListener onSelectionChangeListener) {
        if (onSelectionChangeListeners != null) {
            onSelectionChangeListeners.remove(onSelectionChangeListener);
        }
    }

    public void clearOnSelectionChangedListener() {
        if (onSelectionChangeListeners != null) {
            onSelectionChangeListeners.clear();
        }
    }

    public void setOnSelectionChangeListener(OnSelectionChangeListener onSelectionChangeListener) {
        this.onSelectionChangeListener = onSelectionChangeListener;
    }

    public interface OnSelectionChangeListener {
        void onSelectionChange(int selStart, int selEnd);
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);
        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String [] {"image/png"});

        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (BuildCompat.isAtLeastNMR1() && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();
                            }
                            catch (Exception e) {
                                return false; // return false if failed
                            }
                        }

                        // read and display inputContentInfo asynchronously.
                        // call inputContentInfo.releasePermission() as needed.

                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }
}