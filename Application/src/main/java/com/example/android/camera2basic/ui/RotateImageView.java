/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2basic.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * A @{code ImageView} which can rotate it's content.
 */
public class RotateImageView extends TwoStateImageView implements Rotatable {
    private static final String TAG = "RotateImageView";

    private static final int ANIMATION_SPEED = 270; // 270 deg/sec

    private int mCurrentDegree = 0; // [0, 359]
    private int mStartDegree = 0;
    private int mTargetDegree = 0;

    private boolean mClockwise = false;
    private boolean mEnableAnimation = true;

    private long mAnimationStartTime = 0;
    private long mAnimationEndTime = 0;

    private int flag = 0;
    private final int THUMBNAILVIEW = 4;
    Drawable cropDrawable;
    Paint painter;
    Bitmap cropBitmap;
    Bitmap imageResourceBitmap;
    Drawable imgDrawable;
    Drawable bgDrawable;
    private Animation anim = null;
    private boolean mNeedUpdateAlphaFlag = false;
    private boolean isPressed = false;
    private boolean mNeedUpdatePressedBg = false;
    Drawable pressedBgDrawable;
    private Paint mPaint = new Paint();
    private Bitmap mDrawableBitmap;

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
    }

    public RotateImageView(Context context) {
        super(context);
    }

    protected int getDegree() {
        return mTargetDegree;
    }


    public void cleanResource() {
        Drawable ui = getDrawable();
        if (ui instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) ui).getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
        }
    }


    @Override
    public void setOrientation(int degree, boolean animation) {
        Log.d(TAG, "setOrientation(" + degree + ", " + animation + ") mOrientation=" + mTargetDegree);
        mEnableAnimation = animation;
        // make sure in the range of [0, 359]
        degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
        if (degree == mTargetDegree) {
            return;
        }

        mTargetDegree = degree;
        if (mEnableAnimation) {
            mStartDegree = mCurrentDegree;
            mAnimationStartTime = AnimationUtils.currentAnimationTimeMillis();

            int diff = mTargetDegree - mCurrentDegree;
            diff = diff >= 0 ? diff : 360 + diff; // make it in range [0, 359]

            // Make it in range [-179, 180]. That's the shorted distance between the
            // two angles
            diff = diff > 180 ? diff - 360 : diff;

            mClockwise = diff >= 0;
            mAnimationEndTime = mAnimationStartTime
                    + Math.abs(diff) * 1000 / ANIMATION_SPEED;
        } else {
            mCurrentDegree = mTargetDegree;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            Log.e(TAG, "drawable == null, return");
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && mDrawableBitmap == null
                &&  drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
            mDrawableBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas bitmapCanvas = new Canvas(mDrawableBitmap);
            drawable.setBounds(0, 0, bitmapCanvas.getWidth(), bitmapCanvas.getHeight());
            drawable.draw(bitmapCanvas);
        }
        Rect bounds = drawable.getBounds();
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;

        if (w == 0 || h == 0) {
            Log.e(TAG, "w == 0 || h == 0, return");
            return; // nothing to draw
        }

        if (mCurrentDegree != mTargetDegree) {
            long time = AnimationUtils.currentAnimationTimeMillis();
            if (time < mAnimationEndTime) {
                int deltaTime = (int) (time - mAnimationStartTime);
                int degree = mStartDegree + ANIMATION_SPEED
                        * (mClockwise ? deltaTime : -deltaTime) / 1000;
                degree = degree >= 0 ? degree % 360 : degree % 360 + 360;
                mCurrentDegree = degree;
                invalidate();
            } else {
                mCurrentDegree = mTargetDegree;
                updateStateAfterAnimation();
            }
        }

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        int width = getWidth() - left - right;
        int height = getHeight() - top - bottom;

        int saveCount = canvas.getSaveCount();

        // Scale down the image first if required.
        if ((getScaleType() == ImageView.ScaleType.FIT_CENTER) &&
                ((width < w) || (height < h))) {
            float ratio = Math.min((float) width / w, (float) height / h);
            canvas.scale(ratio, ratio, width / 2.0f, height / 2.0f);
        }
        canvas.translate(left + width / 2, top + height / 2);
        canvas.rotate(-mCurrentDegree);
        canvas.translate(-w / 2, -h / 2);
        if (mDrawableBitmap == null) {
            drawable.draw(canvas);
        } else {
            canvas.drawBitmap(mDrawableBitmap, 0, 0, mPaint);
            if (mCurrentDegree == mTargetDegree) {
                mDrawableBitmap = null;
            }
        }
        canvas.restoreToCount(saveCount);
    }

    @Override
    protected void drawableStateChanged() {
        // TODO Auto-generated method stub
        super.drawableStateChanged();
        if (mNeedUpdateAlphaFlag) {
            if (isPressed != isPressed()) {
                isPressed = isPressed();
                float alpha = isPressed ? 0.5f : 1.0f;
                setAlpha(alpha);
            }
        }
        if (mNeedUpdatePressedBg) {
            if (isPressed != isPressed()) {
                isPressed = isPressed();
                if (isPressed) {
                    setBackground(pressedBgDrawable);
                } else {
                    setBackground(null);
                }
            }
        }
    }

    public void setUpdateAlphaFlag(boolean flag) {
        mNeedUpdateAlphaFlag = flag;
    }

    public void setImageViewPressBg(boolean flag, int pressedDrawableId) {
        mNeedUpdatePressedBg = flag;
        pressedBgDrawable = getResources().getDrawable(pressedDrawableId, null);
    }

    protected void updateStateAfterAnimation() {

    }

}
