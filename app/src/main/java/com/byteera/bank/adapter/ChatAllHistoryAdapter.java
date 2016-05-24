package com.byteera.bank.adapter;

import android.content.Context;

import com.byteera.bank.utils.GetDataUtils;
import com.byteera.bank.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.SmileUtils;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.hxlib.utils.Constants;
import com.easemob.chat.EMContact;
import com.easemob.chat.EMConversation;
import com.easemob.chat.EMGroup;
import com.easemob.chat.EMGroupManager;
import com.easemob.chat.EMMessage;
import com.easemob.chat.ImageMessageBody;
import com.easemob.chat.TextMessageBody;
import com.easemob.util.DateUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** 显示所有聊天记录adpaterE */
public class ChatAllHistoryAdapter extends ArrayAdapter<EMConversation> {
    private static final String TAG = ChatAllHistoryAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private List<EMConversation> conversationList;
    private List<EMConversation> copyConversationList;
    private ConversationFilter conversationFilter;
    private UserDao dao;
    private Context mContext;

    public ChatAllHistoryAdapter(Context context, int textViewResourceId,
                                 List<EMConversation> conversationList) {
        super(context, textViewResourceId, conversationList);
        this.mContext = context;
        this.conversationList = conversationList;
        copyConversationList = new ArrayList<>();
        copyConversationList.addAll(conversationList);
        inflater = LayoutInflater.from(context);
        dao = new UserDao(getContext());
    }

    public void refreshList()
    {
        this.conversationList.clear();
        this.conversationList.addAll(GetDataUtils.loadConversationsWithRecentChat());
        notifyDataSetChanged();
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_chat_history, parent, false);
        }
        TextView name = ViewHolder.get(convertView, R.id.name);
        TextView unreadLabel = ViewHolder.get(convertView, R.id.unread_msg_number);
        TextView message = ViewHolder.get(convertView, R.id.message);
        TextView time = ViewHolder.get(convertView, R.id.time);
        ImageView avatar = ViewHolder.get(convertView, R.id.avatar);
        GridView gridViewHead = ViewHolder.get(convertView, R.id.gridview_head);
        View msgState = ViewHolder.get(convertView, R.id.msg_state);
        RelativeLayout list_item_layout = ViewHolder.get(convertView, R.id.list_item_layout);

//        if (position % 2 == 0) {
//            list_item_layout.setBackgroundResource(R.drawable.mm_listitem);
//        } else {
//            list_item_layout.setBackgroundResource(R.drawable.mm_listitem_grey);
//        }

        // 获取与此用户/群组的会话
        EMConversation conversation = getItem(position);
        LogUtil.d("test", "-test->" + conversation.getUserName());
        String username = conversation.getUserName();
        List<EMGroup> groups = EMGroupManager.getInstance().getAllGroups();
        EMContact contact = null;
        boolean isGroup = false;
        for (EMGroup group : groups) {
            if (group.getGroupId().equals(username)) {
                isGroup = true;
                contact = group;
                break;
            }
        }

        if (isGroup) {
            avatar.setVisibility(View.GONE);
            gridViewHead.setVisibility(View.VISIBLE);
            //获得所有的群成员
            List<String> tempList = new ArrayList<>();
            List<String> members = ((EMGroup) contact).getMembers();
            LogUtil.d("members: %s", members.toString());
            for (int j = 0; j < members.size(); j++) {
                User user = dao.selectUser(members.get(j));
                if (user != null) {
                    String head_photo = user.getAvatar();
                    if (head_photo == null) {
                        head_photo = "";
                    }
                    tempList.add(head_photo);
                }
            }
            LogUtil.d(tempList.toString());
            GridViewAdapter gridViewAdapter = new GridViewAdapter(tempList);
            gridViewHead.setAdapter(gridViewAdapter);
            name.setText(contact.getNick() != null ? contact.getNick() : username);

        } else {
            avatar.setVisibility(View.VISIBLE);
            gridViewHead.setVisibility(View.GONE);
            User hzUser = dao.selectUser(username);
            if (hzUser != null) {
                // 本地或者服务器获取用户详情，以用来显示头像和nick
                if (!hzUser.getAvatar().equals("")) {
                    ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + hzUser.getAvatar(), avatar);
                }
                name.setText(hzUser.getNickName() == null ? username : hzUser.getNickName());
            }

            if (username.equals(Constants.GROUP_USERNAME)) {
                name.setText("群聊");
            } else if (username.equals(Constants.NEW_FRIENDS_USERNAME)) {
                name.setText("申请与通知");
            }
        }

        if (conversation.getUnreadMsgCount() > 0) {
            // 显示与此用户的消息未读数
            unreadLabel.setText(String.valueOf(conversation.getUnreadMsgCount()));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }

        if (conversation.getMsgCount() != 0) {
            // 把最后一条消息的内容作为item的message内容
            EMMessage lastMessage = conversation.getLastMessage();
            message.setText(SmileUtils.getSmiledText(getContext(), getMessageDigest(lastMessage, (this.getContext()))), TextView.BufferType.SPANNABLE);
            time.setText(DateUtils.getTimestampString(new Date(lastMessage.getMsgTime())));
            if (lastMessage.direct == EMMessage.Direct.SEND && lastMessage.status == EMMessage.Status.FAIL) {
                msgState.setVisibility(View.VISIBLE);
            } else {
                msgState.setVisibility(View.GONE);
            }
        }

        if (username.equals("admin")) {
            name.setText("公告通知");
            avatar.setImageResource(R.drawable.default_avatar);
        }

        return convertView;
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     */
    private String getMessageDigest(EMMessage message, Context context) {
        LogUtil.d("-chatAll adapter->" + message.toString());
        String digest = "";
        switch (message.getType()) {
            case LOCATION: // 位置消息
                if (message.direct == EMMessage.Direct.RECEIVE) {
                    // 从sdk中提到了ui中，使用更简单不犯错的获取string的方法
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_recv");
                    digest = getStrng(context, R.string.location_recv);
                    digest = String.format(digest, message.getFrom());
                    return digest;
                } else {
                    // digest = EasyUtils.getAppResourceString(context,
                    // "location_prefix");
                    digest = getStrng(context, R.string.location_prefix);
                }
                break;
            case IMAGE: // 图片消息
                ImageMessageBody imageBody = (ImageMessageBody) message.getBody();
                digest = getStrng(context, R.string.picture) + imageBody.getFileName();
                break;
            case VOICE:// 语音消息
                digest = getStrng(context, R.string.voice);
                break;
            case VIDEO: // 视频消息
                digest = getStrng(context, R.string.video);
                break;
            case TXT: // 文本消息
                if (!message.getBooleanAttribute(Constants.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = txtBody.getMessage();
                } else {
                    TextMessageBody txtBody = (TextMessageBody) message.getBody();
                    digest = getStrng(context, R.string.voice_call) + txtBody.getMessage();
                }
                break;
            case FILE: // 普通文件消息
                digest = getStrng(context, R.string.file);
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }

        return digest;
    }


    String getStrng(Context context, int resId) {
        return context.getResources().getString(resId);
    }


    @Override
    public Filter getFilter() {
        if (conversationFilter == null) {
            conversationFilter = new ConversationFilter(conversationList);
        }
        return conversationFilter;
    }

    private class ConversationFilter extends Filter {
        List<EMConversation> mOriginalValues = null;

        public ConversationFilter(List<EMConversation> mList) {
            mOriginalValues = mList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                mOriginalValues = new ArrayList<EMConversation>();
            }
            if (prefix == null || prefix.length() == 0) {
                results.values = copyConversationList;
                results.count = copyConversationList.size();
            } else {
                String prefixString = prefix.toString();
                final int count = mOriginalValues.size();
                final ArrayList<EMConversation> newValues = new ArrayList<EMConversation>();

                for (int i = 0; i < count; i++) {
                    final EMConversation value = mOriginalValues.get(i);
                    String username = value.getUserName();

                    EMGroup group = EMGroupManager.getInstance().getGroup(username);
                    if (group != null) {
                        username = group.getGroupName();
                    }

                    // First match against the whole ,non-splitted value
                    if (username.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = username.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            conversationList.clear();
            conversationList.addAll((List<EMConversation>) results.values);
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }

        }

    }


    private class GridViewAdapter extends BaseAdapter {
        private List<String> gridList;

        public GridViewAdapter(List<String> gridList) {
            this.gridList = gridList;
        }

        @Override public int getCount() {
            if (gridList.size() <= 9) {
                return gridList.size();
            } else {
                return 9;
            }
        }

        @Override public Object getItem(int position) {
            return gridList.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, null);
            }

            ImageView ivHead = ViewHolder.get(convertView, R.id.iv_head);

            String avatar = gridList.get(position);

            if(!StringUtils.isEmpty(avatar))
                ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + avatar, ivHead);
            else
                ImageLoader.getInstance().displayImage(null, ivHead);

            return convertView;
        }
    }

    @Override public boolean areAllItemsEnabled() {
        return true;
    }
}
