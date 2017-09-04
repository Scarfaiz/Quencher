package com.scarfaiz.cluckinbell;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.widget.Button;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends Activity {

    LocationManager locationManager;
    MapView map;
    IMapController mapController;
    public boolean geodata_updated = false;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue("CB");
        setContentView(R.layout.activity_main);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(map);
        map.getOverlays().add(oMapLocationOverlay);
        oMapLocationOverlay.enableFollowLocation();
        oMapLocationOverlay.enableMyLocation();
        oMapLocationOverlay.enableFollowLocation();
        CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        map.getOverlays().add(compassOverlay);
        compassOverlay.getOrientation();
        


        mapController = map.getController();
        mapController.setZoom(9);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
                int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLoc(lastLocation);
                    return;
                }
            }
        });
    }



    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        int permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
            return;}
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        locationManager.removeUpdates(myLocationListener);
    }

    private void updateLoc(Location loc){
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        mapController.setCenter(locGeoPoint);
        map.invalidate();
    }

    private LocationListener myLocationListener
            = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if(!geodata_updated){
                updateLoc(location);
                geodata_updated = true;}
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    };

}
