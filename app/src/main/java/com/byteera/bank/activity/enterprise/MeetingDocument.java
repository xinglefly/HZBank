package com.byteera.bank.activity.enterprise;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.byteera.R;
import com.byteera.bank.MyApp;
import com.byteera.bank.activity.BaseActivity;
import com.byteera.bank.activity.enterprise.document.OpenFiles;
import com.byteera.bank.activity.enterprise.document.bean.DownloadInfo;
import com.byteera.bank.utils.LoadingDialogShow;
import com.byteera.bank.utils.MyhttpUtils;
import com.byteera.bank.utils.LogUtil;
import com.byteera.bank.utils.ToastUtil;
import com.byteera.bank.utils.ViewHolder;
import com.byteera.bank.widget.HeadViewMain;
import com.byteera.hxlib.utils.ActivityUtil;
import com.byteera.hxlib.utils.Constants;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.view.annotation.ViewInject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeetingDocument extends BaseActivity {
    
    protected static final String TAG = MeetingDocument.class.getSimpleName();
    
    @ViewInject(R.id.head_view) private HeadViewMain mHeadView;
    @ViewInject(R.id.download_list)private ListView lv_downlist;
    
    private List<DownloadInfo> lisDownloadInfos;
    private DownloadListAdapter adapter;
    private LoadingDialogShow dialog;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.document_list);

        ViewUtils.inject(this);

        dialog =new LoadingDialogShow(baseContext);
        String filePath = getIntent().getStringExtra("file_path");

        getDocumentList(filePath);

        mHeadView.setLeftImgClickListener(new HeadViewMain.LeftImgClickListner() {
            @Override
            public void onLeftImgClick() {
                ActivityUtil.finishActivity(baseContext);
            }
        });

    }


    private void getDocumentList(String filePath) {
        dialog.show();
        MyhttpUtils.getInstance().sendAsync(HttpMethod.GET,
                Constants.BYTEERA_SERVICE + "show/file/" + filePath,
                new RequestCallBack<String>() {
                    @Override
                    public void onFailure(HttpException error, String msg) {
                        LogUtil.d("onFailure:" + msg);
                        ToastUtil.showToastText("获取文件失败");
                        dialog.dismiss();
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> resInfo) {
                        try {
                            String result = resInfo.result;
                            LogUtil.d("onSuccess:" + result);
                            JSONTokener jsonTokener = new JSONTokener(result);
                            JSONObject json = new JSONObject(jsonTokener);
                            int count = json.optInt("count");

                            if (count > 0) {
                                JSONArray array = json.optJSONArray("data");
                                lisDownloadInfos = new ArrayList<DownloadInfo>();
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject data = (JSONObject) array.get(i);
                                    String name = data.optString("name");
                                    String file = data.optString("file");
                                    lisDownloadInfos.add(new DownloadInfo(name, file));
                                }

                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        dialog.dismiss();
                                        adapter = new DownloadListAdapter(lisDownloadInfos, baseContext);
                                        lv_downlist.setAdapter(adapter);
                                    }
                                });

                            } else {
                                dialog.dismiss();
                                ToastUtil.showToastText("该会议暂无资料。");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            dialog.dismiss();
                        }
                    }
                });
    }
    
    

    
    
    private class DownloadListAdapter extends BaseAdapter {

        private final Context mContext;
        private final LayoutInflater mInflater;
        private List<DownloadInfo> downloadInfos;

        private DownloadListAdapter(List<DownloadInfo> downloadInfos,Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(mContext);
            this.downloadInfos = downloadInfos;
        }

        @Override
        public int getCount() {
           return downloadInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return downloadInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null) {
                convertView = mInflater.inflate(R.layout.document_item, null);
            }
           
           TextView tv_title = ViewHolder.get(convertView, R.id.tv_title);
           final ProgressBar pb_download = ViewHolder.get(convertView, R.id.pb_download);
           final TextView tv_download = ViewHolder.get(convertView, R.id.tv_download);

           final DownloadInfo loadInfo = downloadInfos.get(position);
           tv_title.setText(loadInfo.getName());
           LogUtil.d("--loadinfo-->"+loadInfo.getName()+","+loadInfo.getUrl());

            //判断文件是否已经下载完成
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bank/"+loadInfo.getName();

            tv_download.setOnClickListener(new OnClickListener() {

                    @Override public void onClick(View v) {

                        final File file = new File(path);
                        if(file.exists()) {
                            openFile(path);
                        }else {
                            MyApp.getInstance().executorService.submit(new Runnable() {
                                @Override
                                public void run() {
                                    MyhttpUtils.getInstance().download(Constants.BYTEERA_SERVICE + loadInfo.getUrl(), path, new RequestCallBack<File>() {

                                        @Override
                                        public void onStart() {
                                            super.onStart();
                                        }

                                        @Override
                                        public void onLoading(long total, final long current,
                                                              boolean isUploading)
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pb_download.setVisibility(View.VISIBLE);
                                                    pb_download.setProgress((int) current * 100 / 100);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(HttpException arg0, String arg1) {
                                            LogUtil.d("fail-->" + arg1 + "," + arg0.toString());
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    tv_download.setText("失败");
                                                }
                                            });
                                        }

                                        @Override
                                        public void onSuccess(final ResponseInfo<File> file) {
                                            LogUtil.d("--file-->");
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    pb_download.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                            openFile(path);
                                        }
                                    });
                                }
                            });
                        }
                    }

            });
            return convertView;
        }

        private void openFile(String path) {
            File file = new File(path);
            if (file != null && file.isFile()) {
                try
                {
                    Intent intent;
                    if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingImage))) {
                        intent = OpenFiles.getImageFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingWebText))) {
                        intent = OpenFiles.getHtmlFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingPackage))) {
                        intent = OpenFiles.getApkFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingAudio))) {
                        intent = OpenFiles.getAudioFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingVideo))) {
                        intent = OpenFiles.getVideoFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingText))) {
                        intent = OpenFiles.getTextFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingPdf))) {
                        intent = OpenFiles.getPdfFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingWord))) {
                        intent = OpenFiles.getWordFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingExcel))) {
                        intent = OpenFiles.getExcelFileIntent(file);
                        startActivity(intent);
                    } else if (checkEndsWithInStringArray(path, getResources().getStringArray(R.array.fileEndingPPT))) {
                        intent = OpenFiles.getPPTFileIntent(file);
                        startActivity(intent);
                    } else {
                        ToastUtil.showToastText("无法打开，请安装相应的软件！");
                    }
                }
                catch (Exception e)
                {
                    LogUtil.e(e, "Open file exception");
                    ToastUtil.showToastText("无法打开该文件，请安装相应的阅读软件后再试！");
                }
            } else {
                ToastUtil.showToastText("对不起，这不是文件！");
            }
        }


        private boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
            for (String aEnd : fileEndings) {
                if (checkItsEnd.endsWith(aEnd))
                    return true;
            }
            return false;
        }

    }





}
