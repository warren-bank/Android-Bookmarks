package com.github.warren_bank.bookmarks.ui.widgets;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.database.DbGateway;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;
import com.github.warren_bank.bookmarks.ui.widgets.RowLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class FolderBreadcrumbsLayout extends RowLayout {

  public FolderBreadcrumbsLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public void populate(int folderId, int breadcrumbTextViewResId, int separatorTextViewResId) {
    DbGateway db = DbGateway.getInstance(getContext());
    List<FolderContentItem> breadcrumbs = db.getFolderBreadcrumbs(folderId);
    FolderContentItem crumb;
    TextView textview;

    // remove all previous breadcrumbs
    removeAllViews();

    for (int i=0; i < breadcrumbs.size(); i++) {
      crumb = breadcrumbs.get(i);

      if (i > 0) {
        // add separator
        textview = (TextView) View.inflate(getContext(), separatorTextViewResId, null);
        addView(textview);
      }

      textview = (TextView) View.inflate(getContext(), breadcrumbTextViewResId, null);
      textview.setText(crumb.name);
      textview.setTag(crumb);
      addView(textview);
    }
  }
}
