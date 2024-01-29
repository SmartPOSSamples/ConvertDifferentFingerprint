package com.cloudpos.convertdifffinger.utils;

import android.util.Log;

/**
 * Created by zxd on 17-7-18.<br/>
 */

public class Logger {

	public static int level = Log.DEBUG;

	public static boolean isDebug() {
		return level <= Log.DEBUG;
	}

	public static void setLevel(int level) {
		Logger.level = level;
	}

	public static void debug(String msg) {

		if (level <= Log.DEBUG) {
			Log.d(createTag(), msg);
		}
	}

	public static void debug(String msg, Throwable tr) {

		if (level <= Log.DEBUG) {
			Log.d(createTag(), msg, tr);
		}
	}

	public static void debug(String msg, Object... args) {

		if (level <= Log.DEBUG) {
			Log.d(createTag(), String.format(msg, args));
		}
	}

	public static void info(String msg) {

		if (level <= Log.INFO) {
			Log.i(createTag(), msg);
		}
	}

	public static void info(String msg, Throwable tr) {

		if (level <= Log.INFO) {
			Log.i(createTag(), msg, tr);
		}
	}

	public static void info(String msg, Object... args) {

		if (level <= Log.INFO) {
			Log.i(createTag(), String.format(msg, args));
		}
	}

	public static void warn(String msg) {

		if (level <= Log.WARN) {
			Log.w(createTag(), msg);
		}
	}

	public static void warn(String msg, Throwable tr) {

		if (level <= Log.WARN) {
			Log.w(createTag(), msg, tr);
		}
	}

	public static void warn(String msg, Object... args) {

		if (level <= Log.WARN) {
			Log.w(createTag(), String.format(msg, args));
		}
	}

	public static void error(String msg) {

		if (level <= Log.ERROR) {
			Log.e(createTag(), msg);
		}
	}

	public static void error(String msg, Throwable tr) {

		if (level <= Log.ERROR) {
			Log.e(createTag(), msg, tr);
		}
	}

	public static void exception(Throwable tr) {
		if (level <= Log.ERROR) {
			Log.e(createTag(), tr.getClass().getSimpleName(), tr);
		}
	}

	public static void exception(String msg, Throwable tr) {
		if (level <= Log.ERROR) {
			Log.e(createTag(), msg);
			Log.w(createTag(), tr.getClass().getSimpleName(), tr);
		}
	}

	public static void error(String msg, Object... args) {

		if (level <= Log.ERROR) {
			Log.e(createTag(), String.format(msg, args));
		}
	}

	private static String createTag() {

		StackTraceElement[] sts = Thread.currentThread().getStackTrace();
		if (sts == null) {
			return null;
		}
		for (StackTraceElement st : sts) {
			if (st.isNativeMethod()) {
				continue;
			}
			if (st.getClassName().equals(Thread.class.getName())) {
				continue;
			}
			if (st.getClassName().equals(Logger.class.getName())) {
				continue;
			}
			return st.getLineNumber() + ":" + st.getFileName();
		}
		return "";
	}
}
