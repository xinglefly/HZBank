package com.byteera.bank.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.byteera.R;
import com.byteera.bank.BankHXSDKHelper;
import com.byteera.bank.adapter.ContactAdapter;
import com.byteera.bank.domain.User;
import com.byteera.bank.widget.Sidebar;
import com.byteera.hxlib.controller.HXSDKHelper;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PickContactNoCheckboxActivity extends BaseActivity {

	private ListView listView;
	private Sidebar sidebar;
	protected ContactAdapter contactAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_contact_no_checkbox);
		listView = (ListView) findViewById(R.id.list);
		sidebar = (Sidebar) findViewById(R.id.sidebar);
		sidebar.setListView(listView);
		List<User> contactList = getContactList();
		contactAdapter = new ContactAdapter(this, R.layout.row_contact, contactList);
		listView.setAdapter(contactAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onListItemClick(position);
			}
		});

	}

	protected void onListItemClick(int position) {
		if (position != 0) {
			setResult(RESULT_OK, new Intent().putExtra("username", contactAdapter.getItem(position)
					.getUsername()));
			ActivityUtil.finishActivity(baseContext);
		}
	}

	public void back(View view) {
		ActivityUtil.finishActivity(baseContext);
	}

	private List<User> getContactList() {
		ArrayList<User> contactList = new ArrayList<>();

		Map<String, User> users = ((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList();
		Iterator<Entry<String, User>> iterator = users.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, User> entry = iterator.next();
			if (!entry.getKey().equals(Constants.NEW_FRIENDS_USERNAME) && !entry.getKey().equals(Constants.GROUP_USERNAME))
				contactList.add(entry.getValue());
		}
		// 排序
		Collections.sort(contactList, new Comparator<User>() {

			@Override
			public int compare(User lhs, User rhs) {
				return lhs.getUsername().compareTo(rhs.getUsername());
			}
		});

		return contactList;
	}

}
