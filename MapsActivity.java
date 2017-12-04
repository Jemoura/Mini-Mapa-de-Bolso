package com.atmanx.lab.minhalocalizacao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.support.annotation.BoolRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button btn_editar;
    Button btn_gps;
    private static final int REQUEST_PERMISSIONS = 100; // apenas foi definida uma constante qualquer, para não ficar amarrado em um numero
    boolean boolean_permission;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    Double latitude=new Double(0), longitude=new Double(0);
    Geocoder geocoder;

    BitmapDescriptor myLocationPNGIcon;

    boolean isUpdatingPos = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();

        String filename = "iconsuserlocation";
        int id = getResources().getIdentifier(filename, "drawable", getPackageName());
        myLocationPNGIcon = BitmapDescriptorFactory.fromResource(id);

        btn_gps = (Button) findViewById(R.id.btn_gps);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolean_permission) {


                    if (!isUpdatingPos) {

                        isUpdatingPos = true;

                        Intent intent = new Intent(getApplicationContext(), ServicoLocalizacao.class);
                        startService(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }

            }
        });


        btn_editar = (Button) findViewById(R.id.btn_editar);
        btn_editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextScreen = new Intent(getApplicationContext(), SecondScreenActivity.class);

                //Sending data to another Activity
                nextScreen.putExtra("lat", latitude.toString());
                nextScreen.putExtra("lng", longitude.toString());

                // starting new activity
                startActivity(nextScreen);
            }
        });

        fn_permission();
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    private int ix=0;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Double newLatitude, newLongitude;

            //Toast.makeText(getApplicationContext(), "Location Received", Toast.LENGTH_SHORT).show();

            newLatitude = Double.valueOf(intent.getStringExtra("latitude"));
            newLongitude = Double.valueOf(intent.getStringExtra("longitude"));

            List<Address> addresses = null;

            try {
                // nao usado para encontrar city, country etc
                addresses = geocoder.getFromLocation(newLatitude, newLongitude, 1);
                //String cityName = addresses.get(0).getAddressLine(0);
                //String stateName = addresses.get(0).getAddressLine(1);
                //String countryName = addresses.get(0).getAddressLine(2);

                //tv_area.setText(addresses.get(0).getAdminArea());
                //tv_locality.setText(stateName);
                //tv_address.setText(countryName);



            } catch (IOException e1) {
                e1.printStackTrace();
            }




            if( !(newLatitude.intValue() == latitude.intValue() && newLongitude.intValue() == longitude.intValue()) ) {

                // Add a marker in Sydney and move the camera
                LatLng novo = new LatLng(newLatitude, newLongitude);
                /*CircleOptions circleOpt = new CircleOptions();
                circleOpt.center(novo);
                circleOpt.radius(1);
                mMap.addCircle(circleOpt);*/
                mMap.addMarker(new MarkerOptions().position(novo).title("Sua Localização").
                        icon(myLocationPNGIcon));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(novo));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                //tv_latitude.setText(latitude+"");
                //tv_longitude.setText(longitude+"");
                //tv_address.getText();

                latitude = newLatitude;
                longitude = newLongitude;

            }else {

                //.makeText(getApplicationContext(), "Same Locations", Toast.LENGTH_SHORT).show();

            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(ServicoLocalizacao.str_receiver));

        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            ArrayList<String> locationsList;
            if((locationsList = bundle.getStringArrayList("SearchResults")) != null){

                addLocations(locationsList);

            }
            else{

                String name = bundle.getString("name");
                latitude = Double.valueOf(intent.getStringExtra("latitude"));
                longitude = Double.valueOf(intent.getStringExtra("longitude"));

                LatLng newLocat = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(newLocat).title(name));

            }

        }

    }

    private void addLocations(ArrayList<String> list){

        int index = 0;
        while(index < list.size())
        {
            String name = list.get(index);
            index++;
            latitude = Double.valueOf(list.get(index));
            index++;
            longitude = Double.valueOf(list.get(index));
            index++;

            LatLng newLocat = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(newLocat).title(name));

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
