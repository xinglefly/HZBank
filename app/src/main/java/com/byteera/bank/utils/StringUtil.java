package com.byteera.bank.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import com.byteera.bank.utils.LogUtil;

import com.byteera.bank.activity.business_circle.activity.util.UIUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Created by bing on 2015/4/22. */
public class StringUtil {

    public static String getPinYin(String str) {

        String convert = "";
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert += pinyinArray[0];
            } else {
                convert += word;
            }
        }
        convert = convert.toUpperCase();
        return convert;
    }

    public static String checkTime(long time) {
        Date date = new Date(time);
        Calendar now = Calendar.getInstance();
        int currentDayOfYear = now.get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        if (dayOfYear == currentDayOfYear) {
            SimpleDateFormat format = new SimpleDateFormat("今天 HH:mm", Locale.CHINA);
            return format.format(time);
        } else {
            SimpleDateFormat format = new SimpleDateFormat("MM-dd", Locale.CHINA);
            return format.format(time);
        }
    }


}
