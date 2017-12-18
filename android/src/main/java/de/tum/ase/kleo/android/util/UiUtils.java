package de.tum.ase.kleo.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Author sahakyanm
 * 18.12.17.
 */

public final class UiUtils {

    private UiUtils() {
    }

    @SuppressWarnings("unused")
    public static void hideKeyboard(Activity activity) {
        hideKeyboard(activity, activity.getCurrentFocus());
    }

    public static void hideKeyboard(Context context, View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
