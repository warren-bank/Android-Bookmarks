package com.github.warren_bank.bookmarks.ui.listeners;

import com.github.warren_bank.bookmarks.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class IntentExtraValueTokenSeparatorOnClickListener implements View.OnClickListener {
  private EditText editText;
  private String   token;
  private Drawable tokenImage;

  public IntentExtraValueTokenSeparatorOnClickListener(Context context, final EditText editText, final String token) {
    context = context.getApplicationContext();
    Resources resources = context.getResources();

    this.editText   = editText;
    this.token      = token;
    this.tokenImage = resources.getDrawable(R.drawable.icon_intent_extra_value_separator);

    int icon_width  = resources.getInteger(R.integer.icon_intent_extra_value_separator_width);
    int icon_height = resources.getInteger(R.integer.icon_intent_extra_value_separator_height);
    this.tokenImage.setBounds(0, 0, icon_width, icon_height);

    this.editText.addTextChangedListener(new IntentExtraValueTextChangedListener());
  }

  @Override
  public void onClick(View v) {
    formatTokens(/* insertNewToken */ true, /* retainCursorPosition */ true);
  }

  public void formatTokens(boolean insertNewToken, boolean retainCursorPosition) {
    int offset          = editText.getSelectionEnd();
    String unstyledText = editText.getText().toString();

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

  protected SpannableString getSpannableString(String unstyledText) {
    SpannableString styledText = new SpannableString(unstyledText);

    int start_index = 0, end_index;
    while ((start_index = unstyledText.indexOf(token, start_index)) >= 0) {
      end_index = start_index + token.length();
      styledText.setSpan(getImageSpan(), start_index, end_index, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
      start_index = end_index;
    }

    return styledText;
  }

  private ImageSpan getImageSpan() {
    ImageSpan tokenImageSpan = (Build.VERSION.SDK_INT >= 3)
      ? new ImageSpan(tokenImage, ImageSpan.ALIGN_BASELINE)
      : new ImageSpan(tokenImage);

    return tokenImageSpan;
  }

  private class IntentExtraValueTextChangedListener implements TextWatcher {
    private List<Integer> spanStartIndexPositions;

    public IntentExtraValueTextChangedListener() {
      spanStartIndexPositions = new ArrayList<Integer>();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int changeStartIndex, int before, int after) {
      boolean isDelete = (after < before);
      if (!isDelete) return;

      String originalUnstyledText = s.toString();

      int start_index = 0;
      spanStartIndexPositions.clear();
      while ((start_index = originalUnstyledText.indexOf(token, start_index)) >= 0) {
        spanStartIndexPositions.add(start_index);
        start_index += token.length();
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /*
     * -------------------------------------------------------------------------
     * assumptions:
     *  - the user cannot position the cursor within a span in such a way that:
     *    * text can be inserted into a span
     *        ..to invalidate a token
     *    * text can be deleted from within a span
     *        ..that does not modify either of its two ends (head or tail)
     * - the user can ONLY modify a span
     *   by deleting characters from either end (head or tail) of the token string
     * -------------------------------------------------------------------------
     */
    @Override
    public void onTextChanged(CharSequence s, int changeStartIndex, int before, int after) {
      boolean isDelete = (after < before);
      if (!isDelete) return;

      for (int spanStartIndex : spanStartIndexPositions) {
        if ((changeStartIndex >= spanStartIndex) && (changeStartIndex < (spanStartIndex + token.length()))) {
          // delete occured within span
          String changedUnstyledText = s.toString();
          String cleanedUnstyledText = getSanitizedExtraValue(changedUnstyledText, spanStartIndex, changeStartIndex, before, after);

          editText.setText(
            getSpannableString(cleanedUnstyledText),
            TextView.BufferType.SPANNABLE
          );
          editText.setSelection(spanStartIndex);
          break;
        }
      }
    }

    private String getSanitizedExtraValue(String changedUnstyledText, int spanStartIndex, int changeStartIndex, int before, int after) {
      String cleanedUnstyledText = "";
      int tail = changeStartIndex;

      if (spanStartIndex > 0)
        cleanedUnstyledText += changedUnstyledText.substring(0, spanStartIndex);

      if (spanStartIndex == tail) {
        // deleting characters from "head" of token string
        int offset = token.length() - (before - after);
        if (offset > 0)
          tail += offset;
      }

      cleanedUnstyledText += changedUnstyledText.substring(tail);

      return cleanedUnstyledText;
    }
  }
};
