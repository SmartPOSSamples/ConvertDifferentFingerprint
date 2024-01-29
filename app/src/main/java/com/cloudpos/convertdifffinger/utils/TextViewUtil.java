package com.cloudpos.convertdifffinger.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class TextViewUtil {
	
	
	public static void infoColorfulTextView(TextView log_text, String msg , int textColor){
		int start = 0;
		if (log_text.getText().length() == 0) {
		} else {
			start = log_text.getText().length();
		}
		log_text.append(msg);
		Spannable style = (Spannable) log_text.getText();
		int end = start + msg.length();
		ForegroundColorSpan color;
		color = new ForegroundColorSpan(textColor);
		style.setSpan(color, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		moveScroller(log_text);

	}
	private static void moveScroller(TextView text) {
		if (text.getLineCount() > 600) {
			text.setText("\n");
		}
		// find the amount we need to scroll. This works by
		// asking the TextView's internal layout for the position
		// of the final line and then subtracting the TextView's height
		final int scrollAmount = text.getLayout().getLineTop(text.getLineCount()) - text.getHeight();
		// if there is no need to scroll, scrollAmount will be <=0
		if (scrollAmount > 0) {
			text.scrollTo(0, scrollAmount + 30);
		} else {
			text.scrollTo(0, 0);
		}
	}
	
	public static void infoRedTextView(TextView log_text, String msg ){
		infoColorfulTextView(log_text, msg, Color.RED);
	}

	public static void infoBlueTextView(TextView log_text, String msg ){
		infoColorfulTextView(log_text, msg, Color.BLUE);
	}

	public static void infoMAGENTATextView(TextView log_text, String msg ){
		infoColorfulTextView(log_text, msg, Color.MAGENTA);
	}

	public static void infoGRAYTextView(TextView log_text, String msg ){
		infoColorfulTextView(log_text, msg, Color.GRAY);
	}
}
