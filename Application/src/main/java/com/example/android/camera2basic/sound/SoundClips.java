/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.example.android.camera2basic.sound;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;

import com.example.android.camera2basic.R;

public class SoundClips {
    private static final String TAG = "SoundClips";
    public static final int CAMERA_SHUTTER_SOUND_ID = 0;
    public static final int CAMERA_FOCUS_COMPLETE_SOUND_ID = 1;
    public static final int CAMERA_VIDEO_START_SOUND_ID = 2;
    public static final int CAMERA_VIDEO_PAUSE_SOUND_ID = 3;
    public static final int CAMERA_VIDEO_STOP_SOUND_ID = 4;
    public static final int CAMERA_BURST_START_SOUND_ID = 5;
    public static final int CAMERA_BURST_LOOP_SOUND_ID = 6;
    public static final int CAMERA_BURST_END_SOUND_ID = 7;
    public static final int CAMERA_PANORAMA_SOUND_ID = 8;

    public interface Player {
        void release();
        void play(int action);
    }

    public static Player getSoundPoolPlayer(Context context) {
    	return new SoundPoolPlayer(context);
    }

    private static class SoundPoolPlayer implements
            Player, SoundPool.OnLoadCompleteListener {
        private static final int[] SOUND_RES = {
            R.raw.camera_shutter,
            R.raw.focus_complete,
            R.raw.video_start,
            R.raw.video_pause,
            R.raw.video_stop,
            R.raw.camera_burst_start,
            R.raw.camera_burst_loop,
            R.raw.camera_burst_end,
            R.raw.panorama_single_photo_shutter_sound
        };

        // ID returned by load() should be non-zero.
        private static final int ID_NOT_LOADED = 0;

        // Maps a sound action to the id;
        private final int[] mSoundRes = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        private Context mContext;
        private SoundPool mSoundPool;
        private final int[] mSoundIDs;
        private final boolean[] mSoundIDReady;
        private int mSoundIDToPlay;

        public SoundPoolPlayer(Context context) {
            Log.v(TAG,"SoundPoolPlayer");
            mContext = context;
            mSoundIDToPlay = ID_NOT_LOADED;

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();

            mSoundPool.setOnLoadCompleteListener(this);
            mSoundIDs = new int[SOUND_RES.length];
            mSoundIDReady = new boolean[SOUND_RES.length];
            for (int i = 0; i < SOUND_RES.length; i++) {
                mSoundIDs[i] = mSoundPool.load(mContext, SOUND_RES[i], 1);
                mSoundIDReady[i] = false;
            }
        }

        @Override
        public void onLoadComplete(SoundPool pool, int soundID, int status) {
            if (status != 0) {
                Log.e(TAG, "loading sound tracks failed (status=" + status + ")");
                for (int i = 0; i < mSoundIDs.length; i++ ) {
                    if (mSoundIDs[i] == soundID) {
                        mSoundIDs[i] = ID_NOT_LOADED;
                        break;
                    }
                }
                return;
            }

            for (int i = 0; i < mSoundIDs.length; i++ ) {
                if (mSoundIDs[i] == soundID) {
                    mSoundIDReady[i] = true;
                    break;
                }
            }

            if (soundID == mSoundIDToPlay) {
                mSoundIDToPlay = ID_NOT_LOADED;
                mSoundPool.play(soundID, 1f, 1f, 0, 0, 1f);
            }
        }

        @Override
        public synchronized void release() {
            if (mSoundPool != null) {
                Log.v(TAG,"SoundPoolPlayer release");
                mSoundPool.release();
                mSoundPool = null;
            }
        }

        @Override
        public synchronized void play(int action) {
            if (action < 0 || action >= mSoundRes.length) {
                Log.e(TAG, "Resource ID not found for action:" + action + " in play().");
                return;
            }

            int index = mSoundRes[action];
            if (mSoundIDs[index] == ID_NOT_LOADED) {
                // Not loaded yet, load first and then play when the loading is complete.
                mSoundIDs[index] = mSoundPool.load(mContext, SOUND_RES[index], 1);
                mSoundIDToPlay = mSoundIDs[index];
            } else if (!mSoundIDReady[index]) {
                // Loading and not ready yet.
                mSoundIDToPlay = mSoundIDs[index];
            } else {
                mSoundPool.play(mSoundIDs[index], 1f, 1f, 0, 0, 1f);
            }
        }
    }
}
