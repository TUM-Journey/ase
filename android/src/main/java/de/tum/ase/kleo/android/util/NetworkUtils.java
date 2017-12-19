package de.tum.ase.kleo.android.util;

import io.reactivex.disposables.Disposable;

/**
 * Author sahakyanm
 * 19.12.17.
 */

public final class NetworkUtils {

    private NetworkUtils() {
    }

    public interface Configuration {
        String BASE_URL = "http://10.13.3.22:8080/api/";
        String CLIENT_ID = "kleo-client";
        String CLIENT_SECRET = "";
    }

    public static void unsubscribe(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
