package de.tum.ase.kleo.app.support;

import android.support.annotation.LayoutRes;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class ReactiveLayoutFragment extends LayoutFragment {

    private final CompositeDisposable disposables = new CompositeDisposable();

    public ReactiveLayoutFragment(@LayoutRes int layoutResource) {
        super(layoutResource);
    }

    protected void disposeOnDestroy(Disposable disposable) {
        disposables.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }
}
