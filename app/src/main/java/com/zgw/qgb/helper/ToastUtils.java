package com.zgw.qgb.helper;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zgw.qgb.R;

import java.lang.ref.WeakReference;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2016/09/29
 *     desc  : 吐司相关工具类
 * </pre>
 */
public final class ToastUtils {

	private static final int COLOR_DEFAULT = 0xFEFFFFFF;
	private static final Handler HANDLER   = new Handler(Looper.getMainLooper());

	private static Toast               sToast;
	private static WeakReference<View> sViewWeakReference;
	private static int sLayoutId  = -1;
	private static int gravity    = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
	private static int xOffset    = 0;
	private static int yOffset    = (int) (64 * getContext().getResources().getDisplayMetrics().density + 0.5);
	private static int bgColor    = COLOR_DEFAULT;
	private static int bgResource = -1;
	private static int msgColor   = COLOR_DEFAULT;

	private ToastUtils() {
		throw new UnsupportedOperationException("u can't instantiate me...");
	}

	public static void init() {

	}

	/**
	 * 设置吐司位置
	 *
	 * @param gravity 位置
	 * @param xOffset x偏移
	 * @param yOffset y偏移
	 */
	public static void setGravity(final int gravity, final int xOffset, final int yOffset) {
		ToastUtils.gravity = gravity;
		ToastUtils.xOffset = xOffset;
		ToastUtils.yOffset = yOffset;
	}

	/**
	 * 设置背景颜色
	 *
	 * @param backgroundColor 背景色
	 */
	public static void setBgColor(@ColorInt final int backgroundColor) {
		ToastUtils.bgColor = backgroundColor;
	}

	/**
	 * 设置背景资源
	 *
	 * @param bgResource 背景资源
	 */
	public static void setBgResource(@DrawableRes final int bgResource) {
		ToastUtils.bgResource = bgResource;
	}

	/**
	 * 设置消息颜色
	 *
	 * @param msgColor 颜色
	 */
	public static void setMsgColor(@ColorInt final int msgColor) {
		ToastUtils.msgColor = msgColor;
	}

	/**
	 * 安全地显示短时吐司
	 *
	 * @param text 文本
	 */
	public static void showShort(@NonNull final CharSequence text) {
		show(text, Toast.LENGTH_SHORT);
	}

	/**
	 * 安全地显示短时吐司
	 *
	 * @param resId 资源Id
	 */
	public static void showShort(@StringRes final int resId) {
		show(resId, Toast.LENGTH_SHORT);
	}

	/**
	 * 安全地显示短时吐司
	 *
	 * @param resId 资源Id
	 * @param args  参数
	 */
	public static void showShort(@StringRes final int resId, final Object... args) {
		show(resId, Toast.LENGTH_SHORT, args);
	}

	/**
	 * 安全地显示短时吐司
	 *
	 * @param format 格式
	 * @param args   参数
	 */
	public static void showShort(final String format, final Object... args) {
		show(format, Toast.LENGTH_SHORT, args);
	}

	/**
	 * 安全地显示长时吐司
	 *
	 * @param text 文本
	 */
	public static void showLong(@NonNull final CharSequence text) {
		show(text, Toast.LENGTH_LONG);
	}

	/**
	 * 安全地显示长时吐司
	 *
	 * @param resId 资源Id
	 */
	public static void showLong(@StringRes final int resId) {
		show(resId, Toast.LENGTH_LONG);
	}

	/**
	 * 安全地显示长时吐司
	 *
	 * @param resId 资源Id
	 * @param args  参数
	 */
	public static void showLong(@StringRes final int resId, final Object... args) {
		show(resId, Toast.LENGTH_LONG, args);
	}

	/**
	 * 安全地显示长时吐司
	 *
	 * @param format 格式
	 * @param args   参数
	 */
	public static void showLong(final String format, final Object... args) {
		show(format, Toast.LENGTH_LONG, args);
	}

	/**
	 * 安全地显示短时自定义吐司
	 */
	public static View showCustomShort(@LayoutRes final int layoutId) {
		final View view = getView(layoutId);
		show(view, Toast.LENGTH_SHORT);
		return view;
	}

	public static View showCustomShort(View view) {
		show(view, Toast.LENGTH_SHORT);
		return view;
	}
	public static View showCustomLong(View view) {
		show(view, Toast.LENGTH_LONG);
		return view;
	}

	/**
	 * 安全地显示长时自定义吐司
	 */
	public static View showCustomLong(@LayoutRes final int layoutId) {
		final View view = getView(layoutId);
		show(view, Toast.LENGTH_LONG);
		return view;
	}

	/**
	 * 取消吐司显示
	 */
	public static void cancel() {
		if (sToast != null) {
			sToast.cancel();
			sToast = null;
		}
	}

	private static void show(@StringRes final int resId, final int duration) {
		show(getContext().getResources().getText(resId).toString(), duration);
	}

	private static Context getContext() {
		return Utils.getContext();
	}

	private static void show(@StringRes final int resId, final int duration, final Object... args) {
		show(String.format(getContext().getResources().getString(resId), args), duration);
	}

	private static void show(final String format, final int duration, final Object... args) {
		show(String.format(format, args), duration);
	}

	private static void show(final CharSequence text, final int duration) {
		HANDLER.post(new Runnable() {
			@Override
			public void run() {
				cancel();
				sToast = Toast.makeText(getContext(), text, duration);

				// solve the font of toast
				TextView tvMessage = (TextView) sToast.getView().findViewById(android.R.id.message);
				tvMessage.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
				tvMessage.setTextColor(msgColor);
				setBgAndGravity();
				sToast.show();
			}
		});
	}

	private static void show(final View view, final int duration) {
		HANDLER.post(new Runnable() {
			@Override
			public void run() {
				cancel();
				sToast = new Toast(getContext());
				sToast.setView(view);
				sToast.setDuration(duration);
				setBgAndGravity();
				sToast.show();
			}
		});
	}

	private static void setBgAndGravity() {
		View toastView = sToast.getView();

		if (bgResource != -1) {
			toastView.setBackgroundResource(bgResource);
		} else if (bgColor != COLOR_DEFAULT) {
			Drawable background = toastView.getBackground();
			if (background != null)
			background.setColorFilter(new PorterDuffColorFilter(bgColor, PorterDuff.Mode.SRC_IN));
		}
		sToast.setGravity(gravity, xOffset, yOffset);
	}

	private static View getView(@LayoutRes final int layoutId) {
		if (sLayoutId == layoutId) {
			if (sViewWeakReference != null) {
				final View toastView = sViewWeakReference.get();
				if (toastView != null) {
					return toastView;
				}
			}
		}
		LayoutInflater inflate = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View toastView = inflate != null ? inflate.inflate(layoutId, null) : null;
		if (null == toastView)  throw new NullPointerException("This toastView can not be null");
		sViewWeakReference = new WeakReference<>(toastView);
		sLayoutId = layoutId;

		return toastView;
	}

	public static void showError(CharSequence msgRes) {
		setBgColor(getContext().getResources().getColor(R.color.colorPrimary));
		setMsgColor(getContext().getResources().getColor(android.R.color.white));
		showLong(msgRes);
	}

	public static void showNormal(CharSequence msgRes) {
		setBgColor(getContext().getResources().getColor(R.color.colorAccent));
		setMsgColor(getContext().getResources().getColor(android.R.color.white));
		showLong(msgRes);
	}
}
