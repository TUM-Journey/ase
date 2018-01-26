package de.tum.ase.kleo.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.util.SparseArray;
import android.view.MenuItem;

import java.util.function.Function;

import de.tum.ase.kleo.android.R;
import io.reactivex.Completable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class MainMenu implements NavigationView.OnNavigationItemSelectedListener {

    private final FragmentManager fragmentManager;
    private final SparseArray<Page> pages;
    private final SparseArray<Runnable> actions;
    private final NavigationView navigationView;

    private final Function<MenuItem, Completable> doBeforeTrigger;
    private final Function<MenuItem, Completable> doAfterTrigger;

    private @IdRes int currentMenuItemId;

    private MainMenu(Builder builder) {
        this.fragmentManager = builder.fragmentManager;
        this.pages = builder.pages;
        this.actions = builder.actions;
        this.doBeforeTrigger = builder.doBeforeTrigger;
        this.doAfterTrigger = builder.doAfterTrigger;
        this.navigationView = builder.navigationView;

        navigationView.setNavigationItemSelectedListener(this);
        fragmentManager.addOnBackStackChangedListener(()
                -> setNavigationViewCheckedItem(currentMenuItemId));

        if (builder.defaultPage != null) {
            switchFragment(builder.defaultPage.fragment, builder.defaultPage.backStackName);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        doBeforeTrigger.apply(menuItem)
                .doOnComplete(() -> {
                    final int menuItemId = menuItem.getItemId();
                    final Page nextPage = pages.get(menuItemId);
                    final Runnable action = actions.get(menuItemId);

                    if (nextPage != null) {
                        switchFragment(nextPage.fragment, nextPage.backStackName);

                        currentMenuItemId = menuItemId;
                    } else if (action != null) {
                        action.run();
                    }
                }).andThen(doAfterTrigger.apply(menuItem)).subscribe();

        return true;
    }

    private void switchFragment(Fragment fragment, String backStackName) {
        final FragmentTransaction pageTx = fragmentManager.beginTransaction()
                .setCustomAnimations(
                        android.R.animator.fade_in, android.R.animator.fade_out,
                        android.R.animator.fade_in, android.R.animator.fade_out)
                .replace(R.id.main_container, fragment);

        if (!isBlank(backStackName)) {
            pageTx.addToBackStack(backStackName);
        }

        pageTx.commit();
    }

    private void setNavigationViewCheckedItem(@IdRes int menuItemId) {
        final MenuItem menuItem = navigationView.getMenu().findItem(menuItemId);
        if (menuItem == null)
            throw new IllegalArgumentException("Unknown menu item");

        navigationView.setCheckedItem(menuItemId);
    }

    public static final class Builder {
        private FragmentManager fragmentManager;
        private NavigationView navigationView;

        private Page defaultPage;
        private SparseArray<Page> pages = new SparseArray<>();
        private SparseArray<Runnable> actions = new SparseArray<>();

        private Function<MenuItem, Completable> doBeforeTrigger = (i) -> Completable.complete();
        private Function<MenuItem, Completable> doAfterTrigger = (i) -> Completable.complete();

        public Builder defaultPage(Page page) {
            this.defaultPage = page;
            return this;
        }

        public Builder defaultPage(Page.Builder pageBuilder) {
            return defaultPage(pageBuilder.build());
        }

        public Builder use(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        public Builder use(NavigationView navigationView) {
            this.navigationView = navigationView;
            return this;
        }

        public Builder setOnClickPageChange(@IdRes int menuItem, Page page) {
            pages.put(menuItem, page);
            return this;
        }

        public Builder setOnClickPageChange(@IdRes int menuItem, Page.Builder  pageBuilder) {
            return setOnClickPageChange(menuItem, pageBuilder.build());
        }

        public Builder setOnClickAction(@IdRes int menuItem, Runnable action) {
            actions.put(menuItem, action);
            return this;
        }

        public Builder setBeforeTriggerListener(Function<MenuItem, Completable> doBeforeTrigger) {
            this.doBeforeTrigger = doBeforeTrigger;
            return this;
        }

        public Builder setAfterTriggerListener(Function<MenuItem, Completable> doAfterTrigger) {
            this.doAfterTrigger = doAfterTrigger;
            return this;
        }

        public MainMenu build() {
            return new MainMenu(this);
        }
    }

    public static Page.Builder Page() {
        return new Page.Builder();
    }

    public static final class Page {
        private final Fragment fragment;
        private final String backStackName;

        private Page(Fragment fragment, String backStackName) {
            this.fragment = notNull(fragment);
            this.backStackName = backStackName;
        }

        public static class Builder {
            private Fragment fragment;
            private String backStackName;

            public Builder from(Fragment fragment) {
                this.fragment = fragment;
                return this;
            }

            public Builder noBackStack() {
                this.backStackName = null;
                return this;
            }

            public Builder defaultBackStack() {
                this.backStackName = fragment.getClass().getName();
                return this;
            }

            public Builder backStack(String name) {
                this.backStackName = name;
                return this;
            }

            public Page build() {
                return new Page(fragment, backStackName);
            }
        }
    }
}
