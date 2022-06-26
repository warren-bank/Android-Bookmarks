package com.github.warren_bank.bookmarks.ui.widgets;

// -----------------------------------------------------------------------------
// https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/widget/ListView.java
// https://stackoverflow.com/a/37410925
// -----------------------------------------------------------------------------

import android.content.Context;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ListView;

public class ListViewInScrollView extends ListView {

  public ListViewInScrollView(Context context) {
    super(context);
  }

  public ListViewInScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ListViewInScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
      Integer.MAX_VALUE >> 2,
      MeasureSpec.AT_MOST
    );

    super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
    ViewGroup.LayoutParams params = getLayoutParams();
    params.height = getMeasuredHeight();
  }
}
