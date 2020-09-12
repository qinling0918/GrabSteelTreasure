package com.zgw.qgb.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.zgw.qgb.helper.utils.EmptyUtils;


/**
 * Created by qinling on 2020/6/11 11:53
 * Description:
 */
public class EditViewHelper {
   /* public static void setOnClickListener(View.OnClickListener listener,View... views) {
        if (null == views || views.length == 0) {
            return;
        }
        for (View view : views) {
            view.setOnClickListener(listener);
        }
    }*/


    public interface OnEditActionSearchListener {
        void oSearch(TextView v);
    }

    private interface OnEditActionListener {
        void onClick(TextView v);

        int action();
    }

    private static abstract class OnEditSearchAction implements OnEditActionListener {
        @Override
        public int action() {
            return EditorInfo.IME_ACTION_SEARCH;
        }
    }

    private static void setOnEditActionListener(EditText editText, final OnEditActionListener listener) {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (null == listener) {
                    return false;
                }
                if (actionId == listener.action()) {
                    listener.onClick(v);
                    return true;
                }
                return false;
            }
        });
    }

    public static void setOnEditActionSearchListener(final EditText editText, final OnEditActionSearchListener action) {
        setOnEditActionListener(editText, new OnEditSearchAction() {
            @Override
            public void onClick(TextView v) {
                action.oSearch(v);
                hideSoftInput(editText);
            }
        });
    }

    private static boolean hideSoftInput(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService("input_method");
        return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public interface OnDrawableRightClickListener {
        void onClick(TextView view, Drawable drawable);
    }


    @SuppressLint("ClickableViewAccessibility")
    public static void setOnDrawableRightClickListener(TextView textView, final OnDrawableRightClickListener listener) {
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView tv = (TextView) v;
                Drawable mDrawable = tv.getCompoundDrawables()[2];
                if (mDrawable != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    boolean xTouchable = event.getX() > (v.getWidth() - tv.getPaddingRight() - mDrawable.getIntrinsicWidth())
                            && (event.getX() < (tv.getWidth() - tv.getPaddingRight()));
                    if (xTouchable) {
                        if (null != listener) {
                            listener.onClick(tv, mDrawable);
                        }
                        // 将事件处理完，不再向下分发。
                        return true;
                    }
                }
                return v.onTouchEvent(event);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setOnTouchListener(TextView textView, final OnDrawableRightClickListener clickListener, final View.OnTouchListener touchListener) {
        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView tv = (TextView) v;
                Drawable mDrawable = tv.getCompoundDrawables()[2];
                if (mDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
                    boolean xTouchable = event.getX() > (v.getWidth() - tv.getPaddingRight() - mDrawable.getIntrinsicWidth())
                            && (event.getX() < (tv.getWidth() - tv.getPaddingRight()));
                    if (xTouchable) {
                        if (null != clickListener) {
                            clickListener.onClick(tv, mDrawable);
                        }
                        // 将事件处理完，不再向下分发。
                        return true;
                    } else {
                        touchListener.onTouch(v, event);
                    }
                }

                return v.onTouchEvent(event);
            }
        });
    }

/*    @SuppressLint("ClickableViewAccessibility")
    public static void wrapClearEditText(final EditText editText) {
        setOnDrawableRightClickListener(editText, new OnDrawableRightClickListener() {
            @Override
            public void onClick(TextView view, Drawable drawable) {
                view.setText("");
            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });
    }*/


    @SuppressLint("ClickableViewAccessibility")
    public static void showDropDownWhenAutoTouched(AutoCompleteTextView... autoCompleteTextViews) {
        if (EmptyUtils.isEmpty(autoCompleteTextViews)) {
            return;
        }

        for (final AutoCompleteTextView autoCompleteTextView : autoCompleteTextViews) {
            autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    autoCompleteTextView.showDropDown();
                    return false;
                }
            });
          /*  setOnTouchListener(autoCompleteTextView, new OnDrawableRightClickListener() {
               @Override
               public void onClick(TextView view, Drawable drawable) {

               }
           }, new View.OnTouchListener() {
               @Override
               public boolean onTouch(View v, MotionEvent event) {
                   return false;
               }
           });*/

       /*     autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus){
                    autoCompleteTextView.showDropDown();
                }
            });*/
        }

    }

    public interface OnTextNotMatchedListener {
        void onNotMatched(CharSequence result);
    }


    public static final String REGEX_ASSET_NUMBER = "\\d{12}|\\d{22}";

    public static boolean assetNumberMatched(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        String assetNo = text.toString();
        return assetNo.matches(REGEX_ASSET_NUMBER);
    }



}
