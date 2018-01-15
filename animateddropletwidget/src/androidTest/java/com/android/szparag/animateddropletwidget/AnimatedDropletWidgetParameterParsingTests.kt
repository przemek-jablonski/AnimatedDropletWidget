package com.android.szparag.animateddropletwidget

import org.junit.Test

@Suppress("IllegalIdentifier")
class AnimatedDropletWidgetParameterParsingTests {


  //<editor-fold desc="drawableSrc, drawableSize, drawableAlpha">
  @Test fun `Not setting drawableSrc in xml should fallback to default resource used`() {}
  @Test fun `Setting drawableSrc to transparent should not create front drawable view at all`() {}
  @Test fun `Setting drawableSize to 0 should not create front drawable view at all`() {}
  @Test fun `Setting drawableAlpha to 0 should not create front drawable view at all`() {}
  @Test fun `Setting custom drawableSrc should apply given resource accordingly`() {}
  @Test fun `Setting drawable alpha to non-0 value should apply parameters accordingly`() {}
  @Test fun `Setting drawable size to non-0 value should apply parameters accordingly`() {}
  @Test fun `Setting drawable alpha and size to non-0 value should apply parameters accordingly`() {}
  @Test fun `Setting drawable alpha parameter with transparent src should not crash nor create front drawable view`() {}
  @Test fun `Setting drawable size parameter with transparent src should not crash nor create front drawable view`() {}
  @Test fun `Setting drawable alpha parameter with non-transparent src should not crash nor create front drawable view`() {}
  @Test fun `Setting drawable size parameter with non-transparent src should not crash nor create front drawable view`() {}
  //</editor-fold>


}