package com.paulvarry.intra42.activities.projects;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import com.paulvarry.intra42.R;
import com.paulvarry.intra42.adapters.ViewStatePagerAdapter;
import com.paulvarry.intra42.ui.BasicActivity;
import com.paulvarry.intra42.ui.BasicTabActivity;
import com.paulvarry.intra42.ui.CustomViewPager;
import com.paulvarry.intra42.ui.tools.Navigation;

public class ProjectsActivity extends BasicTabActivity
        implements ProjectsDoingFragment.OnFragmentInteractionListener, ProjectsAllFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activeHamburger();
        super.setSelectedMenu(Navigation.MENU_SELECTED_PROJECTS);
        super.onCreate(savedInstanceState);

        if (!app.userIsLogged())
            finish();
    }

    @Override
    public void setupViewPager(ViewPager viewPager) {
        ViewStatePagerAdapter adapter = new ViewStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(ProjectsGraphFragment.newInstance(), getString(R.string.title_tab_projects_graphic));
        adapter.addFragment(ProjectsDoingFragment.newInstance(), getString(R.string.title_tab_projects_doing));
        adapter.addFragment(ProjectsAllFragment.newInstance(), getString(R.string.title_tab_projects_all));
        viewPager.setAdapter(adapter);
        ((CustomViewPager) viewPager).disableSwiping(getString(R.string.title_tab_projects_graphic));
    }

    @Nullable
    @Override
    public String getUrlIntra() {
        return null;
    }

    @Override
    public String getToolbarName() {
        return getString(R.string.title_activity_projects);
    }

    /**
     * This text is useful when both {@link GetDataOnThread#getDataOnOtherThread()} and {@link BasicActivity.GetDataOnMain#getDataOnMainThread()} return false.
     *
     * @return A simple text to display on screen, may return null;
     */
    @Override
    public String getEmptyText() {
        return null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
