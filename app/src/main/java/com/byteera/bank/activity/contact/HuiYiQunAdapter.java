package com.byteera.bank.activity.contact;

import android.content.Context;
import com.byteera.bank.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.hxlib.utils.Constants;
import com.easemob.chat.EMGroup;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Created by lieeber on 15/8/29. */
public class HuiYiQunAdapter extends BaseAdapter {
    private Context mContext;
    private List<EMGroup> mList;
    private List<List<User>> groups;

    public HuiYiQunAdapter(Context mContext) {
        this.mContext = mContext;
        this.mList = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public void setData(List<List<User>> groups, List<EMGroup> mList) {
        this.mList = mList;
        this.groups = groups;
    }

    @Override public int getCount() {
        return groups.size();
    }

    @Override public List<User> getItem(int position) {
        return groups.get(position);
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.row_group, null);
        }
        TextView groupName = ViewHolder.get(convertView, R.id.name);
        groupName.setText(mList.get(position).getGroupName());
        GridView gridViewHead = ViewHolder.get(convertView, R.id.gridview_head);
        List<String> imgList = new ArrayList<>();
        List<User> item = getItem(position);
        if (item != null) {
            for (int i = 0; i < item.size(); i++) {
                User user = item.get(i);
                if (user != null) {
                    String avatar = item.get(i).getAvatar();
                    if (avatar == null) {
                        avatar = "";
                    }
                    imgList.add(avatar);
                }
            }
        }
        GridViewAdapter gridViewAdapter = new GridViewAdapter(imgList);
        gridViewHead.setAdapter(gridViewAdapter);
        return convertView;
    }

    public EMGroup getGroupItem(int position) {
        return mList.get(position);
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
            String avatar = gridList.get(position);
            ImageView ivHead = ViewHolder.get(convertView, R.id.iv_head);
            if(!StringUtils.isEmpty(avatar))
                ImageLoader.getInstance().displayImage(Constants.BYTEERA_SERVICE + gridList.get(position), ivHead);
            else
                ImageLoader.getInstance().displayImage(null, ivHead);

            return convertView;
        }
    }
}
