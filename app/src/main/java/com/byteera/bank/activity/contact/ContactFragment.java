package com.byteera.bank.activity.contact;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.AlertDialog;
import com.byteera.bank.activity.ChatActivity;
import com.byteera.bank.activity.NewWorkingGroupActivity;
import com.byteera.bank.activity.contact.fragment.DepartmentListFragment;
import com.byteera.bank.activity.contact.fragment.FriendListFragment;
import com.byteera.bank.activity.contact.fragment.GroupListFragment;
import com.byteera.bank.activity.contact.fragment.GroupListFragment.GroupListFragmentListener;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.hxlib.utils.ActivityUtil;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroup;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment {
    private ViewPager vPager;
    private RelativeLayout layoutContact, layoutOrganization, layoutGroupChat;
    private TextView titleText1, titleText2, titleText3;
    private ImageView titleLine1, titleLine2, titleLine3;
    private List<Fragment> fragments;

    private int currentPagerIndex = 0;
    private boolean isLoaded = false;
    private DepartmentListFragment departFragment;
    private GroupListFragment oldGroupListFragment;
    private FriendListFragment contactListFragment;
    protected LoadingDialogShow dialog = null;


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_tab_contacts, container, false);
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        vPager = (ViewPager) getView().findViewById(R.id.vPager);
        vPager.setOffscreenPageLimit(5);
        layoutContact = (RelativeLayout) getView().findViewById(R.id.layout_contact);
        layoutOrganization = (RelativeLayout) getView().findViewById(R.id.layout_organization);
        layoutGroupChat = (RelativeLayout) getView().findViewById(R.id.layout_group_chat);
        titleText1 = (TextView) getView().findViewById(R.id.title1);
        titleText2 = (TextView) getView().findViewById(R.id.title2);
        titleText3 = (TextView) getView().findViewById(R.id.title3);
        titleLine1 = (ImageView) getView().findViewById(R.id.iv_line1);
        titleLine2 = (ImageView) getView().findViewById(R.id.iv_line2);
        titleLine3 = (ImageView) getView().findViewById(R.id.iv_line3);
        fragments = new ArrayList<>();
        contactListFragment = new FriendListFragment();    //联系人
        departFragment = new DepartmentListFragment();
        dialog = new LoadingDialogShow(getActivity());
        setView(MyApp.getInstance().getGroupList());
    }

    public void setView(List<EMGroup> tempGroup) {

        oldGroupListFragment = new GroupListFragment(tempGroup, new MyGroupFragmentListListener()); //工作组
        fragments.add(contactListFragment);
        fragments.add(departFragment);
        fragments.add(oldGroupListFragment);
        // set viewpager adapter
        vPager.setAdapter(new ContactFragmentPagerAdapter(getChildFragmentManager(), fragments));
        vPager.addOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                switch (arg0) {
                    case 0:
                        if (currentPagerIndex == 1) {
                            setToNormalColor(titleText2);
                            titleLine2.setVisibility(View.INVISIBLE);
                        } else if (currentPagerIndex == 2) {
                            setToNormalColor(titleText3);
                            titleLine3.setVisibility(View.INVISIBLE);
                        }
                        setToSelectedColor(titleText1);
                        titleLine1.setVisibility(View.VISIBLE);

                        break;
                    case 1:
                        if (currentPagerIndex == 0) {
                            setToNormalColor(titleText1);
                            titleLine1.setVisibility(View.INVISIBLE);
                        } else if (currentPagerIndex == 2) {
                            setToNormalColor(titleText3);
                            titleLine3.setVisibility(View.INVISIBLE);
                        }
                        setToSelectedColor(titleText2);
                        titleLine2.setVisibility(View.VISIBLE);

                        break;
                    case 2:
                        if (currentPagerIndex == 1) {
                            setToNormalColor(titleText2);
                            titleLine2.setVisibility(View.INVISIBLE);
                        } else if (currentPagerIndex == 0) {
                            setToNormalColor(titleText1);
                            titleLine1.setVisibility(View.INVISIBLE);
                        }
                        setToSelectedColor(titleText3);
                        titleLine3.setVisibility(View.VISIBLE);

                        break;

                }
                currentPagerIndex = arg0;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        vPager.setCurrentItem(0);
        layoutContact.setOnClickListener(new ContactTitleClickListener(0));
        layoutOrganization.setOnClickListener(new ContactTitleClickListener(1));
        layoutGroupChat.setOnClickListener(new ContactTitleClickListener(2));
        isLoaded = true;
        dialog.dismiss();
    }

    protected class ContactTitleClickListener implements View.OnClickListener {
        private int index = 0;

        public ContactTitleClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            vPager.setCurrentItem(index);
        }
    }

    protected void setToSelectedColor(TextView textView) {
        textView.setTextColor(getResources().getColor(android.R.color.white));
    }

    protected void setToNormalColor(TextView textView) {
        textView.setTextColor(getResources().getColor(R.color.contacts_title_nomal_color));
    }

    //Fragment点击事件
    class MyGroupFragmentListListener implements GroupListFragmentListener {
        @Override public void onListItemClickListener(int position) {
            if (position == 0) {
                //跳转进去查看会议群
                Intent intent = new Intent(getActivity(), HuiYiQunActivity.class);
                startActivity(intent);
            } else if (position == 1) {
                if (EMChatManager.getInstance().isConnected()) {
                    Intent intent = new Intent(getActivity(), NewWorkingGroupActivity.class);
                    intent.putExtra("description", true);
                    ActivityUtil.startActivityForResult(getActivity(), intent, 0);
                } else {
                    ActivityUtil.startActivity(getActivity(), new Intent(getActivity(), AlertDialog.class).putExtra("msg", getActivity().getString(R.string.network_unavailable)));
                }
            } else {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("isChat", false);
                intent.putExtra("chatType", ChatActivity.CHATTYPE_GROUP);
                intent.putExtra("position", position - 2);
                intent.putExtra("groupId", oldGroupListFragment.groupAdapter.getGroupItem(position - 2).getGroupId());
                startActivityForResult(intent, 0);
            }
        }
    }

    class ContactFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;

        public ContactFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int arg0) {
            return fragments.get(arg0);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden)
        {
            if(!isLoaded)
            {
                dialog.setMessage("正在加载群组...");
                dialog.show();
            }
        }
    }
}
