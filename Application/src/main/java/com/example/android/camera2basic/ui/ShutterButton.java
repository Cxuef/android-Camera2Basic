/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * A button designed to be used for the on-screen shutter button.
 * It's currently an {@code ImageView} that can call a delegate when the
 * pressed state changes.
 */
public class ShutterButton extends RotateImageView implements View.OnLongClickListener {

    private static final String TAG = "ShutterButton";

    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(ShutterButton button, boolean pressed);

        void onShutterButtonClick(ShutterButton button);

        void onShutterButtonLongPressed(ShutterButton button);
    }

    private OnShutterButtonListener mListener;
    private boolean mOldPressed;
    private boolean mVolumeKeyDown = false;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
    }

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        Log.i(TAG, "drawableStateChanged() pressed = " + pressed);
        if (pressed != mOldPressed) {
            if (!pressed) {
                post(() -> callShutterButtonFocus(pressed));
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        if (mListener != null) {
            mListener.onShutterButtonFocus(this, pressed);
        }
    }

    @Override
    public boolean performClick() {
        if (mVolumeKeyDown) {
            return false;
        }
        boolean result = super.performClick();
        if (mListener != null) {
            mListener.onShutterButtonClick(this);
        }
        return result;
    }

    public boolean onLongClick(View v) {
        if (mListener != null) {
            mListener.onShutterButtonLongPressed(this);
            return true;
        }
        return false;
    }

    public void setVolumeKeyDown(boolean down) {
        mVolumeKeyDown = down;
    }

    public boolean isVolumeKeyDown() {
        return mVolumeKeyDown;
    }

    @Override
    protected void updateStateAfterAnimation() {
        refreshDrawableState();
        postInvalidate();
    }
}
