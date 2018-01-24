package de.tum.ase.kleo.app.support.ui;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.tum.ase.kleo.app.KleoApplication;
import de.tum.ase.kleo.app.client.BackendClient;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeIn;
import static de.tum.ase.kleo.app.support.ui.ProgressBars.fadeOut;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

public abstract class ResourceListLayoutFragment<T> extends ReactiveLayoutFragment {

    protected final @IdRes int listViewResource;
    protected RecyclerView listView;

    private final @LayoutRes int listItemLayout;

    protected final @IdRes int progressBarResource;

    private final boolean populateOnStart;
    protected ProgressBar progressBar;

    private final Integer noResourcesNotice;
    private View noResourcesNoticeView;

    protected BackendClient backendClient;

    protected ResourceListLayoutFragment(@LayoutRes int layout,
                                      @IdRes int layoutListView,
                                      @LayoutRes int listItemLayout,
                                      @IdRes int layoutProgressBar,
                                      int noResourcesNotice,
                                      boolean populateOnStart) {
        super(layout);
        this.listViewResource = layoutListView;
        this.listItemLayout = listItemLayout;
        this.progressBarResource = layoutProgressBar;
        this.noResourcesNotice = noResourcesNotice;
        this.populateOnStart = populateOnStart;
    }

    protected ResourceListLayoutFragment(@LayoutRes int layout,
                                         @IdRes int layoutListView,
                                         @LayoutRes int listItemLayout,
                                         @IdRes int layoutProgressBar,
                                         int noResourcesNotice) {
        this(layout, layoutListView, listItemLayout, layoutProgressBar, noResourcesNotice, true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backendClient = ((KleoApplication) getActivity().getApplication()).backendClient();
    }

    @Override
    protected void onCreateLayout(View view, Bundle savedInstanceState) {
        listView = view.findViewById(listViewResource);
        listView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        progressBar = view.findViewById(progressBarResource);
        if (noResourcesNotice != null) noResourcesNoticeView = view.findViewById(noResourcesNotice);

        hideProgressBar();

        if (populateOnStart) {
            hideNoResourcesNotice();
            populateResourceList();
        }
    }

    protected abstract Observable<List<T>> fetchResources();

    protected abstract void populateListItem(View view, T resource);

    protected void populateResourceList() {
        final Disposable disposable = fetchResources()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((r) -> this.showProgressBar())
                .doOnTerminate(this::hideProgressBar)
                .subscribe(resources -> {
                    if (resources == null || resources.isEmpty()) {
                        showNoResourcesNotice();
                    } else {
                        changeResources(resources);
                    }
                }, this::showErrorMessage);

        disposeOnDestroy(disposable);
    }

    @SuppressWarnings("unchecked")
    protected void changeResources(List<T> resources) {
        final Optional<ResourceListAdapter> currentListViewAdapterOpt = getCurrentListViewAdapter();

        if (currentListViewAdapterOpt.isPresent()) {
            currentListViewAdapterOpt.get().changeResources(resources);
        } else {
            listView.setAdapter(new ResourceListAdapter(resources));
        }
    }

    protected void removeResourceIf(Predicate<T> predicate) {
        getCurrentListViewAdapter().ifPresent(a -> a.removeResourceIf(predicate));
    }

    protected void updateResource(Consumer<T> updater) {
        getCurrentListViewAdapter().ifPresent(a -> a.updateResource(updater));
    }

    protected void clearResourceList() {
        listView.setAdapter(null);
    }

    protected void showProgressBar() {
        fadeIn(progressBar);
    }

    protected void hideProgressBar() {
        fadeOut(progressBar);
    }

    protected void showMessage(@StringRes int msgId) {
        Toast.makeText(getContext(), msgId, Toast.LENGTH_LONG).show();
    }

    protected void showMessage(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    protected void showErrorMessage(Throwable e) {
        showMessage(e.getMessage());
    }

    protected void showNoResourcesNotice() {
        if (noResourcesNoticeView != null) {
            noResourcesNoticeView.setVisibility(View.VISIBLE);
        }
    }

    protected void hideNoResourcesNotice() {
        if (noResourcesNoticeView != null) {
            noResourcesNoticeView.setVisibility(View.INVISIBLE);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<ResourceListAdapter> getCurrentListViewAdapter() {
        if (listView.getAdapter() == null) {
            return Optional.empty();
        }

        return Optional.of((ResourceListAdapter) listView.getAdapter());
    }

    private class ResourceListAdapter extends RecyclerView.Adapter<ResourceListItemHolder> {

        private List<T> resources;

        private ResourceListAdapter(List<T> resources) {
            this.resources = defaultIfNull(resources, emptyList());
        }

        @Override
        public ResourceListItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(listItemLayout, parent, false);
            return new ResourceListItemHolder(view);
        }

        @Override
        public void onBindViewHolder(ResourceListItemHolder holder, int position) {
            populateListItem(holder.itemView, resources.get(position));
        }

        void changeResources(List<T> resources) {
            this.resources = defaultIfNull(resources, emptyList());
            this.notifyDataSetChanged();
        }

        void removeResourceIf(Predicate<T> predicate) {
            if (this.resources.removeIf(predicate)) {
                this.notifyDataSetChanged();
            }
        }

        void updateResource(Consumer<T> updater) {
            this.resources.forEach(updater);
            this.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return resources.size();
        }
    }

    private static class ResourceListItemHolder extends RecyclerView.ViewHolder {

        public ResourceListItemHolder(View itemView) {
            super(itemView);
        }
    }
}
