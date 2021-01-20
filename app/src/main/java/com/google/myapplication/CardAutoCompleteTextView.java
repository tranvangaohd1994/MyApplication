package com.google.myapplication;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;

public class CardAutoCompleteTextView extends AutoCompleteTextView {
    public CardAutoCompleteTextView(Context context) {
        super(context);
    }

    public CardAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && getAdapter() != null) {
            performFiltering(getText(), 0);
            showDropDown();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.showDropDown();
        return super.onTouchEvent(event);
    }


}
