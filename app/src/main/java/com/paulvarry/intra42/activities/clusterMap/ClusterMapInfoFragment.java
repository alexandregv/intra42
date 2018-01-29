package com.paulvarry.intra42.activities.clusterMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.paulvarry.intra42.R;
import com.paulvarry.intra42.adapters.ListAdapterClusterMapInfo;
import com.paulvarry.intra42.api.ApiService;
import com.paulvarry.intra42.api.model.Projects;
import com.paulvarry.intra42.api.model.ProjectsUsers;
import com.paulvarry.intra42.api.model.UsersLTE;
import com.paulvarry.intra42.utils.Theme;
import com.paulvarry.intra42.utils.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClusterMapInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ClusterMapInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClusterMapInfoFragment extends Fragment implements AdapterView.OnItemSelectedListener, TextWatcher, View.OnClickListener, AdapterView.OnItemClickListener {

    private ClusterMapActivity activity;

    private ListAdapterClusterMapInfo adapter;

    private TextView textViewClusters;
    private TextView textViewLayer;
    private Spinner spinnerMain;
    private Spinner spinnerSecondary;
    private ExpandableHeightListView listView;
    private EditText editText;
    private Button buttonUpdate;
    private ViewGroup layoutLoading;

    private OnFragmentInteractionListener mListener;

    public ClusterMapInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ClusterMapFragment.
     */
    public static ClusterMapInfoFragment newInstance() {
        return new ClusterMapInfoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (ClusterMapActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cluster_map_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.listView);
        spinnerMain = view.findViewById(R.id.spinnerMain);
        spinnerSecondary = view.findViewById(R.id.spinnerSecondary);
        textViewClusters = view.findViewById(R.id.textViewClusters);
        textViewLayer = view.findViewById(R.id.textViewLayer);
        editText = view.findViewById(R.id.editText);
        buttonUpdate = view.findViewById(R.id.buttonUpdate);
        layoutLoading = view.findViewById(R.id.layoutLoading);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ListAdapterClusterMapInfo(getContext(), new ArrayList<>(activity.clusters.clusterInfoList.values()));
        listView.setAdapter(adapter);
        listView.setExpanded(true);

        textViewClusters.setTextColor(Theme.getColorAccent(getContext()));
        textViewLayer.setTextColor(Theme.getColorAccent(getContext()));

        editText.addTextChangedListener(this);
        buttonUpdate.setOnClickListener(this);
        listView.setOnItemClickListener(this);

        int layerSelection = activity.clusters.layerStatus.getId();
        spinnerMain.setSelection(layerSelection);
        spinnerMain.setOnItemSelectedListener(this);
        activity.layerTmpStatus = activity.clusters.layerStatus;

        setMainLayerTmpSelection(layerSelection);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateButton() {
        buttonUpdate.setEnabled(true);
        buttonUpdate.setText(R.string.cluster_map_info_button_update);
        if (!isLayerChanged()) {
            buttonUpdate.setEnabled(false);
            buttonUpdate.setText(R.string.cluster_map_info_button_update_disabled);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent == spinnerMain) {
            setMainLayerTmpSelection(position);
        }

    }

    void setMainLayerTmpSelection(int position) {
        spinnerSecondary.setVisibility(View.GONE);
        editText.setVisibility(View.GONE);
        switch (position) {
            case 0:
                activity.layerTmpStatus = ClusterMapActivity.LayerStatus.FRIENDS;
                break;
            case 1:
                activity.layerTmpStatus = ClusterMapActivity.LayerStatus.USER_HIGHLIGHT;
                editText.setVisibility(View.VISIBLE);
                editText.setHint(R.string.cluster_map_info_layer_input_login);
                editText.setText(activity.layerTmpLogin);
                break;
            case 2:
                activity.layerTmpStatus = ClusterMapActivity.LayerStatus.COALITIONS;
                break;
            case 3:
                activity.layerTmpStatus = ClusterMapActivity.LayerStatus.PROJECT;
                editText.setVisibility(View.VISIBLE);
                spinnerSecondary.setVisibility(View.VISIBLE);
                editText.setHint(R.string.cluster_map_info_layer_input_project);
                editText.setText(activity.layerTmpProjectSlug);
                break;
        }
        updateButton();
    }

    /**
     * Return true if the layer settings have changed
     *
     * @return layer settings changed
     */
    boolean isLayerChanged() {
        return activity.clusters.layerStatus != activity.layerTmpStatus ||
                (activity.clusters.layerStatus == ClusterMapActivity.LayerStatus.USER_HIGHLIGHT && !activity.clusters.layerLogin.contentEquals(activity.layerTmpLogin)) ||
                (activity.clusters.layerStatus == ClusterMapActivity.LayerStatus.PROJECT && !activity.clusters.layerProjectSlug.contentEquals(activity.layerTmpProjectSlug));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (activity.layerTmpStatus == ClusterMapActivity.LayerStatus.USER_HIGHLIGHT)
            activity.layerTmpLogin = String.valueOf(s);
        else if (activity.layerTmpStatus == ClusterMapActivity.LayerStatus.PROJECT)
            activity.layerTmpProjectSlug = String.valueOf(s);
        updateButton();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onClick(View v) {
        layoutLoading.setVisibility(View.VISIBLE);

        spinnerMain.setEnabled(false);
        spinnerSecondary.setEnabled(false);
        editText.setEnabled(false);
        buttonUpdate.setClickable(false);
        buttonUpdate.setEnabled(false);

        switch (activity.layerTmpStatus) {
            case USER_HIGHLIGHT:
                activity.applyLayerUser(activity.layerTmpLogin);
                end();
                break;
            case FRIENDS:
                activity.applyLayerFriends();
                end();
                break;
            case PROJECT:
                findProject();
                break;
        }
    }

    void findProject() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    List<Projects> projects = null;
                    final ApiService api = activity.app.getApiService();
                    Response<List<Projects>> response = api.getProjectsSearch(editText.getText().toString()).execute();
                    if (Tools.apiIsSuccessfulNoThrow(response))
                        projects = response.body();

                    final List<Projects> finalProjects = projects;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectDialog(finalProjects);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    void selectDialog(final List<Projects> projects) {

        if (projects == null || projects.size() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Not found");
            builder.setMessage("No project found for: " + editText.getText().toString());
            builder.setPositiveButton(R.string.ok, null);
            AlertDialog alert = builder.create();
            alert.show();
            end();
            return;
        } else if (projects.size() == 1) {
            editText.setText(projects.get(0).slug);
            buttonProject(projects.get(0).slug);
            return;
        }

        final CharSequence[] items = new CharSequence[projects.size()];
        int i = 0;
        for (Projects project : projects) {
            items[i] = project.name;
            ++i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                buttonProject(projects.get(item).slug);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                end();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    void buttonProject(final String slug) {
        final List<ProjectsUsers> list = new ArrayList<>();
        final int pageSize = 30;

        editText.setText(slug);
        activity.layerTmpProjectSlug = slug;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int id = 0;

                    while (id < activity.clusters.locations.size()) {
                        String ids = UsersLTE.concatIds(new ArrayList<>(activity.clusters.locations.values()), id, pageSize);
                        final ApiService api = activity.app.getApiService();
                        Response<List<ProjectsUsers>> response = api.getProjectsUsers(slug, ids, pageSize, 1).execute();
                        if (!Tools.apiIsSuccessfulNoThrow(response))
                            return;
                        list.addAll(response.body());
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        id += pageSize;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.applyLayerProject(list, slug);
                        end();
                    }
                });
            }
        }).start();
    }

    void end() {
        layoutLoading.setVisibility(View.GONE);
        listView.invalidate();
        adapter.notifyDataSetChanged();

        spinnerMain.setEnabled(true);
        spinnerSecondary.setEnabled(true);
        editText.setEnabled(true);
        buttonUpdate.setClickable(true);
        buttonUpdate.setEnabled(true);

        updateButton();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        activity.viewPager.setCurrentItem(position + 1, true);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}