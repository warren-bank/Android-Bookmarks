package com.github.warren_bank.bookmarks.ui.widgets;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.common.DateFormats;
import com.github.warren_bank.bookmarks.ui.model.FolderContentItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FolderContentsAdapter extends ArrayAdapter<FolderContentItem> {
  private class ViewHolder {
    ImageView icon;
    TextView  name;
  }

  private List<FolderContentItem> items;
  private LayoutInflater inflater;
  private boolean showHidden;

  public FolderContentsAdapter(Context context, List<FolderContentItem> items, boolean showHidden) {
    super(context, R.layout.folder_content_item, items);

    this.items      = items;
    this.inflater   = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.showHidden = showHidden;
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public FolderContentItem getItem(int position) {
    return ((position < 0) || (position >= items.size()))
      ? null
      : items.get(position);
  }

  public void setItem(int position, FolderContentItem item) {
    items.set(position, item);
    notifyDataSetChanged();
  }

  public void setShowHidden(boolean showHidden) {
    this.showHidden = showHidden;
  }

  public View getView(final int position, View convertView, ViewGroup parent) {
    FolderContentItem item = items.get(position);
    ViewHolder holder = null;

    if (item.isFolder && item.isHidden && !showHidden) {
      if ((convertView == null) || convertView.isEnabled()) {
        convertView = inflater.inflate(R.layout.folder_content_item_hidden, parent, false);
        convertView.setEnabled(false);
      }
      return convertView;
    }

    if ((convertView == null) || !convertView.isEnabled()) {
      convertView = inflater.inflate(R.layout.folder_content_item, parent, false);
      holder      = new ViewHolder();
      holder.icon = (ImageView) convertView.findViewById(R.id.folder_content_item_icon);
      holder.name = (TextView)  convertView.findViewById(R.id.folder_content_item_name);

      convertView.setEnabled(true);
      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (holder != null) {
      if (holder.icon != null) {
        holder.icon.setImageResource(item.isFolder ? R.drawable.icon_folder : R.drawable.icon_bookmark);
        holder.icon.setVisibility(View.VISIBLE);
      }
      if (holder.name != null) {
        String name = item.name;
        if (item.lastModified > 0)
          name += " (" + DateFormats.getFileContentDateTime(item.lastModified) + ")";

        holder.name.setText(item.name);
        holder.name.setVisibility(View.VISIBLE);
      }
    }

    return convertView;
  }
}
