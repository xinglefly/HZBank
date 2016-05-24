package com.byteera.bank.activity.business_circle.activity.util;

import android.content.Context;

import com.byteera.bank.domain.ChatContent;
import com.byteera.bank.domain.VisitorContent;
import com.byteera.bank.utils.*;
import com.byteera.hxlib.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhenxing on 2015/7/16.
 */
@SuppressWarnings("unused")
public class ParseJsonStr {

    private static final String TAG = ParseJsonStr.class.getSimpleName();

    private Context context;

    public ParseJsonStr(Context context) {
        this.context = context;
    }

    /**
     * 解析会务圈json data
     * @param result
        {
        "content": "朝鲜和韩国",
        "publisher": "55a0a2d20cecf82bfe95f5a9",
        "publish_time": 1436940539,
        "name": "那宝军",
        "head_photo": "/fc/e557c06e-2acd-11e5-9f09-52547eeb4f1ca2e72964-8f37-496e-b6d4-d0b619653926.jpg",
        "reply": [ ],
        "image": [
        "/fc/fba9fb22-2ab7-11e5-a5c0-52547eeb4f1c517f3dd4-69e6-4a4e-81ae-2614f738f39d.jpg"
        ],
        "id": "55a5f8fbfa7815400bfb1e29",
        "share_url": ""
        },
     */
    public static ArrayList<ChatContent>  parseChatJson(String result) throws JSONException{
        ArrayList<ChatContent> listChatContents = new ArrayList<ChatContent>();
        JSONTokener jsonTokener = new JSONTokener(result);
        JSONObject json  = new JSONObject(jsonTokener);
        String error = json.optString("error");
        String error_description = json.optString("error_description");
        if ("0".equals(error)) {
            JSONArray dataArray = json.optJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject data = (JSONObject) dataArray.get(i);

                String content = data.optString("content");
                String publisher = data.optString("publisher");
                String publish_time = data.optString("publish_time");
                String name = data.optString("name");
                String chat_id = data.optString("id");

				String head_photo = data.optString("head_photo");
                JSONArray replyArray = data.optJSONArray("reply");
                JSONArray imageArray = data.optJSONArray("image");

                //reply
                List<VisitorContent> contentLists = new ArrayList<VisitorContent>();
                if (replyArray.length()>0){
                    for (int j = 0; j < replyArray.length(); j++) {
                        JSONObject obj = (JSONObject) replyArray.get(j);
                        String vcontent = obj.optString("content");
                        int vindex = obj.optInt("index");
                        String vcreate_time = obj.optString("create_time");
                        String vuser_id = obj.optString("user_id");
                        String vname = obj.optString("name");
                        VisitorContent visitorContent = new VisitorContent(vcontent, vindex, vcreate_time,vuser_id, vname);
                        contentLists.add(visitorContent);
                    }
                }

                //image
                List<String> imgLists = new ArrayList<String>();
                if (imageArray.length()>0){
                    for (int j = 0; j < imageArray.length(); j++) {
                        String url = imageArray.getString(j);
                        imgLists.add(Constants.BYTEERA_SERVICE+url);
                    }
                }

                ChatContent chatContent = new ChatContent(name,Constants.BYTEERA_SERVICE+head_photo ,content, publisher,publish_time, chat_id,contentLists,imgLists);
                listChatContents.add(chatContent);
            }
            LogUtil.d("--listChatContents-->"+ listChatContents.size());
        }
        return listChatContents;
    }
}
