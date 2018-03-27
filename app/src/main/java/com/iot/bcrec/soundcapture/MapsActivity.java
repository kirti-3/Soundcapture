package com.iot.bcrec.soundcapture;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DatabaseReference rootRef;
    final ArrayList<LatLng> listas = null;
    TileProvider mProvider;
    TileOverlay mOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
       // addHeatMap();


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
      //  addHeatMap();
        mMap = googleMap;
        LatLng sydney = new LatLng(37, -122);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    public void addHeatMap()
    {
       // readItems();
    }

    public void readItems()
        {

            rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = rootRef.child("RAW DATA");
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren())
                    {
                        Log.i("kks12345",ds.getKey());
                        Double Lat = Double.parseDouble(String.valueOf(ds.child("latitude").getValue()));
                        Double Lng = Double.parseDouble(String.valueOf(ds.child("longitude").getValue()));
                        Log.i("kks12345",Lat.toString());
                        Log.i("kks12345",Lng.toString());
                         LatLng latt= new LatLng(Lat,Lng);
                         listas.add(latt);

                         Log.i("kks12345", latt.toString());

                    }
                    Log.i("kks12345", listas.toString());
                    mProvider = new HeatmapTileProvider.Builder()
                            .data(listas)
                            .build();
                    // Add a tile overlay to the map, using the heat map tile provider.
                    mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }



    }

