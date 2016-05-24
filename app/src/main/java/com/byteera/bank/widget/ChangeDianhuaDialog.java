package com.byteera.bank.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.byteera.R;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;

/** Created by bing on 2015/5/7. */
public class ChangeDianhuaDialog extends AlertDialog {
    private Context mContext;
    private EditText etNickName;

    public ChangeDianhuaDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    public ChangeDianhuaDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public interface OnOkClickListener {
        void onOk();
    }

    private OnOkClickListener onOkClickListener;

    public void setOnOkClickListener(OnOkClickListener listener) {
        this.onOkClickListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_dianhua_dialog);
        WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
        attributes.width = UIUtils.dip2px(mContext, 300);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        this.getWindow().setAttributes(attributes);

        etNickName = (EditText) findViewById(R.id.et_nickname);

        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

            }
        });
        findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkClickListener.onOk();
                dismiss();
            }
        });
    }

    public String getDianHua() {
        return etNickName.getText().toString();
    }

    public void setDianhua(String name) {
        if(etNickName!=null){
            etNickName.setText(name);
        }
    }
}
