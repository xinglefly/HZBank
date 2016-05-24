
package com.byteera.bank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class GroupAdapter2 extends ArrayAdapter<List<User>> {
    private LayoutInflater inflater;
    private List<EMGroup> groupList;
    private Context mContext;

    public GroupAdapter2(Context context, int res, List<List<User>> groups, List<EMGroup> grouplist) {
        super(context, res, groups);
        this.mContext = context;
        this.groupList = grouplist;
        this.inflater = LayoutInflater.from(context);
    }

    public EMGroup getGroupItem(int position) {
        return groupList.get(position);
    }

    @Override public int getViewTypeCount() {
        return 3;
    }

    @Override public int getItemViewType(int position) {

        if (position == 0) {
            return 0;
        } else if (position == 1) {
            return 1;
        } else {
            return 2;
        }

    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == 0) {
            //显示为会议群组
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_add_group, null);
            }
            ImageView avatar = ViewHolder.get(convertView, R.id.avatar);
            TextView name = ViewHolder.get(convertView, R.id.name);
            ImageView iVRightDetail = ViewHolder.get(convertView, R.id.iv_right_detail);
            iVRightDetail.setVisibility(View.VISIBLE);
            avatar.setImageResource(R.drawable.metting_group);
            name.setText("会议群");
        } else if (getItemViewType(position) == 1) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_add_group, null);
            }
            ImageView avatar = ViewHolder.get(convertView, R.id.avatar);
            TextView name = ViewHolder.get(convertView, R.id.name);
            avatar.setImageResource(R.drawable.roominfo_add_btn_normal);
            name.setText("新建工作群组");
        } else {
            position -= 2;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_group, null);
            }
            TextView groupName = ViewHolder.get(convertView, R.id.name);
            groupName.setText(groupList.get(position).getGroupName());
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
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount() + 2;
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
            return groupList.get(position);
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