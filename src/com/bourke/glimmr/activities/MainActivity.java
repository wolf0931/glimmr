package com.bourke.glimmrpro.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.PreferenceManager;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragment;

import com.androidquery.AQuery;

import com.bourke.glimmrpro.common.Constants;
import com.bourke.glimmrpro.common.GlimmrTabsAdapter;
import com.bourke.glimmrpro.fragments.explore.RecentPublicPhotosFragment;
import com.bourke.glimmrpro.fragments.home.ContactsGridFragment;
import com.bourke.glimmrpro.fragments.home.GroupListFragment;
import com.bourke.glimmrpro.fragments.home.PhotosetsFragment;
import com.bourke.glimmrpro.fragments.home.PhotoStreamGridFragment;
import com.bourke.glimmrpro.R;
import com.bourke.glimmrpro.services.AppListener;
import com.bourke.glimmrpro.services.AppService;

import com.commonsware.cwac.wakeful.WakefulIntentService;

import com.googlecode.flickrjandroid.people.User;

import com.sbstrm.appirater.Appirater;

import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends BaseActivity {

    private static final String TAG = "Glimmr/MainActivity";

    public static final int CONTACTS_PAGE = 0;
    public static final int PHOTOSTREAM_PAGE = 1;
    public static final int SETS_PAGE = 2;
    public static final int GROUPS_PAGE = 3;
    public static final int EXPLORE_PAGE = 4;

    public static String[] CONTENT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Constants.DEBUG) Log.d(getLogTag(), "onCreate");

        CONTENT = new String[] { getString(R.string.contacts),
            getString(R.string.you), getString(R.string.sets),
            getString(R.string.groups), getString(R.string.explore)
        };

        if (mOAuth == null) {
            startActivity(new Intent(this, ExploreActivity.class));
        } else {
            setContentView(R.layout.main_activity);
            mAq = new AQuery(this);
            initViewPager();

            /* Enable alarms */
            SharedPreferences defaultSharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
            boolean enableNotifications = defaultSharedPrefs.getBoolean(
                    Constants.KEY_ENABLE_NOTIFICATIONS, false);
            if (enableNotifications) {
                if (Constants.DEBUG) Log.d(TAG, "Scheduling alarms");
                WakefulIntentService.scheduleAlarms(
                        new AppListener(), this, false);
            } else {
                if (Constants.DEBUG) Log.d(TAG, "Cancelling alarms");
                AppService.cancelAlarms(this);
            }
            Appirater.appLaunched(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
        if (mOAuth != null) {
            mUser = mOAuth.getUser();
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(
                R.id.indicator);

        /* Bind the ViewPager to a TitlePageIndicator, or if on larger screens,
         * set up actionbar tabs. */
        if (indicator != null) {
            GlimmrPagerAdapter adapter = new GlimmrPagerAdapter(
                    getSupportFragmentManager());
            viewPager.setAdapter(adapter);
            indicator.setOnPageChangeListener(this);
            indicator.setViewPager(viewPager);
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            GlimmrTabsAdapter tabsAdapter =
                new GlimmrTabsAdapter(this, viewPager);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[CONTACTS_PAGE]),
                    ContactsGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[PHOTOSTREAM_PAGE]),
                    PhotoStreamGridFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[GROUPS_PAGE]),
                    GroupListFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[SETS_PAGE]),
                    PhotosetsFragment.class, null);
            tabsAdapter.addTab(
                    mActionBar.newTab().setText(CONTENT[EXPLORE_PAGE]),
                    RecentPublicPhotosFragment.class, null);
            viewPager.setAdapter(tabsAdapter);
            viewPager.setOnPageChangeListener(tabsAdapter);
        }
    }

    @Override
    public User getUser() {
        return mUser;
    }

    class GlimmrPagerAdapter extends FragmentPagerAdapter {
        public GlimmrPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public SherlockFragment getItem(int position) {
            switch (position) {
                case PHOTOSTREAM_PAGE:
                    return PhotoStreamGridFragment.newInstance();

                case CONTACTS_PAGE:
                    return ContactsGridFragment.newInstance();

                case GROUPS_PAGE:
                    return GroupListFragment.newInstance();

                case SETS_PAGE:
                    return PhotosetsFragment.newInstance();

                case EXPLORE_PAGE:
                    return RecentPublicPhotosFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return MainActivity.CONTENT.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return MainActivity.CONTENT[position % MainActivity.CONTENT.length]
                .toUpperCase();
        }
    }
}
