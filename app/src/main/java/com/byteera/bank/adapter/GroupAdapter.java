/**
 * Copyright (C) 2013-2014 EaseMob Technologies. All rights reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byteera.bank.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;
import com.easemob.chat.EMGroup;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<EMGroup> {
    private LayoutInflater inflater;

    public GroupAdapter(Context context, int res, List<EMGroup> groups) {
        super(context, res, groups);
        this.inflater = LayoutInflater.from(context);
    }

    @Override public int getViewTypeCount() {
        return 2;
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
            ((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.roominfo_add_btn_normal);
            ((TextView) convertView.findViewById(R.id.name)).setText("会议群");

        } else if (getItemViewType(position) == 1) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_add_group, null);
            }
            ((ImageView) convertView.findViewById(R.id.avatar)).setImageResource(R.drawable.roominfo_add_btn_normal);
            ((TextView) convertView.findViewById(R.id.name)).setText("新建工作群组");
        } else {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.row_group, null);
            }
            ((TextView) convertView.findViewById(R.id.name)).setText(getItem(position - 1).getGroupName());

        }

        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}