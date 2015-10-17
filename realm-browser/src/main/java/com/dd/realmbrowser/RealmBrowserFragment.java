package com.dd.realmbrowser;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dd.realmbrowser.utils.L;
import com.dd.realmbrowser.utils.MagicUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;

import static com.dd.realmbrowser.RealmBrowserActivity.FIELDS_PER_PAGE;

public class RealmBrowserFragment extends Fragment implements RealmAdapter.Listener {

    private static final String EXTRAS_REALM_FILE_NAME = "EXTRAS_REALM_FILE_NAME";
    private static final String EXTRAS_REALM_MODEL_INDEX = "REALM_MODEL_INDEX";
    private static final String EXTRAS_VIEW_PAGER_PAGE = "VIEW_PAGER_PAGE";

    private Realm mRealm;
    private Class<? extends RealmObject> mRealmObjectClass;
    private AbstractList<? extends RealmObject> mRealmObjects;
    private RealmAdapter mAdapter;
    private TextView mTxtIndex;
    private TextView mTxtColumn1;
    private TextView mTxtColumn2;
    private TextView mTxtColumn3;
    private List<Field> mVisibleFieldList;
    private List<Field> mFieldsList;



    public static RealmBrowserFragment newInstance(String realmFileName, @Nullable Integer realmModelIndex, int viewPagerPage) {
        RealmBrowserFragment myFragment = new RealmBrowserFragment();

        Bundle args = new Bundle();
        args.putInt(EXTRAS_VIEW_PAGER_PAGE, viewPagerPage);
        args.putString(EXTRAS_REALM_FILE_NAME, realmFileName);
        if (realmModelIndex != null)
            args.putInt(EXTRAS_REALM_MODEL_INDEX, realmModelIndex);
        myFragment.setArguments(args);

        return myFragment;
    }



    @Nullable
    public static RealmList<? extends RealmObject> invokeMethod(Object realmObject, String methodName) {
        RealmList<? extends RealmObject> result = null;
        try {
            Method method = realmObject.getClass().getMethod(methodName);
            result = (RealmList<? extends RealmObject>) method.invoke(realmObject);
        } catch (NoSuchMethodException e) {
            L.e(e.toString());
        } catch (InvocationTargetException e) {
            L.e(e.toString());
        } catch (IllegalAccessException e) {
            L.e(e.toString());
        }

        return result;

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String realmFileName = this.getArguments().getString(EXTRAS_REALM_FILE_NAME);
        if (realmFileName == null)
            throw new IllegalArgumentException("No Realm file name given.");

        RealmConfiguration config = new RealmConfiguration.Builder(getActivity())
                .name(realmFileName)
                .build();
        mRealm = Realm.getInstance(config);

        if (this.getArguments().containsKey(EXTRAS_REALM_MODEL_INDEX)) {
            int index = this.getArguments().getInt(EXTRAS_REALM_MODEL_INDEX, 0);
            mRealmObjectClass = RealmBrowser.getInstance().getRealmModelList().get(index);
            mRealmObjects = mRealm.allObjects(mRealmObjectClass);
        } else {
            RealmObject object = RealmHolder.getInstance().getObject();
            Field field = RealmHolder.getInstance().getField();
            String methodName = MagicUtils.createMethodName(field);
            mRealmObjects = invokeMethod(object, methodName);
            if (MagicUtils.isParameterizedField(field)) {
                ParameterizedType pType = (ParameterizedType) field.getGenericType();
                Class<?> pTypeClass = (Class<?>) pType.getActualTypeArguments()[0];
                mRealmObjectClass = (Class<? extends RealmObject>) pTypeClass;
            }
        }

        mVisibleFieldList = new ArrayList<>();
        mFieldsList = new ArrayList<>();
        mFieldsList.addAll(Arrays.asList(mRealmObjectClass.getDeclaredFields()));
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.frg_realm_browser, container, false);

        mAdapter = new RealmAdapter(getActivity(), mRealmObjects, mVisibleFieldList, this);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        mTxtIndex = (TextView) rootView.findViewById(R.id.txtIndex);
        mTxtColumn1 = (TextView) rootView.findViewById(R.id.txtColumn1);
        mTxtColumn2 = (TextView) rootView.findViewById(R.id.txtColumn2);
        mTxtColumn3 = (TextView) rootView.findViewById(R.id.txtColumn3);

        selectFieldsForCurrentPage();
        updateColumnTitle(mVisibleFieldList);

        return rootView;
    }



    @Override
    public void onResume() {
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }



    @Override
    public void onDestroy() {
        if (mRealm != null) {
            mRealm.close();
        }
        super.onDestroy();
    }



    @Override
    public void onRowItemClicked(@NonNull RealmObject realmObject, @NonNull Field field) {
        RealmHolder.getInstance().setObject(realmObject);
        RealmHolder.getInstance().setField(field);
        String realmFileName = this.getArguments().getString(EXTRAS_REALM_FILE_NAME);
        RealmBrowserActivity.start(getActivity(), realmFileName);
    }



    private void selectFieldsForCurrentPage() {
        mVisibleFieldList.clear();
        int currentPage = getArguments().getInt(EXTRAS_VIEW_PAGER_PAGE);
        int index = FIELDS_PER_PAGE * currentPage;
        while (mVisibleFieldList.size() < FIELDS_PER_PAGE && index < mFieldsList.size()) {
            mVisibleFieldList.add(mFieldsList.get(index++));
        }
    }



    private void updateColumnTitle(List<Field> columnsList) {
        mTxtIndex.setText("#");

        LinearLayout.LayoutParams layoutParams2 = createLayoutParams();
        LinearLayout.LayoutParams layoutParams3 = createLayoutParams();

        if (columnsList.size() > 0) {
            mTxtColumn1.setText(columnsList.get(0).getName());

            if (columnsList.size() > 1) {
                mTxtColumn2.setText(columnsList.get(1).getName());
                layoutParams2.weight = 1;

                if (columnsList.size() > 2) {
                    mTxtColumn3.setText(columnsList.get(2).getName());
                    layoutParams3.weight = 1;
                } else {
                    layoutParams3.weight = 0;
                }
            } else {
                layoutParams2.weight = 0;
            }
        }

        mTxtColumn2.setLayoutParams(layoutParams2);
        mTxtColumn3.setLayoutParams(layoutParams3);
    }



    private LinearLayout.LayoutParams createLayoutParams() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}