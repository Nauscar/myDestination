package cs446.leviathan.mydestination;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs446.leviathan.mydestination.cardstream.Card;


/**
 * Created by nause on 24/05/15.
 */
public class GoogleMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String ARG_SECTION_NUMBER = "section_number";

    private GoogleApiClient mGoogleApiClient = null;
    MapView mMapView;
    private GoogleMap map;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private float mBearing = 0;

    private ArrayList<Marker> mMarkers = new ArrayList<Marker>();

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
        map = mMapView.getMap();
        map.getUiSettings().setScrollGesturesEnabled(false);
        map.setMyLocationEnabled(true);

        buildGoogleApiClient();

        return view;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void removeMarker(Card c){
        for(int i = 0; i < mMarkers.size(); ++i){
            if (mMarkers.get(i).getTitle().equals(c.getTag())) {
                mMarkers.get(i).remove();
                mMarkers.remove(i);
            }
        }
    }

    public void addMarker(Card c){
        Marker tmp = map.addMarker(new MarkerOptions()
                        .position(c.getPlace().getLatLng())
                        .draggable(false)
                        .title(c.getTag())
        );
        mMarkers.add(tmp);
        updateMap();
    }

    public void updateMap(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null || map == null)
            return;

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));

        if(mLastLocation.hasBearing())
            mBearing = mLastLocation.getBearing();

        if(mMarkers.size() > 0){
            CameraPosition previous = map.getCameraPosition();
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            bounds.include(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            for(Marker tmp : mMarkers){
                bounds.include(tmp.getPosition());
            }
            LatLngBounds latLngBounds = bounds.build();
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));
            float zoom_level = map.getCameraPosition().zoom;

            map.moveCamera(CameraUpdateFactory.newCameraPosition(previous));

            CameraPosition cameraLatLng = new CameraPosition.Builder()
                    .target(bounds.build().getCenter())
                    .bearing(mBearing)
                    .tilt(40)
                    .zoom(zoom_level)
                    .build();
            //Perform the tilt!
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraLatLng));

            /*ArrayList<LatLng> sorted = new ArrayList<LatLng>();
            LatLng origin = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            for(Marker tmp : mMarkers){
                for(int i = 0; i < sorted.size(); ++i){
                    if(distance(origin, sorted.get(i)) > distance(origin, sorted.get(i))){
                        sorted.add(i, tmp.getPosition());
                        break;
                    }
                    else if(i == sorted.size() - 1){
                        sorted.add(tmp.getPosition());
                        break;
                    }
                }
            }

            for(int i = 0; i < sorted.size() - 1; ++i) {
                LatLng fromPosition = sorted.get(i);
                LatLng toPosition = sorted.get(i+1);

                GMapV2Direction md = new GMapV2Direction();

                Document doc = md.getDocument(fromPosition, toPosition, GMapV2Direction.MODE_WALKING);
                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(3).color(Color.RED);

                for (int j = 0; j < directionPoint.size(); ++j) {
                    rectLine.add(directionPoint.get(i));
                }

                map.addPolyline(rectLine);
            }*/
        } else {
            CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))      // Sets the center of the map to location user
                    .bearing(mBearing)                // Sets the orientation of the camera to east
                    .tilt(40)
                    .zoom(17);

            CameraPosition cameraPosition = cameraPositionBuilder.build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    public double distance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    public GoogleMap getGoogleMap(){
        return map;
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

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        updateMap();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        Toast.makeText(getActivity(), "Location currently unavailable.",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onConnectionSuspended(int cause){
        //Move along, nothing to see here.
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateMap();
    }
}