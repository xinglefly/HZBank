package com.byteera.bank.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;

import com.byteera.R;
import com.byteera.bank.activity.business_circle.activity.util.UIUtils;

/** Created by bing on 2015/5/7. */
public class ChangeSexDialog extends AlertDialog {
    private Context mContext;
    private RadioButton rbMale;
    private RadioButton rbFemale;


    public interface  OnOkClickListener{
       void onOk();
    }
    private OnOkClickListener okClickListener;
    public void setOkClickListener(OnOkClickListener listener) {
        this.okClickListener = listener;
    }
    public ChangeSexDialog(Context context) {
        this(context,0);
    }

    public ChangeSexDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_sex_dialog);
        WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
        attributes.width = UIUtils.dip2px(mContext, 300);
        this.getWindow().setAttributes(attributes);

        rbMale = (RadioButton) findViewById(R.id.rb_male);
        rbFemale = (RadioButton) findViewById(R.id.rb_female);

        findViewById(R.id.ll_male).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbMale.setChecked(true);
                rbFemale.setChecked(false);
            }
        });

        findViewById(R.id.ll_female).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbMale.setChecked(false);
                rbFemale.setChecked(true);
            }
        });
        findViewById(R.id.tv_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okClickListener.onOk();
                dismiss();
            }
        });
        findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setSex(String sex) {
        if ("男".equals(sex)) {
            rbMale.setChecked(true);
            rbFemale.setChecked(false);
        } else if ("女".equals(sex)) {
            rbMale.setChecked(false);
            rbFemale.setChecked(true);
        }
    }

    public String getSex() {
        if (rbMale.isChecked()) {
            return "男";
        } else {
            return "女";
        }
    }
}
