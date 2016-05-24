package com.byteera.bank.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.byteera.R;


/** Created by bing on 2015/4/27. */
public class HeadViewMain extends FrameLayout implements View.OnClickListener {
    private Context mContext;
    private ImageView imgBack;
    private ImageView imgRight;
    private TextView tvTitleName;
    private TextView tvRight;
    private ImageView imgPullDownView;
    private TextView tvLeft;


    public interface LeftImgClickListner {
        void onLeftImgClick();
    }

    public interface RightImgClickListner {
        void onRightImgClick();
    }

    public interface RightTextClickListener {
        void onRightTextClick();
    }

    public interface LeftTextClickListener {
        void onLeftTextClick();
    }


    private LeftImgClickListner backClickListner;
    private RightImgClickListner addClickListner;
    private RightTextClickListener listener;
    private LeftTextClickListener leftTextlistener;

    public HeadViewMain(Context context) {
        this(context, null);
    }

    public HeadViewMain(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeadViewMain(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.HeadViewMain);
        boolean imgPullDown = attributes.getBoolean(R.styleable.HeadViewMain_imgPullDown, false);
        Drawable leftImg = attributes.getDrawable(R.styleable.HeadViewMain_leftImg);
        Drawable rightImg = attributes.getDrawable(R.styleable.HeadViewMain_rightImg);
        String rightText = attributes.getString(R.styleable.HeadViewMain_rightText);
        String leftText = attributes.getString(R.styleable.HeadViewMain_leftText);
        String titleName = attributes.getString(R.styleable.HeadViewMain_titleName);

        LayoutInflater.from(mContext).inflate(R.layout.head_view_main, this, true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.setPadding(0, UIUtils.dip2px(getContext(), 23), 0, 0);
//        }
        imgBack = (ImageView) findViewById(R.id.img_back);
        if (leftImg != null) {
            imgBack.setImageDrawable(leftImg);
        } else {
            imgBack.setVisibility(View.GONE);
        }
        tvTitleName = (TextView) findViewById(R.id.tv_titlename);
        tvTitleName.setText(titleName);
        imgRight = (ImageView) findViewById(R.id.img_update_bt);
        if (rightImg != null) {
            imgRight.setImageDrawable(rightImg);
        } else {
            imgRight.setVisibility(View.INVISIBLE);
        }
        tvRight = (TextView) findViewById(R.id.tv_right);
        tvLeft = (TextView) findViewById(R.id.tv_left);
        if (rightText != null) {
            tvRight.setText(rightText);
        } else {
            tvRight.setVisibility(View.GONE);
        }
        if (leftText != null) {
            tvLeft.setText(leftText);
        } else {
            tvLeft.setVisibility(View.GONE);
        }
        imgPullDownView = (ImageView) findViewById(R.id.img_pull_down);
        imgPullDownView.setVisibility(imgPullDown ? View.VISIBLE : View.GONE);
        attributes.recycle();
    }

    public void setLeftImgClickListener(LeftImgClickListner backClickListner) {
        this.backClickListner = backClickListner;
        imgBack.setOnClickListener(this);
    }

    public void setRightImgClickListener(RightImgClickListner backClickListner) {
        this.addClickListner = backClickListner;
        imgRight.setOnClickListener(this);
    }

    public void setRightTextClickListener(RightTextClickListener backClickListner) {
        this.listener = backClickListner;
        tvRight.setOnClickListener(this);
    }

    public void setLeftTextClickListener(LeftTextClickListener leftTextListener) {
        this.leftTextlistener = leftTextListener;
        tvLeft.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                backClickListner.onLeftImgClick();
                break;
            case R.id.img_update_bt:
                addClickListner.onRightImgClick();
                break;
            case R.id.tv_right:
                listener.onRightTextClick();
                break;
            case R.id.tv_left:
                leftTextlistener.onLeftTextClick();
                break;
        }
    }

    public void setTitleName(String text) {
        tvTitleName.setText(text);
    }

    public void setTitleName(int text) {
        tvTitleName.setText(text);
    }

    public String getTitleName() {
        return tvTitleName.getText().toString();
    }

    public TextView getRightTextView() {
        return tvRight;
    }

    public ImageView getPullDownImg() {
        return imgPullDownView;
    }
}
