package eu.socialedge.edu.kleo.util;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class AndroidReactives {

    private AndroidReactives() {
        throw new AssertionError("No instance for you");
    }

    public static <T> Observable<T> io(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Completable io(Completable completable) {
        return completable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Single<T> io(Single<T> single) {
        return single.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Maybe<T> io(Maybe<T> maybe) {
        return maybe.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Observable<T> compute(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    public static Completable compute(Completable completable) {
        return completable.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Single<T> compute(Single<T> single) {
        return single.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }

    public static <T> Maybe<T> compute(Maybe<T> maybe) {
        return maybe.subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread());
    }
}
