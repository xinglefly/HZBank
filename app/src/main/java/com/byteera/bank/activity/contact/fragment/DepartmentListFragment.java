package com.byteera.bank.activity.contact.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.byteera.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


public class DepartmentListFragment extends Fragment {
    @ViewInject(R.id.list) private ListView deptListView;
    @ViewInject(R.id.button_container) private LinearLayout buttonContainer;
    private DepartmentListAdapter mAdapter;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewUtils.inject(this, getView());
        mAdapter = new DepartmentListAdapter(getActivity(),buttonContainer,deptListView);
        deptListView.setAdapter(mAdapter);
        mAdapter.setView();
    }
}
