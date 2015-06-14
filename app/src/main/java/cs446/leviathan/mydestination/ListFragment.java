package cs446.leviathan.mydestination;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
/**
 * Created by nause on 24/05/15.
 */
public class ListFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Default empty constructor.
     */
    public ListFragment(){
        super();
    }

    /**
     * Static factory method
     * @param sectionNumber
     * @return
     */
    public static ListFragment newInstance(int sectionNumber) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * OnCreateView fragment override
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // Trap the google maps button.
        Button mapsButton = (Button) view.findViewById(R.id.test);
        mapsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Launch Google Maps
                        Intent myDestination = new Intent(getActivity(), DestinationActivity.class);
                        startActivity(myDestination);
                    }
                }
        );

        return view;
    }
}
