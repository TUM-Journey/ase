package de.tum.ase.kleo.android.fragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

abstract class ReactiveLayoutFragment extends LayoutFragment {

    private final CompositeDisposable disposables = new CompositeDisposable();

    ReactiveLayoutFragment(int layoutResource) {
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
