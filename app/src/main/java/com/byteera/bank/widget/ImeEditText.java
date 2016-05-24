package com.byteera.bank.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/** Created by bing on 2015/6/26. */
public class ImeEditText extends EditText {
    public interface OnImeHideListener {
        void imeHide();
    }

    private OnImeHideListener onImeHideListener;

    public void setOnImeHideListener(OnImeHideListener listener) {
        this.onImeHideListener = listener;
    }

    public ImeEditText(Context context) {
        this(context, null);
    }

    public ImeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (onImeHideListener != null) {
                onImeHideListener.imeHide();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
