package com.github.warren_bank.bookmarks.ui.widgets;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.ui.model.AlarmContentItem;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AlarmContentsAdapter extends ArrayAdapter<AlarmContentItem> {

  private class ViewHolder {
    TextView name;
    TextView time;
    TextView freq;
  }

  private List<AlarmContentItem> items;
  private LayoutInflater inflater;

  private String labelTime;
  private String labelFreq;

  public AlarmContentsAdapter(Context context, List<AlarmContentItem> items) {
    super(context, R.layout.alarm_content_item, items);

    this.items     = items;
    this.inflater  = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    this.labelTime = context.getString(R.string.alarm_attribute_time);
    this.labelFreq = context.getString(R.string.alarm_attribute_interval);
  }

  @Override
  public int getCount() {
    return items.size();
  }

  @Override
  public AlarmContentItem getItem(int position) {
    return ((position < 0) || (position >= items.size()))
      ? null
      : items.get(position);
  }

  public void setItem(int position, AlarmContentItem item) {
    items.set(position, item);
    notifyDataSetChanged();
  }

  public View getView(final int position, View convertView, ViewGroup parent) {
    AlarmContentItem item = items.get(position);
    ViewHolder holder = null;

    if ((convertView == null) || !convertView.isEnabled()) {
      convertView = inflater.inflate(R.layout.alarm_content_item, parent, false);
      holder      = new ViewHolder();
      holder.name = (TextView)  convertView.findViewById(R.id.alarm_content_item_name);
      holder.time = (TextView)  convertView.findViewById(R.id.alarm_content_item_time);
      holder.freq = (TextView)  convertView.findViewById(R.id.alarm_content_item_freq);

      convertView.setEnabled(true);
      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (holder != null) {
      if (holder.name != null) {
        holder.name.setText(item.toString());
        holder.name.setVisibility(View.VISIBLE);
      }
      if (holder.time != null) {
        holder.time.setText(labelTime + " " + item.time);
        holder.time.setVisibility(View.VISIBLE);
      }
      if (holder.name != null) {
        if (!TextUtils.isEmpty(item.freq)) {
          holder.freq.setText(labelFreq + " " + item.freq);
          holder.freq.setVisibility(View.VISIBLE);
        }
        else {
          holder.freq.setText("");
          holder.freq.setVisibility(View.GONE);
        }
      }
    }

    return convertView;
  }
}
