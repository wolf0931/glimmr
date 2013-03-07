package com.bourke.glimmr.fragments.group;

import com.bourke.glimmr.fragments.viewer.AddToGroupDialogFragment;

import android.content.Context;
import android.content.SharedPreferences;

import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadGroupPoolTask;

import com.googlecode.flickrjandroid.groups.Group;
import com.googlecode.flickrjandroid.photos.Photo;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.view.MenuItem;

public class GroupPoolGridFragment extends PhotoGridFragment {

    private static final String TAG = "Glimmr/GroupPoolGridFragment";

    public static final String KEY_NEWEST_GROUPPOOL_PHOTO_ID =
        "glimmr_newest_grouppool_photo_id";
    public static final String KEY_GROUP_FRAGMENT_GROUP_ID =
        "glimmr_grouppool_group_id";

    private Group mGroup;

    protected LoadGroupPoolTask mTask;

    public static GroupPoolGridFragment newInstance(Group group) {
        GroupPoolGridFragment newFragment = new GroupPoolGridFragment();
        newFragment.mGroup = group;
        return newFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.groupviewer_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_photos:
                FragmentTransaction ft =
                    mActivity.getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                Fragment prev = mActivity.getSupportFragmentManager()
                    .findFragmentByTag(AddToGroupDialogFragment.TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                SherlockDialogFragment newFragment =
                    AddToGroupDialogFragment.newInstance(mGroup);
                newFragment.show(ft, AddToGroupDialogFragment.TAG);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.  So we don't need to
     * override startTask().
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        if (mGroup == null) {
            loadGroup();
        }
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadGroupPoolTask(this, mGroup, page);
        mTask.execute(mOAuth);
    }

    /**
     * Load the last viewed group from storage for when the fragment gets
     * destroyed.
     */
    public void loadGroup() {
        SharedPreferences sp = mActivity.getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String groupId = sp.getString(
                KEY_GROUP_FRAGMENT_GROUP_ID, null);
        if (groupId != null) {
            mGroup = new Group();
            mGroup.setId(groupId);
            if (Constants.DEBUG) Log.d(getLogTag(), "Restored mGroup");
        } else {
            Log.e(getLogTag(), "Could not restore mGroup");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGroup != null) {
            SharedPreferences sp = mActivity.getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(KEY_GROUP_FRAGMENT_GROUP_ID,
                    mGroup.getId());
            editor.commit();
        }
    }

    @Override
    public String getNewestPhotoId() {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        String newestId = prefs.getString(KEY_NEWEST_GROUPPOOL_PHOTO_ID, null);
        return newestId;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
        SharedPreferences prefs = mActivity.getSharedPreferences(Constants
                .PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_NEWEST_GROUPPOOL_PHOTO_ID, photo.getId());
        editor.commit();
        if (Constants.DEBUG)
            Log.d(getLogTag(), "Updated most recent grouppool photo id to " +
                photo.getId());
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}