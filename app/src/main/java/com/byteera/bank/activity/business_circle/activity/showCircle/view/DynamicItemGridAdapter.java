package com.byteera.bank.activity.business_circle.activity.showCircle.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.byteera.R;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ViewHolder;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by bing on 2015/6/4.
 */
public class DynamicItemGridAdapter extends BaseAdapter {
    private static final String TAG = DynamicItemGridAdapter.class.getSimpleName();
    private List<String> imgList;
    private Context mContext;
    private int numColumns;


    public DynamicItemGridAdapter(Context context, int numColumns, List<String> imgList) {
        this.mContext = context;
        this.numColumns = numColumns;
        this.imgList = imgList;
    }

    public void setData(List<String> list, int numColumns) {
        this.imgList = list;
        this.numColumns = numColumns;
    }

    @Override public int getItemViewType(int position) {
        return AdapterView.ITEM_VIEW_TYPE_IGNORE;
    }

    @Override public int getCount() {
        return imgList.size();
    }

    @Override public Object getItem(int arg0) {
        return imgList.get(arg0);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.img_view, parent, false);
        }

        String avatar = imgList.get(position);
        ImageView img = ViewHolder.get(convertView, R.id.img_content);
        ImageLoader.getInstance().displayImage(avatar, img);
        return convertView;
    }
}
