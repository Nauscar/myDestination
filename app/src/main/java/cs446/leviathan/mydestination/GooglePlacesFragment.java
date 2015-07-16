package cs446.leviathan.mydestination;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cs446.leviathan.mydestination.cardstream.CardStreamFragment;
import cs446.leviathan.mydestination.cardstream.CardStreamState;
import cs446.leviathan.mydestination.cardstream.OnCardClickListener;
import cs446.leviathan.mydestination.cardstream.StreamRetentionFragment;

/**
 * Created by nause on 24/05/15.
 */
public class GooglePlacesFragment extends CardStreamFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String FRAGTAG = "PlacePickerFragment";
    private static final String RETENTION_TAG = "retention";

    /**
     * Default empty constructor.
     */
    public GooglePlacesFragment(){
        super();
    }

    /**
     * Static factory method
     * @param sectionNumber
     * @return
     */
    public static GooglePlacesFragment newInstance(int sectionNumber) {
        GooglePlacesFragment fragment = new GooglePlacesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        PlacePickerFragment fragment = (PlacePickerFragment) fm.findFragmentByTag(FRAGTAG);

        if (fragment == null) {
            FragmentTransaction transaction = fm.beginTransaction();
            fragment = new PlacePickerFragment();
            transaction.add(fragment, FRAGTAG);
            transaction.commit();
        }

        // Use fragment as click listener for cards, but must implement correct interface
        if (!(fragment instanceof OnCardClickListener)) {
            throw new ClassCastException("PlacePickerFragment must " +
                    "implement OnCardClickListener interface.");
        }
        OnCardClickListener clickListener = (OnCardClickListener) fm.findFragmentByTag(FRAGTAG);

        ((MainActivity) getActivity()).mRetentionFragment = (StreamRetentionFragment) fm.findFragmentByTag(RETENTION_TAG);
        if (((MainActivity) getActivity()).mRetentionFragment == null) {
            ((MainActivity) getActivity()).mRetentionFragment = new StreamRetentionFragment();
            fm.beginTransaction().add(((MainActivity) getActivity()).mRetentionFragment, RETENTION_TAG).commit();
        } else {
            // If the retention fragment already existed, we need to pull some state.
            // pull state out
            CardStreamState state = ((MainActivity) getActivity()).mRetentionFragment.getCardStream();

            // dump it in CardStreamFragment.
            ((MainActivity) getActivity()).mCardStreamFragment =
                    (CardStreamFragment) fm.findFragmentById(R.id.fragment_cardstream);
            ((MainActivity) getActivity()).mCardStreamFragment.restoreState(state, clickListener);
        }
        return view;
    }
}
