package com.wzx.lightmodule;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.ArrayMap;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wangzhenxing on 16/10/10.
 */

public class ModuleManager extends ModuleGroup {

    private static final String FRAGMENT_TAG_LIFECYCLE = "lifecycle";

    private Handler mHandler = new Handler();

    private ArrayMap<String, RefreshTask> mPendingRefreshTasks = new ArrayMap<String, RefreshTask>();

    public ModuleManager(Context context, ViewGroup container) {
        super(context, container);
    }

    public void bindLifecycle(FragmentActivity activity) {
        bindLifecycle(activity.getSupportFragmentManager());
    }

    public void bindLifecycle(Fragment fragment) {
        bindLifecycle(fragment.getChildFragmentManager());
    }

    private void bindLifecycle(FragmentManager fm) {
        LifecycleFragment current = (LifecycleFragment) fm.findFragmentByTag(FRAGMENT_TAG_LIFECYCLE);
        if (current == null) {
            current = new LifecycleFragment();
            fm.beginTransaction().add(current, FRAGMENT_TAG_LIFECYCLE).commitNowAllowingStateLoss();
        }
        current.addModuleManager(this);
    }

    @Override
    public void requestModulesRefresh(Object... targets) {
        String targetsKey = getTargetsKey(targets);
        RefreshTask task = mPendingRefreshTasks.remove(targetsKey);
        if (task != null) {
            mHandler.removeCallbacks(task);
        }
        RefreshTask newTask = new RefreshTask(targets);
        mPendingRefreshTasks.put(targetsKey, newTask);
        mHandler.post(newTask);
    }

    public void refresh() {
        performPreRefreshModuleTree();
        super.refresh();
    }

    private final class RefreshTask implements Runnable {

        private Object[] mTargets;

        private RefreshTask(Object... targets) {
            mTargets = targets;
        }

        @Override
        public void run() {
            performPreRefreshModuleTree();

            String targetsKey = getTargetsKey(mTargets);
            mPendingRefreshTasks.remove(targetsKey);
            refresh(mTargets);
        }
    }

    private String getTargetsKey(Object... targets) {
        if (targets == null || targets.length == 0) {
            return "";
        }
        List<String> targetKeys = new ArrayList<>();
        for (Object target : targets) {
            targetKeys.add(target.toString());
        }
        Collections.sort(targetKeys);
        StringBuilder stringBuilder = new StringBuilder();
        for (String key : targetKeys) {
            stringBuilder.append(key).append("_");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    void start() {
        start(true);
    }

    void resume() {
        resume(true);
    }

    void pause() {
        pause(true);
    }

    void stop() {
        stop(true);
    }

}
