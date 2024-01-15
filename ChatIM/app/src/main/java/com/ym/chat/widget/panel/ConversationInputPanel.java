/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.ym.chat.widget.panel;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.NoRecentEmoji;
import com.ym.base.ext.StringExtKt;
import com.ym.base.util.save.LoginData;
import com.ym.base.util.save.MMKVUtils;
import com.ym.chat.R;
import com.ym.chat.adapter.ChatGifAdapter;
import com.ym.chat.bean.ChatMessageBean;
import com.ym.chat.bean.DraftBean;
import com.ym.chat.bean.EmojListBean;
import com.ym.chat.db.ChatDao;
import com.ym.chat.db.ConversationDb;
import com.ym.chat.ui.MeExpressionActivity;
import com.ym.chat.utils.ChatUtils;
import com.ym.chat.utils.EmojiUtils;
import com.ym.chat.utils.MsgType;
import com.ym.chat.utils.audio.AudioRecorderPanel;
import com.ym.chat.widget.PageTransformer;
import com.ym.chat.widget.ateditview.AtUserEditText;
import com.ym.chat.widget.ateditview.AtUserHelper;

import java.util.List;

public class ConversationInputPanel extends FrameLayout implements View.OnClickListener, TextWatcher, View.OnKeyListener {
    static final String TAG = "ConversationInputPanel";
    LinearLayout inputContainerLinearLayout;
    LinearLayout mediaOption;
    LinearLayout llInputRoot;
    TextView disableInputTipTextView;

    AtUserEditText editText;
    ImageView emotionImageView;
    ImageView extImageView;
    ImageView audioImageView;
    ImageView sendButton;
    Button audioButton;
    EmojiPopup emojiPopup;

    KeyboardHeightFrameLayout emotionContainerFrameLayout;
    FrameLayout emotionLayout;
    KeyboardHeightFrameLayout extContainerFrameLayout;
    ImageView cancelReply;
    View replyRoot;
    View editRoot;
    TextView tvEditContent;
    ImageView ivEditCancel;
    private AudioRecorderPanel audioRecorderPanel;


    private InputAwareLayout rootLinearLayout;
    private Activity activity;

    private long lastTypingTime;
    private String draftString;
    private static final int TYPING_INTERVAL_IN_SECOND = 10;
    private static final int MAX_EMOJI_PER_MESSAGE = 50;
    private int messageEmojiCount = 0;
    private SharedPreferences sharedPreferences;
    private SendMsgListener sendMsgListener;
    private NoticeFriendListener notifyListener;
    private OnExtClickListener extClickListener;
    private OnDelGifClickListener delGifClickListener;
    private TextView tvMuteState;

    private OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener;
    private TextView tvReplyName;
    private TextView tvReplyContent;
    private ImageView ivReplyPreview;
    private RecyclerView listGif;
    private boolean isAutoClear = true;

    private ChatMessageBean editMessage;//编辑消息
    private ChatMessageBean replyMessage;//回复消息
    private DraftBean draftData;//草稿
    private Long sendTxtMsgTime = 0L; // 记录发送文本消息的时间戳
    private int ALLOW_TIME_LONG = 1; //限制发送时间间隔时长 单位秒

    private ChatGifAdapter mGifAdapter;
    private ConversationDb conversationDb = new ConversationDb();
    private LoginData userInfo;

    public ConversationInputPanel(@NonNull Context context) {
        super(context);
    }

    public ConversationInputPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    public ConversationInputPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConversationInputPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public void setOnConversationInputPanelStateChangeListener(OnConversationInputPanelStateChangeListener onConversationInputPanelStateChangeListener) {
        this.onConversationInputPanelStateChangeListener = onConversationInputPanelStateChangeListener;
    }

    public void bind(FragmentActivity activity, InputAwareLayout rootInputAwareLayout) {

    }

    public void disableAutoClearOnSend() {
        isAutoClear = false;
    }

    /**
     * 设置发送事件回调
     *
     * @param listener
     */
    public void setSendMsgListener(SendMsgListener listener) {
        this.sendMsgListener = listener;
    }

    /**
     * @param listener
     * @功能回调
     */
    public void setNoticeFriendListener(NoticeFriendListener listener) {
        this.notifyListener = listener;
    }

    /**
     * 设置扩展菜单点击事件
     *
     * @param listener
     */
    public void setExtClickListener(OnExtClickListener listener) {
        this.extClickListener = listener;
    }

    /**
     * 设置扩展菜单点击事件
     *
     * @param listener
     */
    public void setDelGifClickListener(OnDelGifClickListener listener) {
        this.delGifClickListener = listener;
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void disableInput(String tip) {
        closeConversationInputPanel();
        inputContainerLinearLayout.setVisibility(GONE);
        mediaOption.setVisibility(GONE);
        disableInputTipTextView.setVisibility(VISIBLE);
        disableInputTipTextView.setText(tip);
    }

    public void enableInput() {
        inputContainerLinearLayout.setVisibility(VISIBLE);
        mediaOption.setVisibility(VISIBLE);
        disableInputTipTextView.setVisibility(GONE);
    }

    public void onDestroy() {
        if (draftData == null) {
            return;
        }
        if (TextUtils.isEmpty(draftData.getContent())) {
            deleteDraft(draftData);
        }
    }

    private boolean isMute = false;

    /**
     * 群禁言
     */
    public void setMute(String text, OnClickListener listener) {
        isMute = true;
        inputContainerLinearLayout.setVisibility(View.GONE);
        mediaOption.setVisibility(View.GONE);
        tvMuteState.setVisibility(View.VISIBLE);
        if (listener != null) {
            tvMuteState.setOnClickListener(listener);
        }
        if (!TextUtils.isEmpty(text)) {
            tvMuteState.setText(text);
        }
    }

    /**
     * 关闭禁言
     */
    public void closeMute() {
        isMute = false;
        inputContainerLinearLayout.setVisibility(View.VISIBLE);
        mediaOption.setVisibility(View.VISIBLE);
        tvMuteState.setVisibility(View.GONE);
    }

    /**
     * 是否处于禁言状态
     *
     * @return
     */
    public boolean isMute() {
        return isMute;
    }

    public void init(Activity fragment, InputAwareLayout rootInputAwareLayout) {
        LayoutInflater.from(getContext()).inflate(R.layout.conversation_input_panel, this, true);

        this.activity = fragment;
        this.rootLinearLayout = rootInputAwareLayout;
        editText = findViewById(R.id.editText);

        sharedPreferences = getContext().getSharedPreferences("sticker", Context.MODE_PRIVATE);

        inputContainerLinearLayout = findViewById(R.id.inputContainerLinearLayout);
        mediaOption = findViewById(R.id.llOption);
        llInputRoot = findViewById(R.id.llInputRoot);
        emotionImageView = findViewById(R.id.emotionImageView);
        extImageView = findViewById(R.id.extImageView);
        sendButton = findViewById(R.id.sendButton);
        emotionContainerFrameLayout = findViewById(R.id.emotionContainerFrameLayout);
        emotionLayout = findViewById(R.id.emotionLayout);
        extContainerFrameLayout = findViewById(R.id.extContainerContainerLayout);
        cancelReply = findViewById(R.id.tvCancelReply);
        replyRoot = findViewById(R.id.consReply);
        tvMuteState = findViewById(R.id.tvMuteState);
        tvReplyName = findViewById(R.id.tvReplyName);
        tvReplyContent = findViewById(R.id.tvReplyContent);
        ivReplyPreview = findViewById(R.id.ivReplyPreview);
        listGif = findViewById(R.id.listGif);

        editRoot = findViewById(R.id.consEdit);
        tvEditContent = findViewById(R.id.tvEditContent);
        ivEditCancel = findViewById(R.id.ivCancelEdit);

        audioButton = findViewById(R.id.audioButton);
        audioImageView = findViewById(R.id.audioImageView);

        emotionImageView.setOnClickListener(this);
        audioImageView.setOnClickListener(this);
        extImageView.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        cancelReply.setOnClickListener(this);
        ivEditCancel.setOnClickListener(this);

        findViewById(R.id.llSelectPhoto).setOnClickListener(this);
        findViewById(R.id.llSelectVideo).setOnClickListener(this);
        findViewById(R.id.llSelect3).setOnClickListener(this);
        findViewById(R.id.ivSelectImage).setOnClickListener(this);
        findViewById(R.id.ivOpenCamera).setOnClickListener(this);
        findViewById(R.id.ivGIf).setOnClickListener(this);
        findViewById(R.id.ivSelectVideo).setOnClickListener(this);
        findViewById(R.id.ivSelectFile).setOnClickListener(this);

        editText.setOnClickListener(this);
        editText.addTextChangedListener(this);
        AtUserHelper.addSelectionChangeListener(editText);
        editText.setOnKeyListener(this);

        //Gif设置
        mGifAdapter = new ChatGifAdapter();
        listGif.setAdapter(mGifAdapter);

        emojiPopup = EmojiPopup.Builder.fromRootView(emotionLayout)
                .setOnEmojiBackspaceClickListener(ignore ->
                        Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> {
                            emotionImageView.setImageResource(R.drawable.ic_cheat_keyboard);
                            hideAudioButton();
                        }
                )
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> {
                            emotionImageView.setImageResource(R.drawable.ic_emoj_chat_new);
                        }
                )
                .setOnSoftKeyboardCloseListener(() -> Log.d(TAG, "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_fade_animation_style)
                .setPageTransformer(new PageTransformer())
                .setRecentEmoji(NoRecentEmoji.INSTANCE) // Uncomment this to hide recent emojis.
                .build(editText);

        // audio record panel
        audioRecorderPanel = new AudioRecorderPanel(getContext());
        audioRecorderPanel.setRecordListener(new AudioRecorderPanel.OnRecordListener() {
            @Override
            public void onRecordSuccess(@NonNull String audioFile, int duration) {
                if (onConversationInputPanelStateChangeListener != null)
                    onConversationInputPanelStateChangeListener.onRecordSuccess(audioFile, duration);
//                发送文件
//                File file = new File(audioFile);
//                if (file.exists()) {
//                    messageViewModel.sendAudioFile(conversation, Uri.parse(audioFile), duration);
//                }
            }

            @Override
            public void onRecordFail(String reason) {
                Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecordStateChanged(AudioRecorderPanel.RecordState state) {
                if (state == AudioRecorderPanel.RecordState.START) {
                    //正在录制音频文件
//                    TypingMessageContent content = new TypingMessageContent(TypingMessageContent.TYPING_VOICE);
//                    messageViewModel.sendMessage(conversation, content);
                }
            }
        });

        userInfo = MMKVUtils.INSTANCE.getUser();
    }

    /**
     * 显示gif表情图片
     */
    public void setGifData(List<EmojListBean.EmojBean> data) {
        listGif.setVisibility(VISIBLE);
        mGifAdapter.setList(data);
        mGifAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                EmojListBean.EmojBean item = mGifAdapter.getData().get(position);
                if (item.isAddDefault()) {
                    //进入管理emoj图片
                    getContext().startActivity(new Intent(getContext(), MeExpressionActivity.class));
                } else {
                    //发送
                    int w = item.getWidth() > 0 ? item.getWidth() : 400;
                    int h = item.getHeight() > 0 ? item.getHeight() : 400;

                    if (isAllowSendMsg()){
                        sendMsgListener.sendKeyboardImage(item.getUrl(), w, h, true);
                    }
                }
            }
        });
        mGifAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (mGifAdapter.getData() != null && mGifAdapter.getData().size() > position)
                    delGifClickListener.onDelGifClick(mGifAdapter.getData().get(position).getId());
            }
        });
    }

    void onClearRefImageButtonClick() {
        updateConversationDraft();
    }

    public void onKeyboardShown() {
        hideEmotionLayout();
    }

    public void onKeyboardHidden() {
        // do nothing
    }


    public void setInputText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        editText.setText(text);
        editText.setSelection(text.length());
        editText.requestFocus();
        rootLinearLayout.showSoftkey(editText);
    }

    public void setInputText(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        String txt = editText.getText() + text;
        editText.setText(txt);
        editText.setSelection(txt.length());
        editText.requestFocus();
        rootLinearLayout.showSoftkey(editText);
    }

    public void appendAtText(String text, String uid) {
        AtUserHelper.appendChooseUser(editText, text, uid,
                this, getResources().getColor(R.color.color_at));
    }

    public void longClickappendAtText(String text, String uid) {
        AtUserHelper.longClickAppendChooseUser(editText, text, uid,
                this, getResources().getColor(R.color.color_at));
    }

    public void onActivityPause() {
        updateConversationDraft();
    }

    private void updateConversationDraft() {
        if (draftData == null) {
            return;
        }
        Editable editable = Editable.Factory.getInstance().newEditable(editText.getText());
        editable = AtUserHelper.toAtUser(editable);
        if (TextUtils.isEmpty(editable)) {
            draftData.setContent("");
        } else {
            draftData.setContent(editable.toString());
        }
        saveDraft(draftData);
    }

    private void showAudioButton() {
        audioRecorderPanel.attach(rootLinearLayout, audioButton);
        llInputRoot.setVisibility(View.GONE);
        audioButton.setVisibility(View.VISIBLE);
        audioImageView.setImageResource(R.drawable.ic_cheat_keyboard);
        rootLinearLayout.hideCurrentInput(editText);
        rootLinearLayout.hideAttachedInput(true);
    }


    private void hideAudioButton() {
        audioButton.setVisibility(View.GONE);
        audioRecorderPanel.deattch();
        llInputRoot.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(editText.getText().toString())) {
            sendButton.setVisibility(View.VISIBLE);
        }
//        if (TextUtils.isEmpty(editText.getText())) {
//            extImageView.setVisibility(VISIBLE);
//            sendButton.setVisibility(View.GONE);
//        } else {
//            extImageView.setVisibility(GONE);
//            sendButton.setVisibility(View.VISIBLE);
//        }
        audioImageView.setImageResource(R.drawable.ic_voice_chat);
    }

    private void showEmotionLayout() {
        emotionImageView.setImageResource(R.drawable.ic_cheat_keyboard);
        rootLinearLayout.show(editText, emotionContainerFrameLayout);
        emojiPopup.show();
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideEmotionLayout() {
        emojiPopup.dismiss();
        emotionImageView.setImageResource(R.drawable.ic_emoj_chat_new);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    /**
     * 显示扩d展菜单
     */
    private void showConversationExtension() {
        rootLinearLayout.show(editText, extContainerFrameLayout);
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelExpanded();
        }
    }

    private void hideConversationExtension() {
        if (onConversationInputPanelStateChangeListener != null) {
            onConversationInputPanelStateChangeListener.onInputPanelCollapsed();
        }
    }

    /**
     * 关闭表情面板
     */
    public void closeConversationInputPanel() {
        emotionImageView.setImageResource(R.drawable.ic_emoj_chat_new);
        rootLinearLayout.hideAttachedInput(true);
        rootLinearLayout.hideCurrentInput(editText);
    }

    private void notifyTyping(int type) {
//        if (conversation.type == Conversation.ConversationType.Single) {
//            long now = System.currentTimeMillis();
//            if (now - lastTypingTime > TYPING_INTERVAL_IN_SECOND * 1000) {
//                lastTypingTime = now;
//                TypingMessageContent content = new TypingMessageContent(type);
//                messageViewModel.sendMessage(conversation, content);
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.emotionImageView:
                if (audioRecorderPanel.isShowingRecorder()) {
                    return;
                }

                if (rootLinearLayout.getCurrentInput() == emotionContainerFrameLayout) {
                    hideEmotionLayout();
//                    rootLinearLayout.showSoftkey(editText);
                    emojiPopup.toggle();
                } else {
                    hideAudioButton();
                    emojiPopup.toggle();
//                    showEmotionLayout();
                }
                break;
            case R.id.editText:
                if (audioRecorderPanel.isShowingRecorder()) {
                    return;
                }
                if (emojiPopup != null && emojiPopup.isShowing()) {
                    hideEmotionLayout();
//                    emojiPopup.toggle();
                }
                break;
            case R.id.ivCancelEdit:
                cancelEdit();//取消编辑
                break;
            case R.id.extImageView:
                if (rootLinearLayout.getCurrentInput() == extContainerFrameLayout) {
                    hideConversationExtension();
                    rootLinearLayout.showSoftkey(editText);
                } else {
                    emotionImageView.setImageResource(R.drawable.ic_emoj_chat_new);
                    showConversationExtension();
                }
                break;
            case R.id.sendButton:
                //发送消息
                Editable editable = Editable.Factory.getInstance().newEditable(editText.getText());
                editable = AtUserHelper.toAtUser(editable);

                messageEmojiCount = 0;
//                Editable content = editText.getText();
                if (TextUtils.isEmpty(editable)) {
                    StringExtKt.toast("请输入发送内容");
                    return;
                }
                String s = editable.toString().trim();
                if (TextUtils.isEmpty(s)) {
                    StringExtKt.toast("请输入发送内容");
                    return;
                }
                if (isAllowSendMsg())
                    if (sendMsgListener != null) {
                        if (isAutoClear) {
                            editText.setText("");
                        }

                        if (editMessage != null) {
                            hideEdit();
                            editMessage.setContent(s);
                            sendMsgListener.editMsg(editMessage);
                            editMessage = null;
                        } else {
                            //普通消息
                            sendMsgListener.clickSendButton(s);
                        }
                        hideReplyView();
                    }
                break;
            case R.id.tvCancelReply:
//                waitReplyMsg = null;
                //取消回复内容
                replyMessage = null;
                sendMsgListener.replyMsgListener(null);
                replyRoot.setVisibility(GONE);
                break;
            case R.id.llSelectPhoto:
            case R.id.ivSelectImage:
                if (extClickListener != null) {
                    extClickListener.onExtMenuClick(0);
                }
                break;
            case R.id.llSelectVideo:
            case R.id.ivOpenCamera:
                if (extClickListener != null) {
                    extClickListener.onExtMenuClick(1);
                }
                break;
            case R.id.llSelect3:
            case R.id.ivSelectVideo:
                if (extClickListener != null) {
                    extClickListener.onExtMenuClick(2);
                }
                break;
            case R.id.ivSelectFile:
                if (extClickListener != null) {
                    extClickListener.onExtMenuClick(3);
                }
                break;
            case R.id.ivGIf: {
                if (rootLinearLayout.getCurrentInput() == extContainerFrameLayout) {
                    hideConversationExtension();
                    rootLinearLayout.showSoftkey(editText);
                } else {
                    showConversationExtension();
                }
                break;
            }
            case R.id.audioImageView: {
                //发送录音文件
                boolean has = XXPermissions.isGranted(activity, Manifest.permission.RECORD_AUDIO);
                if (!has) {
                    XXPermissions.with(activity)
                            .permission(Manifest.permission.RECORD_AUDIO)
                            .request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    startRecord();
                                }

                                @Override
                                public void onDenied(List<String> permissions, boolean never) {
                                    if (never) {
                                        XXPermissions.startPermissionActivity(activity, Manifest.permission.RECORD_AUDIO);
                                    } else {
                                        StringExtKt.toast("录音需要打开录音权限");
                                    }
                                }
                            });
                } else {
                    startRecord();
                }
                break;
            }
        }
    }

    /**
     * 隐藏回复的View
     */
    public void hideReplyView() {
        replyRoot.setVisibility(GONE);
        sendMsgListener.replyMsgListener(null);
    }


    /**
     * 频繁发送消息的限制
     * 是否允许发送消息 时间间隔限制在1秒
     */
    private Boolean isAllowSendMsg() {
        if (MMKVUtils.INSTANCE.isAdmin()) {
            //管理员没有发言频率限制
            return true;
        }
        long newTime = System.currentTimeMillis();
        if ((newTime - sendTxtMsgTime) < ALLOW_TIME_LONG * 5000) {
            StringExtKt.toast("消息发送过于频繁");
            return false;
        }
        sendTxtMsgTime = newTime;
        return true;
    }

    private void startRecord() {
        if (audioButton.isShown()) {
            hideAudioButton();
            editText.requestFocus();
            rootLinearLayout.showSoftkey(editText);
        } else {
//            editText.clearFocus();
            showAudioButton();
            hideEmotionLayout();
            rootLinearLayout.hideSoftkey(editText, null);
            hideConversationExtension();
        }
    }

    public interface SendMsgListener {
        //普通发送消息
        void clickSendButton(String str);

        //直接发送输入法的图片
        void sendKeyboardImage(String imgUrl, int with, int height, Boolean isGifE);

        //编辑消息
        void editMsg(ChatMessageBean editMsg);

        //回复消息
        void replyMsgListener(ChatMessageBean waitMsg);
//
//        //回复消息
//        void replyMsgListener(String str, ChatMessageBean waitMsg);
    }

    public interface NoticeFriendListener {
        void onNotifyListener(String str);

        //删除了@
        void onCancenAt();

        //搜索过滤数据
        void onSearchMem(String keyword);
    }

    public interface OnExtClickListener {
        void onExtMenuClick(int position);
    }

    public interface OnDelGifClickListener {
        void onDelGifClick(String id);
    }
//
//    //待回复消息
//    private ChatMessageBean waitReplyMsg;

    /**
     * 隐藏编辑状态
     */
    public void hideEdit() {
        if (editRoot != null && editRoot.getVisibility() == VISIBLE) {
            editRoot.setVisibility(GONE);
        }
    }

    /**
     * 显示编辑状态
     */
    public void showEdit() {
        if (editRoot != null) {
            editRoot.setVisibility(VISIBLE);
        }
    }

    /**
     * 开启编辑
     *
     * @param text
     * @param messageBean
     */
    public void setEditMessage(CharSequence text, ChatMessageBean messageBean) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        showEdit();
        this.editMessage = messageBean;
        tvEditContent.setText(text);
        editText.setText(text);
        editText.setSelection(text.length());
        //显示软键盘
        showInput();
    }

    public void showReply(ChatMessageBean msg) {
        //回复消息
        replyRoot.setVisibility(VISIBLE);
        if (msg != null) {
            //回调待回复内容
            replyMessage = msg;
            sendMsgListener.replyMsgListener(msg);

            //显示回复消息
            ChatUtils.INSTANCE.showRelyMsg(getContext(), msg, ivReplyPreview, tvReplyName, tvReplyContent);
        }
        //显示软键盘
        showInput();
    }

    /**
     * 取消编辑
     */
    private void cancelEdit() {
        editMessage = null;
        hideEdit();
        editText.setText("");
    }

    /**
     * 显示草稿
     */
    public void setDraft(DraftBean draftData) {
        try {
            this.draftData = draftData;
            if (draftData != null) {
                String content = draftData.getContent();
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                int type = draftData.getType();
                String otherMsg = draftData.getAtContent();
                ChatMessageBean msg = GsonUtils.fromJson(otherMsg, ChatMessageBean.class);
                if (type == 1) {
                    showReply(msg);
                } else if (type == 2) {
                    if (msg.getMsgType().equals(MsgType.MESSAGETYPE_TEXT)) {
                        setEditMessage(msg.getContent(), msg);
                    } else if (msg.getMsgType().equals(MsgType.MESSAGETYPE_AT)) {
                        setEditMessage(
                                AtUserHelper.parseAtUserLinkJxEditText(
                                        msg.getContent(),
                                        ContextCompat.getColor(getContext(), R.color.color_at)
                                ), msg
                        );
                    }
                }

                Log.e("Draft", draftData.getContent());
                CharSequence atContent = AtUserHelper.parseAtUserLinkJx(content, ContextCompat.getColor(getContext(), R.color.color_at), null);
                editText.setText(atContent);
                editText.setSelection(atContent.length());
                editText.requestFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存草稿
     */
    private void saveDraft(DraftBean draftData) {
        try {
            if (replyMessage != null) {
                draftData.setType(1);
                draftData.setAtContent(GsonUtils.toJson(replyMessage));
            } else if (editMessage != null) {
                draftData.setType(2);
                draftData.setAtContent(GsonUtils.toJson(editMessage));
            }
            ChatDao.INSTANCE.getDraftDb().addDraft(draftData);

            //更新会话显示草稿
            conversationDb.updateDraftMsgByTargtId(draftData.getChatId(), draftData.getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改草稿
     */
    private void updateDraft(DraftBean draftData) {
        try {
            ChatDao.INSTANCE.getDraftDb().updateDraft(draftData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除草稿
     */
    private void deleteDraft(DraftBean draftData) {
        try {
            this.draftData = null;
            ChatDao.INSTANCE.getDraftDb().deleteDraft(draftData);

            //更新会话显示草稿
            conversationDb.updateDraftMsgByTargtId(draftData.getChatId(), "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int beforeEditStart;
    private int beforeEditEnd;
    private SpannableStringBuilder beforeText, afterText;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        beforeText = new SpannableStringBuilder(s);
        beforeEditStart = editText.getSelectionStart();
        beforeEditEnd = editText.getSelectionEnd();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        afterText = new SpannableStringBuilder(s);
        if (s.length() > 0) {
            //显示发送按钮
            sendButton.setVisibility(VISIBLE);
            audioImageView.setVisibility(GONE);
        } else {
            //隐藏发送按钮
            sendButton.setVisibility(GONE);
            audioImageView.setVisibility(VISIBLE);
        }
    }

//    private boolean isAt = false;

    @Override
    public void afterTextChanged(Editable s) {
        //删除At整体
        AtUserHelper.isRemoveAt(editText, this, beforeText, afterText, s, beforeEditStart, beforeEditEnd);
        String content = editText.getText().toString();
        if (EmojiUtils.INSTANCE.isImageBySys(content)) {
            //有图片
            String tempStr = EmojiUtils.INSTANCE.getImageUrl(content);
            if (!TextUtils.isEmpty(tempStr) && sendMsgListener != null) {
                if (tempStr.toLowerCase().endsWith(".gif")
                        || tempStr.toLowerCase().endsWith(".png")
                        || tempStr.toLowerCase().endsWith(".jpg")) {
                    //清空当前输入框内容
                    editText.setText("");
                    if (isAllowSendMsg()){
                        //发送图片
                        sendMsgListener.sendKeyboardImage(tempStr, 400, 400, false);
                    }
                } else {
                    editText.setText("");
                    ToastUtils.showShort("不支持该表情图片");
                }
            }
        } else if (content.trim().length() > 0) {
            if (activity.getCurrentFocus() == editText) {
//                notifyTyping(TypingMessageContent.TYPING_TEXT);
            }
//            extImageView.setVisibility(View.GONE);
//            sendButton.setVisibility(View.VISIBLE);

            //@功能处理
            if (s.length() == 1) {
                if (content.equals("@")) {
                    if (notifyListener != null) {
                        notifyListener.onNotifyListener(editText.getText().toString());
                    }
                }
            } else {
                if (editText.getText().toString().lastIndexOf(" @") == s.length() - 1) {
                    notifyListener.onNotifyListener(editText.getText().toString());
                } else {
                    int curIndex = editText.getSelectionStart();
                    int lastAt = content.lastIndexOf("@", curIndex);
                    if (lastAt != -1) {
                        //检索到了@符号
                        String before = content.substring(0, lastAt);
                        String end = content.substring(lastAt, s.length());

                        if (lastAt == 0) {
                            notifyListener.onSearchMem(end);
                        } else if ((before.endsWith(" ") && end.endsWith(" "))) {
                            //@符号前面是空格&后面是空格，不进行搜索
                            notifyListener.onCancenAt();
                        } else if (!before.endsWith(" ")) {
                            //不处理
                        } else {
                            notifyListener.onSearchMem(end);
                        }
                    } else {
                        //没有检索到@符号
                        notifyListener.onCancenAt();
                    }
                }

            }
        } else {
            notifyListener.onCancenAt();

//            sendButton.setVisibility(View.GONE);
//            extImageView.setVisibility(View.VISIBLE);
        }


    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
//            return EmojiUtils.INSTANCE.deleteEmojiInfo(editText);
//        }
        return false;
    }

    public interface OnConversationInputPanelStateChangeListener {
        /**
         * 输入面板展开
         */
        void onInputPanelExpanded();

        /**
         * 输入面板关闭
         */
        void onInputPanelCollapsed();

        /**
         * 录音成功
         */
        void onRecordSuccess(String audioFile, int duration);
    }

    /**
     * 显示键盘
     */
    public void showInput() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rootLinearLayout.showSoftkey(editText);
            }
        }, 100);
    }
}
