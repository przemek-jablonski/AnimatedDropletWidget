package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.content.res.TypedArray
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_screen_picker_layout.view.*

class ScreenPickerView: ConstraintLayout{
  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    LayoutInflater.from(context).inflate(R.layout.view_screen_picker_layout, this, true)
    context.theme.obtainStyledAttributes(attrs, R.styleable.ScreenPickerView, defStyleAttr, 0)
      ?.let { typedArray ->
        try {
          fetchAndApplyCustomAttributes(typedArray)
        } finally {
          typedArray.recycle()
        }
      }
  }

  private fun fetchAndApplyCustomAttributes(attrs: TypedArray) {
    backgroundImageView.setImageResource(attrs.getResourceId(R.styleable.ScreenPickerView_background, -1))
    headerTextView.text = attrs.getString(R.styleable.ScreenPickerView_header)
    contentTextView.text = attrs.getString(R.styleable.ScreenPickerView_content)
  }

}