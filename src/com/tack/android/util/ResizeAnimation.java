package com.tack.android.util;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * an animation for resizing the view.
 */
public class ResizeAnimation extends Animation {
  private View mView;
  private float mToHeight;
  private float mFromHeight;
  private float mToWidth;
  private float mFromWidth;

  public ResizeAnimation(View v, float fromWidth, float fromHeight, float toWidth, float toHeight, float density) {
    mToHeight = toHeight*density;
    mToWidth = toWidth*density;
    mFromHeight = fromHeight*density;
    mFromWidth = fromWidth*density;
    mView = v;
    setDuration(300);
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    float height = (mToHeight - mFromHeight) * interpolatedTime + mFromHeight;
    float width = (mToWidth - mFromWidth) * interpolatedTime + mFromWidth;
    ViewGroup.LayoutParams p = mView.getLayoutParams();
    p.height = (int) height;
    p.width = (int) width;
    mView.requestLayout();
  }

}
