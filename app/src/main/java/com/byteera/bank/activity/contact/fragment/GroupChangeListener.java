package com.byteera.bank.activity.contact.fragment;

import com.easemob.chat.EMGroup;

import java.util.List;

public interface GroupChangeListener {
	void groupChanged(List<EMGroup> groupList);
}
