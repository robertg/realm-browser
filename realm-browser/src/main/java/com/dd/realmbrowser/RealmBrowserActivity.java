package com.dd.realmbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.dd.realmbrowser.utils.MagicUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

public class RealmBrowserActivity extends AppCompatActivity {

    static final String EXTRAS_REALM_FILE_NAME = "EXTRAS_REALM_FILE_NAME";
    static final String EXTRAS_REALM_MODEL_INDEX = "REALM_MODEL_INDEX";
    static final int FIELDS_PER_PAGE = 3;

    private Class<? extends RealmObject> mRealmObjectClass;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private List<Field> mFieldsList;



    public static void start(Activity activity, int realmModelIndex, String realmFileName) {
        Intent intent = new Intent(activity, RealmBrowserActivity.class);
        intent.putExtra(EXTRAS_REALM_MODEL_INDEX, realmModelIndex);
        intent.putExtra(EXTRAS_REALM_FILE_NAME, realmFileName);
        activity.startActivity(intent);
    }



    public static void start(Activity activity, String realmFileName) {
        Intent intent = new Intent(activity, RealmBrowserActivity.class);
        intent.putExtra(EXTRAS_REALM_FILE_NAME, realmFileName);
        activity.startActivity(intent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_realm_browser_viewpager_container);

        String realmFileName = getIntent().getStringExtra(EXTRAS_REALM_FILE_NAME);
        Integer realmModelIndex = getIntent().getExtras().containsKey(EXTRAS_REALM_MODEL_INDEX) ? getIntent().getIntExtra(EXTRAS_REALM_MODEL_INDEX, 0) : null;

        Realm mRealm;
        RealmConfiguration config = new RealmConfiguration.Builder(this).name(realmFileName).build();
        mRealm = Realm.getInstance(config);

        if (realmModelIndex != null) {
            mRealmObjectClass = RealmBrowser.getInstance().getRealmModelList().get(realmModelIndex);
        } else {
            Field field = RealmHolder.getInstance().getField();
            if (MagicUtils.isParameterizedField(field)) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Class<?> pTypeClass = (Class<?>) pType.getActualTypeArguments()[0];
                mRealmObjectClass = (Class<? extends RealmObject>) pTypeClass;
            }
        }

        mFieldsList = new ArrayList<>();
        mFieldsList.addAll(Arrays.asList(mRealmObjectClass.getDeclaredFields()));

        int numPages = (int) Math.ceil(((double) mFieldsList.size()) / (double) FIELDS_PER_PAGE);

        mRealm.close();

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), numPages, realmFileName, realmModelIndex);
        mPager.setAdapter(mPagerAdapter);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_columns) {
            // TODO
        }
        if (id == R.id.action_settings) {
            SettingsActivity.start(this);
        }
        return super.onOptionsItemSelected(item);
    }



    private static class ViewPagerAdapter extends FragmentStatePagerAdapter {

        private int numPages;
        private String realmFileName;
        private Integer realmModelIndex;



        public ViewPagerAdapter(FragmentManager fm, int numPages, String realmFileName, Integer realmModelIndex) {
            super(fm);
            this.numPages = numPages;
            this.realmFileName = realmFileName;
            this.realmModelIndex = realmModelIndex;
        }



        @Override
        public Fragment getItem(int position) {
            return RealmBrowserFragment.newInstance(realmFileName, realmModelIndex, position);
        }



        @Override
        public int getCount() {
            return numPages;
        }
    }
}
