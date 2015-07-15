package cs446.leviathan.mydestination;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.location.LocationManager;

/**
 * Created by nause on 14/06/15.
 */
public class MyLocation {
    LocationManager lm;

    LocationResult locationResult;
    boolean gps_enabled=false;
    boolean network_enabled=false;
    AsyncTask<Context, Void, Void> mtask;

    public boolean getLocation(Context context, LocationResult result)
    {
        locationResult = result;
        if(lm==null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try{gps_enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        if(!gps_enabled && !network_enabled)
            return false;

        if(gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GpsListener);
        if(network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, NetworkListener);

        mtask= new GetLastLocation();
        mtask.execute();
        return true;
    }

    LocationListener GpsListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mtask.cancel(true);
            locationResult.locationCallback(location);
            lm.removeUpdates(this);
            lm.removeUpdates(NetworkListener);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener NetworkListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            mtask.cancel(true);
            locationResult.locationCallback(location);
            lm.removeUpdates(this);
            lm.removeUpdates(GpsListener);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private Context mContext;
    public MyLocation(Context c) { this.mContext = c; }


    class GetLastLocation extends AsyncTask<Context, Void, Void>
    {

        //ProgressDialog dialog = new ProgressDialog(mContext);

        protected void onPreExecute()
        {
            //dialog.setMessage("Searching....");
            //dialog.show();
        }

        protected Void doInBackground(Context... params)
        {
            Handler mHandler = new Handler(Looper.getMainLooper());



            // ...
            mHandler.post(new Runnable() {
                public void run() {
                    lm.removeUpdates(GpsListener);
                    lm.removeUpdates(NetworkListener);

                    Location net_loc=null, gps_loc=null;
                    if(gps_enabled)
                        gps_loc=lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(network_enabled)
                        net_loc=lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    //if there are both values use the latest one
                    /*if(gps_loc!=null && net_loc!=null){
                        if(gps_loc.getTime() > net_loc.getTime())
                            locationResult.locationCallback(gps_loc);
                        else
                            locationResult.locationCallback(net_loc);
                        return;
                    }*/

                    if(gps_loc != null){
                        locationResult.locationCallback(gps_loc);
                        return;
                    }
                    if(net_loc != null){
                        locationResult.locationCallback(net_loc);
                        return;
                    }
                    locationResult.locationCallback(null);
                }
            });
            // ...


            return null;
        }

        protected void onPostExecute(final Void unused)
        {
            //dialog.dismiss();
        }
    }

    public static abstract class LocationResult {
        public abstract void locationCallback(Location location);
    }
}
