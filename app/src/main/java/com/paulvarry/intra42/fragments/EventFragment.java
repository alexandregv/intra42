package com.paulvarry.intra42.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paulvarry.intra42.AppClass;
import com.paulvarry.intra42.R;
import com.paulvarry.intra42.api.ApiService;
import com.paulvarry.intra42.api.ServiceGenerator;
import com.paulvarry.intra42.api.model.Events;
import com.paulvarry.intra42.api.model.EventsUsers;
import com.paulvarry.intra42.utils.Calendar;
import com.paulvarry.intra42.utils.DateTool;
import com.paulvarry.intra42.utils.Tag;
import com.paulvarry.intra42.utils.Tools;
import com.veinhorn.tagview.TagView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_EVENT = "event";

    Button buttonSubscribe;
    LinearLayout linearLayoutProgress;
    ProgressBar progressBarButton;

    AppClass appClass;
    ApiService api;
    Call<List<EventsUsers>> listCallEventsUsers;
    private Events event;
    private EventsUsers eventsUsers;
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                //dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    private Callback<List<EventsUsers>> callback = new Callback<List<EventsUsers>>() {
        @Override
        public void onResponse(Call<List<EventsUsers>> call, Response<List<EventsUsers>> response) {

            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
            eventsUsers = null;
            if (response.isSuccessful()) {
                if (response.body() != null && !response.body().isEmpty())
                    eventsUsers = response.body().get(0);

                setButtonSubscribe();
                if (call.request().method().equals("DELETE"))
                    Toast.makeText(getContext(), R.string.event_unsubscribed, Toast.LENGTH_SHORT).show();

                Calendar.syncEventCalendarAfterSubscription(getContext(), event, eventsUsers);
            }
        }

        @Override
        public void onFailure(Call<List<EventsUsers>> call, Throwable t) {
            Context context = getContext();
            if (context != null)
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
        }
    };

    private Callback<Void> callbackDelete = new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {

            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
            eventsUsers = null;
            if (response.isSuccessful()) {

                setButtonSubscribe();
                if (call.request().method().equals("DELETE"))
                    Toast.makeText(getContext(), R.string.event_unsubscribed, Toast.LENGTH_SHORT).show();
                Calendar.syncEventCalendarAfterSubscription(getContext(), event, eventsUsers);
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            Context context = getContext();
            if (context != null)
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
        }
    };

    private Callback<EventsUsers> callbackSubscribe = new Callback<EventsUsers>() {
        @Override
        public void onResponse(Call<EventsUsers> call, Response<EventsUsers> response) {

            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
            eventsUsers = null;
            if (response.isSuccessful()) {
                eventsUsers = response.body();
                setButtonSubscribe();
                Toast.makeText(getContext(), R.string.event_subscribed, Toast.LENGTH_SHORT).show();
            }
            Calendar.syncEventCalendarAfterSubscription(getContext(), event, eventsUsers);
        }

        @Override
        public void onFailure(Call<EventsUsers> call, Throwable t) {
            Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            linearLayoutProgress.setVisibility(View.INVISIBLE);
            progressBarButton.setVisibility(View.GONE);
            buttonSubscribe.setEnabled(true);
        }
    };

    private OnFragmentInteractionListener mListener;

    public EventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param event The event to build this fragment.
     * @return A new instance of fragment EventFragment.
     */
    public static EventFragment newInstance(Events event) {
        return newInstance(ServiceGenerator.getGson().toJson(event));
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param json The json to build this fragment.
     * @return A new instance of fragment EventFragment.
     */
    public static EventFragment newInstance(String json) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT, json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = ServiceGenerator.getGson().fromJson(getArguments().getString(ARG_EVENT), Events.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (event != null)
            setView(view);
    }

    private void setView(View contentView) {

        appClass = (AppClass) getActivity().getApplication();
        api = appClass.getApiService();

        TextView textViewTitle = contentView.findViewById(R.id.textViewTitle);
        TagView tagViewKind = contentView.findViewById(R.id.tagViewKind);
        TextView textViewDate = contentView.findViewById(R.id.textViewDate);
        TextView textViewTime = contentView.findViewById(R.id.textViewTime);
        LinearLayout linearLayoutPlace = contentView.findViewById(R.id.linearLayoutPlace);
        TextView textViewPlace = contentView.findViewById(R.id.textViewPlace);
        TextView textViewPeople = contentView.findViewById(R.id.textViewPeople);
        TextView textViewDescription = contentView.findViewById(R.id.textViewDescription);
        buttonSubscribe = contentView.findViewById(R.id.buttonSubscribe);
        linearLayoutProgress = contentView.findViewById(R.id.linearLayoutProgress);
        progressBarButton = contentView.findViewById(R.id.progressBarButton);

        if (tagViewKind != null && textViewTitle != null) {
            Tag.setTagEvent(event, tagViewKind);
            textViewTitle.setText(event.name);
            textViewTitle.setBackgroundColor(tagViewKind.getTagColor());
        }

        String date = DateTool.getTodayTomorrow(getContext(), event.beginAt, true);

        if (DateTool.sameDayOf(event.beginAt, event.endAt)) {
            date += DateTool.getDateLong(event.beginAt);
            textViewDate.setText(date);
            String time = DateTool.getTimeShort(event.beginAt) + " - " + DateTool.getTimeShort(event.endAt);
            textViewTime.setText(time);
        } else {
            date += DateTool.getDateTimeLong(event.beginAt);
            textViewDate.setText(date);
            String time = DateTool.getDateTimeLong(event.endAt);
            textViewTime.setText(time);
        }

        if (event.location == null || event.location.isEmpty())
            linearLayoutPlace.setVisibility(View.GONE);
        else {
            linearLayoutPlace.setVisibility(View.VISIBLE);
            textViewPlace.setText(event.location);
        }

        String people;
        if (event.maxPeople == 0)
            people = getString(R.string.event_subscription_unavailable);
        else
            people = String.valueOf(event.nbrSubscribers) + " / " + String.valueOf(event.maxPeople);
        textViewPeople.setText(people);

        Tools.setMarkdown(getContext(), textViewDescription, event.description);

        if (false) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
            CoordinatorLayout.Behavior behavior = params.getBehavior();

            if (behavior != null && behavior instanceof BottomSheetBehavior) {
                ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            }
        }

        progressBarButton.setVisibility(View.GONE);
        buttonSubscribe.setEnabled(false);
        linearLayoutProgress.setVisibility(View.VISIBLE);
        listCallEventsUsers = api.getEventsUsers(appClass.me.id, event.id);
        listCallEventsUsers.enqueue(callback);

        buttonSubscribe.setOnClickListener(this);
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

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        progressBarButton.setVisibility(View.VISIBLE);
        buttonSubscribe.setEnabled(false);
        if (eventsUsers == null)
            api.createEventsUsers(event.id, appClass.me.id).enqueue(callbackSubscribe);
        else
            api.deleteEventsUsers(eventsUsers.id).enqueue(callbackDelete);
    }

    void setButtonSubscribe() {
        linearLayoutProgress.setVisibility(View.INVISIBLE);
        progressBarButton.setVisibility(View.GONE);
        buttonSubscribe.setEnabled(true);

        if (eventsUsers == null) {
            if (DateTool.isInFuture(event.beginAt)) {
                buttonSubscribe.setEnabled(true);
                buttonSubscribe.setText(R.string.event_subscribe);
            } else {
                buttonSubscribe.setEnabled(false);
                buttonSubscribe.setText(R.string.event_subscription_unavailable);
            }
        } else {
            buttonSubscribe.setText(R.string.event_unsubscribe);
            if (DateTool.isInFuture(event.beginAt))
                buttonSubscribe.setEnabled(true);
            else
                buttonSubscribe.setEnabled(false);
            Calendar.syncEventCalendarAfterSubscription(getContext(), event, eventsUsers);
        }

        if (eventsUsers == null && event.nbrSubscribers >= event.maxPeople) {

            if (event.maxPeople == 0)
                buttonSubscribe.setText(R.string.event_subscription_unavailable);
            else
                buttonSubscribe.setText(R.string.event_full);
            buttonSubscribe.setEnabled(false);
        }
    }

    public void setEvent(Events event) {
        this.event = event;
        setView(getView());
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