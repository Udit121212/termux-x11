package com.termux.x11.utils;

import static com.termux.shared.termux.extrakeys.ExtraKeysConstants.PRIMARY_KEY_CODES_FOR_STRINGS;
import static com.termux.x11.MainActivity.toggleKeyboardVisibility;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.termux.shared.termux.extrakeys.*;
import com.termux.x11.LoriePreferences;
import com.termux.x11.MainActivity;

import org.json.JSONException;

public class TermuxX11ExtraKeys implements ExtraKeysView.IExtraKeysView {
    @SuppressWarnings("FieldCanBeLocal")
    private static final String LOG_TAG = "TermuxX11ExtraKeys";
    private final View.OnKeyListener mEventListener;
    private final MainActivity mActivity;
    private final ExtraKeysView mExtraKeysView;
    private final ClipboardManager mClipboardManager;
    static private ExtraKeysInfo mExtraKeysInfo;

    private boolean ctrlDown;
    private boolean altDown;
    private boolean shiftDown;
    private boolean metaDown;

    /** Defines the key for extra keys */
    public static final String DEFAULT_IVALUE_EXTRA_KEYS = "[['ESC','/',{key: '-', popup: '|'},'HOME','UP','END','PGUP','PREFERENCES'], ['TAB','CTRL','ALT','LEFT','DOWN','RIGHT','PGDN','KEYBOARD']]"; // Double row

    public TermuxX11ExtraKeys(@NonNull View.OnKeyListener eventlistener, MainActivity activity, ExtraKeysView extrakeysview) {
        mEventListener = eventlistener;
        mActivity = activity;
        mExtraKeysView = extrakeysview;
        mClipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private final KeyCharacterMap mVirtualKeyboardKeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    static final String ACTION_START_PREFERENCES_ACTIVITY = "com.termux.x11.start_preferences_activity";

    @Override
    public void onExtraKeyButtonClick(View view, ExtraKeyButton buttonInfo, Button button) {
        Log.e("keys", "key " + buttonInfo.display);
        if (buttonInfo.macro) {
            String[] keys = buttonInfo.key.split(" ");
            boolean ctrlDown = false;
            boolean altDown = false;
            boolean shiftDown = false;
            boolean metaDown = false;
            boolean fnDown = false;
            for (String key : keys) {
                if (SpecialButton.CTRL.getKey().equals(key)) {
                    ctrlDown = true;
                } else if (SpecialButton.ALT.getKey().equals(key)) {
                    altDown = true;
                } else if (SpecialButton.SHIFT.getKey().equals(key)) {
                    shiftDown = true;
                } else if (SpecialButton.META.getKey().equals(key)) {
                    metaDown = true;
                } else if (SpecialButton.FN.getKey().equals(key)) {
                    fnDown = true;
                } else {
                    ctrlDown = false;
                    altDown = false;
                    shiftDown = false;
                    metaDown = false;
                    fnDown = false;
                }
                onLorieExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, metaDown, fnDown);
            }
        } else {
            onLorieExtraKeyButtonClick(view, buttonInfo.key, false, false, false, false, false);
        }
    }

    protected void onTerminalExtraKeyButtonClick(@SuppressWarnings("unused") View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean metaDown, @SuppressWarnings("unused") boolean fnDown) {
        if (this.ctrlDown != ctrlDown) {
            this.ctrlDown = ctrlDown;
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_CTRL_LEFT, ctrlDown);
        }

        if (this.altDown != altDown) {
            this.altDown = altDown;
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_ALT_LEFT, altDown);
        }

        if (this.shiftDown != shiftDown) {
            this.shiftDown = shiftDown;
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_SHIFT_LEFT, shiftDown);
        }

        if (this.metaDown != metaDown) {
            this.metaDown = metaDown;
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_META_LEFT, metaDown);
        }

        if (PRIMARY_KEY_CODES_FOR_STRINGS.containsKey(key)) {
            Integer keyCode = PRIMARY_KEY_CODES_FOR_STRINGS.get(key);
            if (keyCode == null) return;

            mActivity.getLorieView().sendKeyEvent(0, keyCode, true);
            mActivity.getLorieView().sendKeyEvent(0, keyCode, false);
        } else {
            // not a control char
            mActivity.getLorieView().sendTextEvent(key.getBytes(UTF_8));
        }
    }

    public void unsetSpecialKeys() {
        if (mExtraKeysView == null)
            return;

        if (Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.CTRL, true)))
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_CTRL_LEFT, false);
        if (Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.ALT, true)))
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_ALT_LEFT, false);
        if (Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.SHIFT, true)))
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_SHIFT_LEFT, false);
        if (Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.META, true)))
            mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_META_LEFT, false);
    }

    @Override
    public boolean performExtraKeyButtonHapticFeedback(View view, ExtraKeyButton buttonInfo, Button button) {
        MainActivity.handler.postDelayed(() -> {
            boolean pressed;
            switch (buttonInfo.key) {
                case "CTRL":
                    pressed = Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.CTRL, false));
                    mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_CTRL_LEFT, pressed);
                    break;
                case "ALT":
                    pressed = Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.ALT, false));
                    mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_ALT_LEFT, pressed);
                    break;
                case "SHIFT":
                    pressed = Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.SHIFT, false));
                    mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_SHIFT_LEFT, pressed);
                    break;
                case "META":
                    pressed = Boolean.TRUE.equals(mExtraKeysView.readSpecialButton(SpecialButton.META, false));
                    mActivity.getLorieView().sendKeyEvent(0, KeyEvent.KEYCODE_META_LEFT, pressed);
                    break;
            }
        }, 100);

        return false;
    }

    @SuppressLint("RtlHardcoded")
    public void onLorieExtraKeyButtonClick(View view, String key, boolean ctrlDown, boolean altDown, boolean shiftDown, boolean metaDown, boolean fnDown) {
        if ("KEYBOARD".equals(key))
            toggleKeyboardVisibility(mActivity);
        else if ("DRAWER".equals(key) || "PREFERENCES".equals(key))
            mActivity.startActivity(new Intent(mActivity, LoriePreferences.class) {{ setAction(ACTION_START_PREFERENCES_ACTIVITY); }});
        else if ("EXIT".equals(key))
            mActivity.finish();
        else if ("PASTE".equals(key)) {
            ClipData clipData = mClipboardManager.getPrimaryClip();
            if (clipData != null) {
                CharSequence pasted = clipData.getItemAt(0).coerceToText(mActivity);
                if (!TextUtils.isEmpty(pasted)) {
                    KeyEvent[] events = mVirtualKeyboardKeyCharacterMap.getEvents(pasted.toString().toCharArray());
                    if (events != null)
                        for (KeyEvent event : events)
                            mEventListener.onKey(mActivity.getLorieView(), event.getKeyCode(), event);
                }
            }
        } else {
            onTerminalExtraKeyButtonClick(view, key, ctrlDown, altDown, shiftDown, metaDown, fnDown);
        }
    }

    /**
     * Set the terminal extra keys and style.
     */
    public static void setExtraKeys() {
        mExtraKeysInfo = null;

        try {
            // The mMap stores the extra key and style string values while loading properties
            // Check {@link #getExtraKeysInternalPropertyValueFromValue(String)} and
            // {@link #getExtraKeysStyleInternalPropertyValueFromValue(String)}
            String extrakeys = MainActivity.getPrefs().extra_keys_config.get();
            mExtraKeysInfo = new ExtraKeysInfo(extrakeys, "extra-keys-style", ExtraKeysConstants.CONTROL_CHARS_ALIASES);
        } catch (JSONException e) {
            Toast.makeText(MainActivity.getInstance(), "Could not load and set the \"extra-keys\" property from the properties file: " + e, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "Could not load and set the \"extra-keys\" property from the properties file: ", e);

            try {
                mExtraKeysInfo = new ExtraKeysInfo(TermuxX11ExtraKeys.DEFAULT_IVALUE_EXTRA_KEYS, "default", ExtraKeysConstants.CONTROL_CHARS_ALIASES);
            } catch (JSONException e2) {
                Toast.makeText(MainActivity.getInstance(), "Can't create default extra keys", Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG, "Could create default extra keys: ", e);
                mExtraKeysInfo = null;
            }
        }
    }

    public static ExtraKeysInfo getExtraKeysInfo() {
        if (mExtraKeysInfo == null)
            setExtraKeys();
        return mExtraKeysInfo;
    }
}
