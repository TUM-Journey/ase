package de.tum.ase.kleo.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import java.util.function.Function;

import io.reactivex.Completable;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.Validate.notNull;

public class MenuPageNavigation implements NavigationView.OnNavigationItemSelectedListener {

    private final static String MENU_BACK_STACK_NAME = "main-menu-back-stack";

    private final @IdRes int fragmentContainer;
    private final FragmentManager fragmentManager;
    private final SparseArray<Fragment> pages;
    private final SparseArray<Runnable> actions;
    private final NavigationView navigationView;

    private final Function<MenuItem, Completable> doBeforeTrigger;
    private final Function<MenuItem, Completable> doAfterTrigger;

    private MenuPageNavigation(Builder builder) {
        this.fragmentContainer = builder.fragmentContainer;
        this.fragmentManager = builder.fragmentManager;
        this.pages = builder.pages;
        this.actions = builder.actions;
        this.doBeforeTrigger = builder.doBeforeTrigger;
        this.doAfterTrigger = builder.doAfterTrigger;
        this.navigationView = builder.navigationView;

        navigationView.setNavigationItemSelectedListener(this);
        fragmentManager.addOnBackStackChangedListener(this::syncMenuItemAndCurrentFragment);

        if (builder.defaultPage != null) {
            switchFragment(builder.defaultPage, null);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        doBeforeTrigger.apply(menuItem)
                .doOnComplete(() -> {
                    final int menuItemId = menuItem.getItemId();
                    final Fragment nextPage = pages.get(menuItemId);
                    final Runnable action = actions.get(menuItemId);

                    if (nextPage != null) {
                        switchFragment(nextPage, MENU_BACK_STACK_NAME);
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
                .replace(fragmentContainer, fragment);

        if (!isBlank(backStackName)) {
            pageTx.addToBackStack(backStackName);
        }

        pageTx.commit();
    }

    private void syncMenuItemAndCurrentFragment() {
        final Fragment currentFragment = findCurrentFragment();
        if (currentFragment == null) {
            uncheckAllNavigationViewItems();
            return;
        }

        final Integer currentMenuItem = findMenuItemByFragment(currentFragment.getClass());
        if (currentMenuItem == null) {
            uncheckAllNavigationViewItems();
            return;
        }

        setNavigationViewCheckedItem(currentMenuItem);
    }

    private @IdRes Integer findMenuItemByFragment(Class<?> fragmentType) {
        Integer menuItemId = null;

        for(int i = 0; i < pages.size(); i++) {
            int pageMenuItemId = pages.keyAt(i);
            Fragment pageFragment = pages.get(pageMenuItemId);

            if (pageFragment.getClass().equals(fragmentType)) {
                menuItemId = pageMenuItemId;
                break;
            }
        }

        return menuItemId;
    }

    private Fragment findCurrentFragment() {
        return fragmentManager.findFragmentById(fragmentContainer);
    }

    private void setNavigationViewCheckedItem(@IdRes int menuItemId) {
        final MenuItem menuItem = navigationView.getMenu().findItem(menuItemId);
        if (menuItem == null)
            throw new IllegalArgumentException("Unknown menu item");

        navigationView.setCheckedItem(menuItemId);
    }

    private void uncheckAllNavigationViewItems() {
        final Menu navigationViewMenu = navigationView.getMenu();
        for (int i = 0; i < navigationViewMenu.size(); i++) {
            navigationViewMenu.getItem(i).setChecked(false);
        }
    }

    public static final class Builder {
        private int fragmentContainer;
        private FragmentManager fragmentManager;
        private NavigationView navigationView;

        private Fragment defaultPage;
        private SparseArray<Fragment> pages = new SparseArray<>();
        private SparseArray<Runnable> actions = new SparseArray<>();

        private Function<MenuItem, Completable> doBeforeTrigger = (i) -> Completable.complete();
        private Function<MenuItem, Completable> doAfterTrigger = (i) -> Completable.complete();

        public Builder defaultPage(Fragment page) {
            this.defaultPage = page;
            return this;
        }

        public Builder withFragmentContainer(int fragmentContainer) {
            this.fragmentContainer = fragmentContainer;
            return this;
        }

        public Builder withFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        public Builder withNavigationView(NavigationView navigationView) {
            this.navigationView = navigationView;
            return this;
        }

        public Builder setOnClickPageChange(@IdRes int menuItem, Fragment page) {
            pages.put(menuItem, page);
            return this;
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

        public MenuPageNavigation build() {
            return new MenuPageNavigation(this);
        }
    }
}
