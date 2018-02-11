package com.android.szparag.animateddropletwidgetshowcase

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import kotlin.math.min

class RoundImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
  ImageButton(context, attrs, defStyleAttr) {

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    clipToOutline = false
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    setMeasuredDimension(
      min(
        View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
        View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
      )
    )
  }

  private fun setMeasuredDimension(commonDimension: Int) =
    setMeasuredDimension(commonDimension, commonDimension)

}