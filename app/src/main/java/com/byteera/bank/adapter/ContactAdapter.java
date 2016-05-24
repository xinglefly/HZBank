package com.byteera.bank.adapter;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.db.UserDao;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.Sidebar;
import com.byteera.hxlib.utils.Constants;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** 简单的好友Adapter实现 */
public class ContactAdapter extends ArrayAdapter<User>  implements SectionIndexer{
    private LayoutInflater layoutInflater;
    private SparseIntArray positionOfSection;
    private SparseIntArray sectionOfPosition;
    private int res;


    public ContactAdapter(Context context, int resource, List<User> userList) {
        super(context, resource, userList);
        this.res = resource;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){convertView = layoutInflater.inflate(res, parent,false);}
        ImageView avatar = ViewHolder.get(convertView, R.id.avatar);
        TextView unreadMsgView = ViewHolder.get(convertView, R.id.unread_msg_number);
        TextView nameTextview = ViewHolder.get(convertView, R.id.name);
        TextView tvHeader = ViewHolder.get(convertView, R.id.header);
        TextView tv_userid = ViewHolder.get(convertView, R.id.tv_userid);
        User user = getItem(position);
        String firstLetter = user.getFirstLetter();
        if (position == 0 || firstLetter != null && !firstLetter.equals(getItem(position - 1).getFirstLetter())) {
            if ("".equals(firstLetter)) {
                tvHeader.setVisibility(View.GONE);
            } else {
                tvHeader.setVisibility(View.VISIBLE);
                tvHeader.setText(firstLetter);
            }
        } else {
            tvHeader.setVisibility(View.GONE);
        }

        nameTextview.setText(user.getNickName());
        if (!user.getAvatar().equals("")){
            ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + user.getAvatar(),avatar);
        }else{
            avatar.setImageResource(R.drawable.head_down);
        }
        if(unreadMsgView != null) {
            unreadMsgView.setVisibility(View.INVISIBLE);
        }
        tv_userid.setText(user.getUserId());
        return convertView;
    }


    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getCount() {
        //有搜索框，count+1
//		return super.getCount() + 1;
        return super.getCount();
    }

    public int getPositionForSection(int section) {
        return positionOfSection.get(section);
    }

    public int getSectionForPosition(int position) {
        return sectionOfPosition.get(position);
    }

    @Override
    public Object[] getSections() {
        positionOfSection = new SparseIntArray();
        sectionOfPosition = new SparseIntArray();
        int count = getCount();
        List<String> list = new ArrayList<String>();
        list.add("");
        positionOfSection.put(0, 0);
        sectionOfPosition.put(0, 0);
        for (int i = 1; i < count; i++) {
            String letter = getItem(i).getFirstLetter();
            int section = list.size() - 1;
            if (list.get(section) != null && !list.get(section).equals(letter)) {
                list.add(letter);
                section++;
                positionOfSection.put(section, i);
            }
            sectionOfPosition.put(i, section);
        }
        return list.toArray(new String[list.size()]);
    }

}
