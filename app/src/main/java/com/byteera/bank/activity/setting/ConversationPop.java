package com.byteera.bank.activity.setting;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.byteera.R;


/** Created by lieeber on 15/8/7. */
public class ConversationPop {

    private PopupWindow conversationPop;

    public interface GroupChatListener {
        void groupChat();
    }

    public interface AddFriendsListener {
        void addFriends();
    }

    public interface SaoyisaoListener {
        void saoyisao();
    }

    private GroupChatListener groupChatListener;
    private AddFriendsListener addFriendsListener;
    private SaoyisaoListener saoyisaoListener;

    public void setSaoyisaoListener(SaoyisaoListener saoyisaoListener) {
        this.saoyisaoListener = saoyisaoListener;
    }

    public void setGroupChatListener(GroupChatListener groupChatListener) {
        this.groupChatListener = groupChatListener;
    }

    public void setAddFriendsListener(AddFriendsListener addFriendsListener) {
        this.addFriendsListener = addFriendsListener;
    }

    public void showAtLocation(View locationView, final Activity activity) {
        conversationPop.setFocusable(true);
        conversationPop.setOutsideTouchable(true);
        conversationPop.setBackgroundDrawable(new BitmapDrawable());
        conversationPop.setAnimationStyle(R.style.conversationPop);
        conversationPop.update();
        conversationPop.showAsDropDown(locationView);
    }

    public ConversationPop(Activity mContext) {
        View view = mContext.getLayoutInflater().inflate(R.layout.conversation_pop, null);
        conversationPop = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView tvChatGroup = (TextView) view.findViewById(R.id.tv_chat_group);
        final TextView tvAddFriends = (TextView) view.findViewById(R.id.tv_add_friends);
        final TextView tvSaoyisao = (TextView) view.findViewById(R.id.tv_saoyisao);
        tvChatGroup.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                conversationPop.dismiss();
                if (groupChatListener != null) {
                    groupChatListener.groupChat();
                }

            }
        });
        tvAddFriends.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                conversationPop.dismiss();
                if (addFriendsListener != null) {
                    addFriendsListener.addFriends();
                }
            }
        });

        tvSaoyisao.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                conversationPop.dismiss();
                if (saoyisaoListener != null) {
                    saoyisaoListener.saoyisao();
                }
            }
        });
    }
}
