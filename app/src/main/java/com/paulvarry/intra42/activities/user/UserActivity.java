package com.paulvarry.intra42.activities.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.paulvarry.intra42.AppClass;
import com.paulvarry.intra42.R;
import com.paulvarry.intra42.adapters.ViewPagerAdapter;
import com.paulvarry.intra42.api.ApiService;
import com.paulvarry.intra42.api.ServiceGenerator;
import com.paulvarry.intra42.api.model.CursusUsers;
import com.paulvarry.intra42.api.model.Locations;
import com.paulvarry.intra42.api.model.Users;
import com.paulvarry.intra42.api.model.UsersLTE;
import com.paulvarry.intra42.cache.CacheUsers;
import com.paulvarry.intra42.ui.BasicActivity;
import com.paulvarry.intra42.ui.BasicTabActivity;
import com.paulvarry.intra42.ui.CustomViewPager;
import com.paulvarry.intra42.ui.tools.Navigation;
import com.paulvarry.intra42.utils.AppSettings;
import com.paulvarry.intra42.utils.Friends;
import com.paulvarry.intra42.utils.UserImage;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class UserActivity extends BasicTabActivity
        implements UserOverviewFragment.OnFragmentInteractionListener, UserMarksFragment.OnFragmentInteractionListener,
        UserProjectsDoingFragment.OnFragmentInteractionListener, UserSkillsFragment.OnFragmentInteractionListener,
        UserAchievementsFragment.OnFragmentInteractionListener, UserForumFragment.OnFragmentInteractionListener,
        UserExpertisesFragment.OnFragmentInteractionListener, UserProjectsFragment.OnFragmentInteractionListener,
        BasicActivity.GetDataOnMain, BasicActivity.GetDataOnThread {

    private static final String INTENT_USER_LTE = "user_lte";
    private static final String INTENT_USER_FULL = "user_full";
    private static final String INTENT_LOGIN = "user_login";

    public Users user;
    public HashMap<String, Bitmap> picAchievements;
    public CursusUsers selectedCursus;
    String login;
    int selectedTab;

    public static void openIt(Context context, UsersLTE user) {
        if (user != null) {
            Intent intent = getIntent(context, user);
            context.startActivity(intent);
        }
    }

    public static Intent getIntent(Context context, UsersLTE user) {
        if (user != null) {
            Intent intent = new Intent(context, UserActivity.class);
            intent.putExtra(UserActivity.INTENT_USER_LTE, ServiceGenerator.getGson().toJson(user));
            return intent;
        }
        return null;
    }

    public static void openIt(final Context context, final UsersLTE user, Activity activity) {
        if (user != null)
            openIt(context, user.login, activity);
    }

    public static boolean openIt(final Context context, final String login, final Activity activity) {
        return activity != null && openIt(context, login, (AppClass) activity.getApplication());
    }

    public static boolean openIt(final Context context, final String login, final AppClass app) {
        if (app != null) {

            if (CacheUsers.isCached(app.cacheSQLiteHelper, login)) {
                UserActivity.openIt(context, login);
                return true;
            }

            final ProgressDialog dialog = ProgressDialog.show(context, "", context.getString(R.string.loading_please_wait), true);
            ApiService s = app.getApiService();

            Call<Users> call = s.getUser(login);
            call.enqueue(new Callback<Users>() {
                @Override
                public void onResponse(Call<Users> call, Response<Users> response) {

                    if (!response.isSuccessful() && response.code() != 404)
                        Toast.makeText(context, "Error on get user", Toast.LENGTH_SHORT).show();
                    else if (response.body() == null || response.code() == 404)
                        Toast.makeText(context, "Users not found", Toast.LENGTH_SHORT).show();
                    else {

                        UserActivity.openIt(context, response.body());

                        String gson = ServiceGenerator.getGson().toJson(response.body());
                        CacheUsers.put(app.cacheSQLiteHelper, response.body(), gson);

                    }
                    dialog.cancel();
                }

                @Override
                public void onFailure(Call<Users> call, Throwable t) {
                    dialog.cancel();
                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return false;
    }

    public static void openIt(Context context, String login) {

        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra(UserActivity.INTENT_LOGIN, login);
        context.startActivity(intent);

    }

    public static void openIt(Context context, Users user) {
        if (user != null) {
            Intent intent = new Intent(context, UserActivity.class);
            intent.putExtra(UserActivity.INTENT_USER_FULL, ServiceGenerator.getGson().toJson(user));
            context.startActivity(intent);
        }
    }

    public static void openLocation(final Context context, String location, final AppClass app) {
        final ProgressDialog dialog = ProgressDialog.show(context, "", context.getString(R.string.loading_please_wait), true);
        ApiService s = app.getApiService();

        Call<List<Locations>> call = s.getLocationsHost(location);
        call.enqueue(new Callback<List<Locations>>() {
            @Override
            public void onResponse(Call<List<Locations>> call, final Response<List<Locations>> response) {

                if (!response.isSuccessful() && response.code() != 404)
                    Toast.makeText(context, "Error on get", Toast.LENGTH_SHORT).show();
                else if (response.body() == null || response.code() == 404)
                    Toast.makeText(context, "not found", Toast.LENGTH_SHORT).show();
                else {

                    AlertDialog.Builder d = new AlertDialog.Builder(context);

                    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                            context,
                            android.R.layout.select_dialog_singlechoice);

                    for (Locations l : response.body()) {
                        arrayAdapter.add(l.user.login);
                    }
                    d.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            UserActivity.openIt(context, response.body().get(position).user, app);
                        }
                    });
                    d.create().show();
                }
                dialog.cancel();
            }

            @Override
            public void onFailure(Call<List<Locations>> call, Throwable t) {
                dialog.cancel();
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void openIt(final Context context, final UsersLTE user, AppClass app) {
        if (user != null)
            openIt(context, user.login, app);
    }

    @Override
    public final Object onRetainCustomNonConfigurationInstance() {
        return user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //handle just logged users.
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                login = handleSendText(intent); // Handle text being sent
            }
        } else if (Intent.ACTION_VIEW.equals(action)) {
            if (intent.getData().getHost().equals("profile.intra.42.fr")) {
                List<String> tmp = intent.getData().getPathSegments();
                if (tmp.size() == 2)
                    login = tmp.get(1);
            }
        } else {
            if (intent.hasExtra(INTENT_USER_FULL)) {
                user = ServiceGenerator.getGson().fromJson(intent.getStringExtra(INTENT_USER_FULL), Users.class);
                if (user != null)
                    login = user.login;
            } else if (intent.hasExtra(INTENT_USER_LTE)) {
                login = ServiceGenerator.getGson().fromJson(intent.getStringExtra(INTENT_USER_LTE), UsersLTE.class).login;
            } else if (intent.hasExtra(INTENT_LOGIN))
                login = intent.getStringExtra(INTENT_LOGIN).toLowerCase();
        }

        user = (Users) getLastCustomNonConfigurationInstance();
        if (user != null)
            login = user.login;

        registerGetDataOnOtherThread(this);
        registerGetDataOnMainTread(this);

        super.setSelectedMenu(Navigation.MENU_SELECTED_USERS);

        super.onCreate(savedInstanceState);
        if (user == null && login == null)
            finish();
    }

    private String handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            return sharedText.toLowerCase();
        }
        return null;
    }

    @Override
    protected void setViewContent() {
        super.setViewContent();

        if (user != null && user.local_cachedAt != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(user.local_cachedAt);
            calendar.add(Calendar.MINUTE, 15);
            Date timeout = calendar.getTime();
            if (user.local_cachedAt != null && timeout.before(new Date())) {

                Snackbar s = Snackbar.make(coordinatorLayout, "Data cached long time ago", Snackbar.LENGTH_LONG);
                s.setAction(R.string.refresh, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Snackbar s = Snackbar.make(coordinatorLayout, "Refreshing", Snackbar.LENGTH_INDEFINITE);
                        s.show();

                        refresh(new Runnable() {
                            @Override
                            public void run() {
                                UserActivity.super.setView();
                                s.dismiss();
                            }
                        });
                    }
                });
                s.show();
            }
        }
    }

    @Override
    public void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(UserOverviewFragment.newInstance(), getString(R.string.tab_user_overview));
        adapter.addFragment(UserForumFragment.newInstance(), getString(R.string.tab_user_forum));
        adapter.addFragment(UserProjectsFragment.newInstance(), getString(R.string.tab_user_projects));
//        adapter.addFragment(UserMarksFragment.newInstance(), getString(R.string.tab_user_marks));
//        adapter.addFragment(UserProjectsDoingFragment.newInstance(), getString(R.string.tab_user_projects));
        adapter.addFragment(UserExpertisesFragment.newInstance(), getString(R.string.tab_user_expertises));
        adapter.addFragment(UserAchievementsFragment.newInstance(), getString(R.string.tab_user_achievements));
        adapter.addFragment(UserSkillsFragment.newInstance(), getString(R.string.tab_user_skills));
        adapter.addFragment(UserPartnershipsFragment.newInstance(), getString(R.string.tab_user_partnerships));

        if (AppSettings.Advanced.getAllowAdvancedData(this)) {
            adapter.addFragment(UserAppsFragment.newInstance(), "Apps");
        }

        viewPager.setAdapter(adapter);

        ((CustomViewPager) viewPager).disableSwiping(getString(R.string.tab_user_projects));

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                selectedTab = position;
                if (menuItemSpinner == null)
                    return;
                if (position == 2) {
                    menuItemSpinner.setVisible(true);

                    new MaterialShowcaseView.Builder(UserActivity.this)
                            .setTarget(menuItemSpinner.getActionView())
                            .setDismissText(getString(R.string.showcaseview_user_project_galaxy_title))
                            .setDelay(200) // optional but starting animations immediately in onCreate can make them choppy
                            .singleUse("showcase_view_user_project_galaxy") // provide a unique ID used to ensure it is only shown once
                            .setMaskColour(Color.argb(220, 230, 81, 0))
                            .setContentText(getString(R.string.showcaseview_user_project_galaxy))
                            .setDismissOnTouch(true)
                            .show();
                } else
                    menuItemSpinner.setVisible(false);

                app.mFirebaseAnalytics.setCurrentScreen(UserActivity.this, "User Profile -> " + adapter.getItem(position).getClass().getSimpleName(), null /* class override */);
            }
        });
        app.mFirebaseAnalytics.setCurrentScreen(UserActivity.this, "User Profile -> " + UserOverviewFragment.class.getSimpleName(), null /* class override */);
    }

    public void refresh(final Runnable runnable) {
        if (app == null)
            return;

        final Runnable localRunnable = new Runnable() {
            @Override
            public void run() {
                if (app.me != null && login != null && login.contentEquals(app.me.login))
                    user = app.me;
                if (user != null && user.cursusUsers != null && !user.cursusUsers.isEmpty()) {
                    selectedCursus = user.cursusUsers.get(0);
                }
                if (runnable != null)
                    runnable.run();
            }
        };

        if (login != null) {

            app.getApiService().getUser(login).enqueue(new Callback<Users>() {
                @Override
                public void onResponse(Call<Users> call, Response<Users> response) {
                    if (response.isSuccessful()) {
                        user = response.body();
                        if (app.me != null && login != null && login.contentEquals(app.me.login)) // when refreshing
                            app.me = response.body();
                        CacheUsers.put(app.cacheSQLiteHelper, user);
                    }
                    localRunnable.run();
                }

                @Override
                public void onFailure(Call<Users> call, Throwable t) {
                    localRunnable.run();
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(R.string.menu_action_user_add_to_home).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addShortCut();
                return true;
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_user_projects, R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        Spinner menuSpinner = (Spinner) menuItemSpinner.getActionView();
        //menuSpinner.setGravity(Gravity.END);
        menuSpinner.setAdapter(adapter);

        if (viewPager != null) {
            ViewPagerAdapter pagerAdapter = (ViewPagerAdapter) viewPager.getAdapter();

            menuSpinner.setOnItemSelectedListener(((UserProjectsFragment) pagerAdapter.getItem(2)));
            if (selectedTab == 2)
                menuItemSpinner.setVisible(true);
        }

//        menu.add("Add to contacts").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Contacts.add(UserActivity.this, user);
//                    }
//                }).start();
//
//                return true;
//            }
//        });

        menu.add(R.string.menu_action_user_add_friends).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Friends.actionAddRemove(app.firebaseRefFriends, user);
                return true;
            }
        });

        return true;
    }

    @Nullable
    @Override
    public String getUrlIntra() {
        if (user != null)
            return getString(R.string.base_url_intra_profile) + "users/" + user.login;
        return null;
    }

    @Override
    public StatusCode getDataOnOtherThread() {
        if (login != null) {
            if (user == null) {
                ApiService service = app.getApiService();
                Call<Users> call = service.getUser(login);
                try {
                    Response<Users> ret = call.execute();
                    if (ret.code() == 200)
                        user = ret.body();
                    else
                        return StatusCode.ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (user != null && user.cursusUsers != null && !user.cursusUsers.isEmpty()) {
                selectedCursus = user.cursusUsers.get(0);
            }
        }
        return StatusCode.FINISH;
    }

    @Override
    public StatusCode getDataOnMainThread() {
        if (user != null)
            return StatusCode.FINISH;
        if (app == null)
            return StatusCode.ERROR;

        if (app.me != null && login != null && login.contentEquals(app.me.login)) {
            user = app.me;
            return StatusCode.FINISH;
        }

        Users tmp = CacheUsers.get(app.cacheSQLiteHelper, login);
        if (tmp != null) {
            user = tmp;
            return StatusCode.FINISH;
        }

        return StatusCode.CONTINUE;
    }

    @Override
    public String getToolbarName() {
        if (user != null)
            return user.login;
        else
            return login;
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

    private void addShortCut() {
        if (user == null)
            return;

        final Intent addIntent = new Intent();
        final RequestCreator p = UserImage.getPicassoCorned(UserActivity.this, user);
        if (p == null)
            return;
        p.resize(200, 240);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = p.get();
                    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
                } catch (final IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Adding shortcut for MainActivity
                        //on Home screen
                        Intent shortcutIntent = new Intent(getApplicationContext(), UserActivity.class);

                        shortcutIntent.setAction(Intent.ACTION_MAIN);
                        shortcutIntent.putExtra(INTENT_LOGIN, user.login);


                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
                        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, user.login);

                        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                        addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
                        getApplicationContext().sendBroadcast(addIntent);
                    }
                });
            }
        }).start();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
