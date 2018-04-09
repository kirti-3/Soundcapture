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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String tag, node;
    DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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
        rootRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference userRef = rootRef.child("IMEI");
        mMap = googleMap;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    node= String.valueOf(ds.getKey());
                    tag=node;
                    Log.d(MainActivity.TAG,"1"+ node);
                    //Log.i(TAG, String.valueOf(++k));
                    userRef.child(node).limitToLast(1).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            double data2= 0.0;
                            Double Lat = Double.parseDouble(String.valueOf(dataSnapshot.child("latitude").getValue()));
                            Double Lng = Double.parseDouble(String.valueOf(dataSnapshot.child("longitude").getValue()));
                            Double data1 = Double.valueOf(String.valueOf(dataSnapshot.child("Data").getValue()));
                            LatLng newLocation = new LatLng(Lat,Lng);
                            tag = dataSnapshot.getRef().toString();
                            String[] tokens= tag.split("/");
                            String tag1 = tokens[tokens.length-2];
                            Log.d(MainActivity.TAG,"2"+ node);
                            mMap.addMarker(new MarkerOptions()
                                    .position(newLocation)
                                    .title( tag1 + " SoundData =" + data1 ));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation,20));
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*userRef.addChildEventListener(new ChildEventListener() {


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                if(dataSnapshot.child("latitude").getValue()!=null && dataSnapshot.child("longitude").getValue()!=null)
                {
                    Double Lat = Double.parseDouble(String.valueOf(dataSnapshot.child("latitude").getValue()));
                    Double Lng = Double.parseDouble(String.valueOf(dataSnapshot.child("longitude").getValue()));
                    Double data1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Data").getValue()));
                    LatLng newLocation = new LatLng(Lat,Lng);
                    mMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .title(MainActivity.imei + "SoundData=" + data1 ));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation,15));

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Query lastQuery = databaseReference.child(MainActivity.imei).orderByKey().limitToLast(1);
        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data = dataSnapshot.child("Data").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Handle possible errors.
            }
        });


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(23.544, 87.3387);
        mMap.addMarker(new MarkerOptions().position(sydney).title(MainActivity.imei + "SoundData=" +data));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
*/

    }
}
