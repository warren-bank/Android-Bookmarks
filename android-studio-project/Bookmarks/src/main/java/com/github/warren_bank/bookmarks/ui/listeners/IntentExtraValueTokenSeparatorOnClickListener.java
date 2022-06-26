package com.github.warren_bank.bookmarks.ui.listeners;

import com.github.warren_bank.bookmarks.R;
import com.github.warren_bank.bookmarks.database.model.DbIntent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class IntentExtraValueTokenSeparatorOnClickListener implements View.OnClickListener {
  private EditText editText;
  private Drawable tokenImage;

  public IntentExtraValueTokenSeparatorOnClickListener(Context context, EditText editText) {
    context = context.getApplicationContext();

    Resources resources   = context.getResources();
    int       icon_width  = resources.getInteger(R.integer.icon_intent_extra_value_separator_width);
    int       icon_height = resources.getInteger(R.integer.icon_intent_extra_value_separator_height);

    this.editText   = editText;
    this.tokenImage = resources.getDrawable(R.drawable.icon_intent_extra_value_separator);
    this.tokenImage.setBounds(0, 0, icon_width, icon_height);
  }

  private ImageSpan getImageSpan() {
    ImageSpan tokenImageSpan = (Build.VERSION.SDK_INT >= 3)
      ? new ImageSpan(tokenImage, ImageSpan.ALIGN_BASELINE)
      : new ImageSpan(tokenImage);

    return tokenImageSpan;
  }

  private SpannableString getSpannableString(String unstyledText) {
    SpannableString styledText = new SpannableString(unstyledText);

    String token = DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN;
    int start_index = 0, end_index;
    while ((start_index = unstyledText.indexOf(token, start_index)) >= 0) {
      end_index = start_index + token.length();
      styledText.setSpan(getImageSpan(), start_index, end_index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      start_index = end_index;
    }

    return styledText;
  }

  public void formatTokens(boolean insertNewToken, boolean retainCursorPosition) {
    int offset          = editText.getSelectionEnd();
    String unstyledText = editText.getText().toString();
    String token        = DbIntent.EXTRA_ARRAY_SEPARATOR_TOKEN;

    // default position of cursor is after last character of text
    if ((offset < 0) || (offset > unstyledText.length()))
      offset = unstyledText.length();

    if (insertNewToken) {
      if (offset == 0)
        unstyledText = token + unstyledText;
      else if (offset == unstyledText.length())
        unstyledText += token;
      else
        unstyledText = unstyledText.substring(0, offset) + token + unstyledText.substring(offset, unstyledText.length());

      offset += token.length();
    }

    editText.setText(
      getSpannableString(unstyledText),
      TextView.BufferType.SPANNABLE
    );

    if (retainCursorPosition) {
      // reposition the cursor after the inserted token
      editText.setSelection(offset);
    }
  }

  @Override
  public void onClick(View v) {
    formatTokens(/* insertNewToken */ true, /* retainCursorPosition */ true);
  }
};
