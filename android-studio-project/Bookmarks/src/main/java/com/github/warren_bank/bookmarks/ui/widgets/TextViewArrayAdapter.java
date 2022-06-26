package com.github.warren_bank.bookmarks.ui.widgets;

import com.github.warren_bank.bookmarks.R;

// -----------------------------------------------------------------------------
// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/ArrayAdapter.java
// https://stackoverflow.com/a/4533488
// -----------------------------------------------------------------------------

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class TextViewArrayAdapter<T> extends ArrayAdapter<T> {
  private static final int UNDEFINED_TEXT_ATTRIBUTE = -1;
  private ColorStateList mTextColorStateList;
  private int mTextColor, mTextAppearance;

  public TextViewArrayAdapter(Context context, int resource) {
    super(context, resource);
    resetTextAttributes();
  }

  public TextViewArrayAdapter(Context context, int resource, int textViewResourceId) {
    super(context, resource, textViewResourceId);
    resetTextAttributes();
  }

  public TextViewArrayAdapter(Context context, int resource, T[] objects) {
    super(context, resource, objects);
    resetTextAttributes();
  }

  public TextViewArrayAdapter(Context context, int resource, int textViewResourceId, T[] objects) {
    super(context, resource, textViewResourceId, objects);
    resetTextAttributes();
  }

  public TextViewArrayAdapter(Context context, int resource, List<T> objects) {
    super(context, resource, objects);
    resetTextAttributes();
  }

  public TextViewArrayAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
    super(context, resource, textViewResourceId, objects);
    resetTextAttributes();
  }

  private void resetTextAttributes() {
    mTextColorStateList = null;
    mTextColor          = UNDEFINED_TEXT_ATTRIBUTE;
    mTextAppearance     = UNDEFINED_TEXT_ATTRIBUTE;
  }

  public void setTextColorStateList(ColorStateList textColorStateList) {
    mTextColorStateList = textColorStateList;
  }

  public void setTextColor(int textColorResId) {
    try {
      mTextColor = getContext().getResources().getInteger(textColorResId);
    }
    catch(Exception e) {
      mTextColor = UNDEFINED_TEXT_ATTRIBUTE;
    }
  }

  public void setTextAppearance(int textAppearance) {
    mTextAppearance = textAppearance;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View     view     = super.getView(position, convertView, parent);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);

    if (textView != null) {
      if (mTextColorStateList != null)
        textView.setTextColor(mTextColorStateList);
      else if (mTextColor != UNDEFINED_TEXT_ATTRIBUTE)
        textView.setTextColor(mTextColor);

      if (mTextAppearance != UNDEFINED_TEXT_ATTRIBUTE)
        textView.setTextAppearance(getContext(), mTextAppearance);
    }

    if (mTextColor != UNDEFINED_TEXT_ATTRIBUTE) {
      textView.setTextColor(mTextColor);
    }

    return view;
  }
}
