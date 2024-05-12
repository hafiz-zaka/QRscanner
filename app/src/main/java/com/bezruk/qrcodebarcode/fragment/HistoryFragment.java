package com.bezruk.qrcodebarcode.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bezruk.qrcodebarcode.R;
import com.bezruk.qrcodebarcode.activity.ResultActivity;
import com.bezruk.qrcodebarcode.adapter.HistoryAdapter;
import com.bezruk.qrcodebarcode.data.constant.Constants;
import com.bezruk.qrcodebarcode.data.preference.AppPreference;
import com.bezruk.qrcodebarcode.data.preference.PrefKey;
import com.bezruk.qrcodebarcode.utility.ActivityUtils;
import com.bezruk.qrcodebarcode.utility.AdManager;
import com.bezruk.qrcodebarcode.utility.AppUtils;
import com.bezruk.qrcodebarcode.utility.DialogUtils;
import com.bezruk.qrcodebarcode.utility.ResultOfTypeAndValue;
import com.google.android.gms.ads.AdListener;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {

    private Activity mActivity;
    private Context mContext;

    private TextView noResultView;
    private RecyclerView mRecyclerView;

    public ArrayList<String> getArrayList() {
        return arrayList;
    }

    private ArrayList<String> arrayList, arrayDateList, arrayColorList;
    private HistoryAdapter adapter;
    private ImageButton deleteAll;
    private TextView scanned_btn, created_btn;
    private int current_history_btn = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);

        initView(rootView);
        initFunctionality();
        initListener();

        //Обновление списка с главного активити
        /*((MainActivity) getActivity()).setFragmentRefreshListener(new MainActivity.FragmentRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("REFRESHH", Integer.toString(current_history_btn));
                arrayList.clear();
                if(current_history_btn==0) {
                    arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED));
                } else {
                    arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_CREATED));
                }
                Collections.reverse(arrayList);
                refreshList();
            }
        });*/

        return rootView;
    }

    private void initVar() {
        mActivity = getActivity();
        if (mActivity != null) {
            mContext = mActivity.getApplicationContext();
        }
    }

    private void initView(View rootView) {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        noResultView = (TextView) rootView.findViewById(R.id.noResultView);
        deleteAll = (ImageButton) rootView.findViewById(R.id.deleteAll);
        scanned_btn = (TextView) rootView.findViewById(R.id.scanned_res_btn);
        created_btn = (TextView) rootView.findViewById(R.id.created_res_btn);

        //full ads load
        AdManager.getInstance(mContext).loadFullScreenAd(mActivity,getString(R.string.admobe_intertesial_history));
    }

    public void initFunctionality() {
        arrayList = new ArrayList<>();
        arrayDateList = new ArrayList<>();
        arrayColorList = new ArrayList<>();
        adapter = new HistoryAdapter(mContext, arrayList, arrayDateList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(adapter);

        arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED));
        Collections.reverse(arrayList);
        arrayDateList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_SCANNED));
        Collections.reverse(arrayDateList);
        arrayColorList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED));
        Collections.reverse(arrayColorList);
        refreshList();
    }

    private void initListener() {
        adapter.setClickListener(new HistoryAdapter.ClickListener() {
            @Override
            public void onDeleteClicked(final int position) {
                // AppUtils.copyToClipboard(mContext, arrayList.get(position));
                DialogUtils.showDialogPrompt(mActivity, null, getString(R.string.delete_message_item),
                        getString(R.string.yes), getString(R.string.no), true, new DialogUtils.DialogActionListener() {
                            @Override
                            public void onPositiveClick() {
                                delete(position);
                            }
                        });
            }

            @Override
            public void onItemClicked(final int position) {
                ResultOfTypeAndValue resultValues = AppUtils.getResourceType(arrayList.get(position));
                // TODO Sample fullscreen Ad implementation

                AdManager.getInstance(mContext).showFullScreenAd(requireActivity(), new AdManager.onAdShowListener() {
                    @Override
                    public void onAdShow() {
                        if (current_history_btn == 0) {
                            ActivityUtils.getInstance().invokeActivity(mActivity, ResultActivity.class, false, Constants.HISTORY_SCAN_FRAGMENT, position);
                        } else {
                            ActivityUtils.getInstance().invokeActivity(mActivity, ResultActivity.class, false, Constants.HISTORY_GENERATE_FRAGMENT, position);
                        }
                    }
                });
                // fullscreen ad show


            }

            @Override
            public void onItemLongClicked(final int position) {
                /*DialogUtils.showDialogPrompt(mActivity, null, getString(R.string.delete_message_item),
                        getString(R.string.yes), getString(R.string.no), true, new DialogUtils.DialogActionListener() {
                            @Override
                            public void onPositiveClick() {
                                delete(position);
                            }
                        });*/
            }

        });

        scanned_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanned_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                created_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                current_history_btn = 0;

                arrayList.clear();
                arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED));
                Collections.reverse(arrayList);

                arrayDateList.clear();
                arrayDateList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_SCANNED));
                Collections.reverse(arrayDateList);

                arrayColorList.clear();
                arrayColorList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED));
                Collections.reverse(arrayColorList);

                refreshList();
            }
        });

        created_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanned_btn.setBackgroundColor(getResources().getColor(R.color.grey));
                created_btn.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                current_history_btn = 1;

                arrayList.clear();
                arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_CREATED));
                Collections.reverse(arrayList);

                arrayDateList.clear();
                arrayDateList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_CREATED));
                Collections.reverse(arrayDateList);

                arrayColorList.clear();
                arrayColorList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_CREATED));
                Collections.reverse(arrayColorList);

                refreshList();
            }
        });


        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogUtils.showDialogPrompt(mActivity, null, getString(R.string.delete_message_all),
                        getString(R.string.yes), getString(R.string.no), true, new DialogUtils.DialogActionListener() {
                            @Override
                            public void onPositiveClick() {
                                deleteAll();
                            }
                        });
            }
        });
    }

    private void delete(int position) {
        if (current_history_btn == 0) {
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_SCANNED, null);
            arrayList.remove(position);
            Collections.reverse(arrayList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_SCANNED, arrayList);
            Collections.reverse(arrayList);

            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_SCANNED, null);
            arrayDateList.remove(position);
            Collections.reverse(arrayDateList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_SCANNED, arrayDateList);
            Collections.reverse(arrayDateList);

            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_SCANNED, null);
            arrayColorList.remove(position);
            Collections.reverse(arrayColorList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_SCANNED, arrayColorList);
            Collections.reverse(arrayColorList);

            refreshList();
        } else {
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_CREATED, null);
            arrayList.remove(position);
            Collections.reverse(arrayList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_CREATED, arrayList);
            Collections.reverse(arrayList);

            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_CREATED, null);
            arrayDateList.remove(position);
            Collections.reverse(arrayDateList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_CREATED, arrayDateList);
            Collections.reverse(arrayDateList);

            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_CREATED, null);
            arrayColorList.remove(position);
            Collections.reverse(arrayColorList);
            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_CREATED, arrayColorList);
            Collections.reverse(arrayColorList);

            refreshList();
        }
    }

    private void deleteAll() {
        if (current_history_btn == 0) {
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_SCANNED, null);
            arrayList.clear();

            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_SCANNED, null);
            arrayDateList.clear();

            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_SCANNED, null);
            arrayColorList.clear();

            refreshList();
        } else {
            AppPreference.getInstance(mContext).setStringArray(PrefKey.RESULT_LIST_OF_CREATED, null);
            arrayList.clear();

            AppPreference.getInstance(mContext).setStringArray(PrefKey.DATE_LIST_OF_CREATED, null);
            arrayDateList.clear();

            AppPreference.getInstance(mContext).setStringArray(PrefKey.COLOR_LIST_OF_CREATED, null);
            arrayColorList.clear();

            refreshList();
        }
    }

    public void refreshList() {
        if (arrayList.isEmpty()) {
            noResultView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            deleteAll.setVisibility(View.GONE);
        } else {
            noResultView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            deleteAll.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            arrayList.clear();
            arrayDateList.clear();
            if (current_history_btn == 0) {
                arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_SCANNED));
                arrayDateList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_SCANNED));
                arrayColorList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_SCANNED));
            } else {
                arrayList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.RESULT_LIST_OF_CREATED));
                arrayDateList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.DATE_LIST_OF_CREATED));
                arrayColorList.addAll(AppPreference.getInstance(mContext).getStringArray(PrefKey.COLOR_LIST_OF_CREATED));
            }
            Collections.reverse(arrayList);
            Collections.reverse(arrayDateList);
            Collections.reverse(arrayColorList);
            refreshList();
        }
    }

}
