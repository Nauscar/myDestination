package cs446.leviathan.mydestination;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;


/**
 * Created by nause on 24/05/15.
 */
public class GoogleMapFragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final int CAMERA_REQUEST = 1888;

    MapView mMapView;
    private GoogleMap googleMap;
    private MyLocation myLocation;

    /**
     * Default empty constructor.
     */
    public GoogleMapFragment(){
        super();
    }

    /**
     * Static factory method
     * @param sectionNumber
     * @return
     */
    public static GoogleMapFragment newInstance(int sectionNumber) {
        GoogleMapFragment fragment = new GoogleMapFragment();
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
        View view = inflater.inflate(R.layout.fragment_googlemap, container, false);
        mMapView = (MapView) view.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        googleMap = mMapView.getMap();
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.setMyLocationEnabled(true);

        Button photoButton = (Button) view.findViewById(R.id.camera_button);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        myLocation = new MyLocation(getActivity().getApplicationContext());
        myLocation.getLocation(getActivity().getApplicationContext(), ((MainActivity) getActivity()).locationResult);

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ImageView imageView = (ImageView) getView().findViewById(R.id.thumbnail);
            imageView.setImageBitmap(photo);
            myLocation.getLocation(getActivity().getApplicationContext(), ((MainActivity) getActivity()).locationResult);
        }
    }

    public GoogleMap getGoogleMap(){
        return googleMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
