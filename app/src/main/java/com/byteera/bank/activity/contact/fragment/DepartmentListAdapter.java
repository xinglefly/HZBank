package com.byteera.bank.activity.contact.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;
import com.byteera.bank.activity.contact.ContactDetailActivity;
import com.byteera.bank.db.DBManager;
import com.byteera.bank.domain.Department;
import com.byteera.bank.domain.User;
import com.byteera.bank.utils.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by lieeber on 15/8/24. */
public class DepartmentListAdapter extends BaseAdapter {

    private Context mContext;
    private List currentList = new ArrayList();
    private LinearLayout mButtonContainer;
    private ListView mListView;
    private int maxDepth = 5;   //最深深度
    private int currentDepth = 0;   //当前界面的深度
    private String currentDepartment = "";  //当前的分行
    private String currentDepartmentOrderId = ""; //当前部门的排序编号
    private Map<String, String> departmentOrderPathMap = new HashMap<>();

    public void setView() {
        //添加所有的按钮
        for (int i = 1; i < maxDepth + 1; i++) {
            final Button button = (Button) LayoutInflater.from(mContext).inflate(R.layout.button_department, mButtonContainer, false);
            button.setTag(i);
            button.setVisibility(View.GONE);
            mButtonContainer.addView(button);
            final int depth = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    currentDepth = depth;
                    currentDepartment = button.getText().toString();
                    currentDepartmentOrderId = button.getContentDescription().toString();
                    notifyDataSetChanged();
                    for (int j = depth + 1; j < maxDepth + 1; j++) {
                        mButtonContainer.findViewWithTag(j).animate().alpha(0).setDuration(200);
                        final int finalJ = j;
                        MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                            @Override public void run() {
                                mButtonContainer.findViewWithTag(finalJ).setVisibility(View.GONE);
                            }
                        }, 200);
                    }
                }
            });
        }
    }

    public DepartmentListAdapter(final Context mContext, LinearLayout buttonContainer, ListView deptListView) {
        this.mContext = mContext;
        mButtonContainer = buttonContainer;
        this.mListView = deptListView;
        Button view = (Button) LayoutInflater.from(mContext).inflate(R.layout.button_department, mButtonContainer, false);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = UIUtils.dip2px(mContext,70);
        view.setLayoutParams(params);
        view.setText("部门");
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //点击部门按钮回到第一页
                currentDepth = 0;
                currentDepartment = "";
                currentDepartmentOrderId = "";
                notifyDataSetChanged();
                //隐藏其他按钮
                for (int i = 1; i < maxDepth + 1; i++) {
                    if(mButtonContainer.findViewWithTag(i) == null)
                    {
                        return;
                    }

                    mButtonContainer.findViewWithTag(i).animate().alpha(0).setDuration(200);
                    final int finalI = i;
                    MyApp.getMainThreadHandler().postDelayed(new Runnable() {
                        @Override public void run() {
                            mButtonContainer.findViewWithTag(finalI).setVisibility(View.GONE);
                        }
                    }, 200);
                }
            }
        });

        mButtonContainer.addView(view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //设置单击事件
                Object item = currentList.get(position);
                if (item instanceof User) {
                    Intent intent = new Intent(mContext, ContactDetailActivity.class);
                    intent.putExtra("user_id", ((User) item).getUserId());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    mContext.startActivity(intent);
                } else {
                    currentDepth += 1;
                    currentDepartment = ((Department) item).getDepartmentName();
                    currentDepartmentOrderId = ((Department) item).getOrderId();
                    departmentOrderPathMap.put(currentDepartmentOrderId, currentDepartment);
                    notifyDataSetChanged();
                    Button button = (Button) mButtonContainer.findViewWithTag(currentDepth);
                    String[] departments = currentDepartment.split("/");
                    button.setText(departments[departments.length - 1]);
                    button.setContentDescription(currentDepartmentOrderId);
                    button.setAlpha(0);
                    button.animate().alpha((maxDepth - currentDepth * 1.0f) / maxDepth).setDuration(200);
                    button.setVisibility(View.VISIBLE);
                }
            }
        });

        view.performClick();
    }

    @Override public int getCount() {
        return currentList.size();
    }

    @Override public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        List<Department> departmentList = DBManager.getInstance(mContext).getDepartmentList(currentDepartmentOrderId);

        String departmentPath = departmentOrderPathMap.get(currentDepartmentOrderId);
        List<User> userList = DBManager.getInstance(mContext).getUserListInDepartment(departmentPath);

        currentList.clear();
        currentList.addAll(departmentList);
        currentList.addAll(userList);
    }

    @Override public Object getItem(int position) {
        return null;
    }

    @Override public long getItemId(int position) {
        return 0;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.depart_list_item, null);
        }

        Object item = currentList.get(position);
        TextView dept = ViewHolder.get(convertView, R.id.dept);
        ImageView ivNext = ViewHolder.get(convertView, R.id.iv_next);
        if (item instanceof User) {
            ivNext.setVisibility(View.GONE);
            dept.setText(((User) item).getNickName());
        } else {
            ivNext.setVisibility(View.VISIBLE);
            String fullDepartment = ((Department) item).getDepartmentName();
            String[] departments = fullDepartment.split("/");
            dept.setText(departments[departments.length - 1]);
        }
        return convertView;
    }
}
