package com.example.sagar.cabook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class WelcomeScreen extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;


    private static final int MY_PERMISSION_REQUEST_CODE=100;
    private static final int PLAY_SERVICE_REQUEST_CODE=101;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL=5000;
    private static int FASTEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        location_switch=findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if(isOnline)
                {
                    startLocationUpdate();
                    displayLocation();
                    Snackbar.make(mapFragment.getView(),"You Are Online",Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    stopLocationUpdate();
                    mCurrent.remove();
                    Snackbar.make(mapFragment.getView(),"You are Offline",Snackbar.LENGTH_LONG).show();
                }
            }
        });
        //geoFire
        drivers= FirebaseDatabase.getInstance().getReference("DRIVERS");
        geoFire=new GeoFire(drivers);

        setUpLocation();
    }

    private void setUpLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                if(location_switch.isChecked())
                {
                    displayLocation();
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void createLocationRequest()
    {
        mLocationRequest= new LocationRequest();
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient()
    {
        mGoogleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).
                addApi(LocationServices.API).
                build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices()
    {
        int resultCode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode!=ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_REQUEST_CODE).show();
            }
            else
            {
                Toast.makeText(this, "This Device is not supported", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdate()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,(com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    buildGoogleApiClient();
                    createLocationRequest();
                    if(location_switch.isChecked())
                    {
                        displayLocation();
                    }
                }
        }
    }

    private void displayLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation!=null)
        {
            if(location_switch.isChecked())
            {
                final double latlitude=mLastLocation.getLatitude();
                final double longtitude=mLastLocation.getLongitude();
                //updating firebase
                geoFire.setLocation(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()), new GeoLocation(latlitude, longtitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error)
                    {
                        //adding marker
                        if(mCurrent!=null)
                        {
                            mCurrent.remove();
                        }
                        mCurrent=mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car1)).position(new LatLng(latlitude,longtitude)).title("You"));
                        //zooming camera at marker

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latlitude,longtitude),15.0f));
                        //animation addition to rotate marker
                        rotateMarker(mCurrent,-360,mMap);
                    }
                });
            }
        }
        else
        {
            Log.d("Erorr","Can not get your Location");
        }
    }

    private void rotateMarker(final Marker mCurrent, final int i, GoogleMap mMap)
    {
        final Handler handler=new Handler();
        final long start= SystemClock.uptimeMillis();
        final float startRotation=mCurrent.getRotation();
        final long duration=1500;

        final Interpolator interpolator=new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed=SystemClock.uptimeMillis()-start;
                float t=interpolator.getInterpolation((float)elapsed/duration);
                float rot=t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if(t<1.0)
                {
                    handler.postDelayed(this,16);
                }
            }
        });
    }

    private void startLocationUpdate()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

    }

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation=location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        displayLocation();
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
