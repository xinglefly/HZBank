package com.byteera.bank.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.byteera.R;
import com.byteera.bank.BankHXSDKHelper;
import com.byteera.bank.adapter.NewFriendsMsgAdapter;
import com.byteera.bank.db.InviteMessgeDao;
import com.byteera.bank.domain.InviteMessage;
import com.byteera.hxlib.controller.HXSDKHelper;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;

import java.util.List;

/**
 * 申请与通知
 *
 */
public class NewFriendsMsgActivity extends BaseActivity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_friends_msg);

		listView = (ListView) findViewById(R.id.list);
		InviteMessgeDao dao = new InviteMessgeDao(this);
		List<InviteMessage> msgs = dao.getMessagesList();
		//设置adapter
		NewFriendsMsgAdapter adapter = new NewFriendsMsgAdapter(this, 1, msgs); 
		listView.setAdapter(adapter);
		((BankHXSDKHelper) HXSDKHelper.getInstance()).getContactList().get(Constants.NEW_FRIENDS_USERNAME).setUnreadMsgCount(0);
		
	}

	public void back(View view) {
		ActivityUtil.finishActivity(baseContext);
	}
	
	
}
