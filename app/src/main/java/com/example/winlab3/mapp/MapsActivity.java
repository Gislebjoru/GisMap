package com.example.winlab3.mapp;

import android.graphics.Color;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //definere variabler
    private String myFra;
    private String myTil;
    private GoogleMap mMap;
    double fraLat;
    double fraLong;
    double tilLat;
    double tilLong;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //onCreate hentin layout og mapFragment
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //definere geocoder og finne knapp, edittext og textview
        final Geocoder geocoder = new Geocoder(this);
        Button btn = (Button) findViewById(R.id.searchbutton);
        final EditText editFra = (EditText) findViewById(R.id.fraText);
        final EditText editTil = (EditText) findViewById(R.id.tilText);
        final TextView myText = (TextView) findViewById(R.id.wedidit);


        //når knapp trykkes
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(myFra !="" && myTil !=""){

                    //set myFra og myTil til inhold i editTexts
                    myFra = String.valueOf(editFra.getText());
                    myTil = String.valueOf(editTil.getText());
                        try {
                            //hent latitude og longitude fra geocoder med innhold hentet fra myFra og myTil
                            fraLat = geocoder.getFromLocationName(myFra, 1).get(0).getLatitude();
                            fraLong = geocoder.getFromLocationName(myFra, 1).get(0).getLongitude();
                            tilLat = geocoder.getFromLocationName(myTil, 1).get(0).getLatitude();
                            tilLong = geocoder.getFromLocationName(myTil, 1).get(0).getLongitude();
                            //lager LatLng som inneholder lat og lon fra myFra og myTil
                            LatLng fra = new LatLng(fraLat, fraLong);
                            LatLng til = new LatLng(tilLat, tilLong);
                            //fjerner alle markers fra map(kan derfor søke flere ganger uten å ende opp med fler enn 2 markers
                            mMap.clear();
                            //legg til markers på start og sluttpunkt
                            mMap.addMarker(new MarkerOptions().position(fra).title("Start"));
                            mMap.addMarker(new MarkerOptions().position(til).title("Slutt"));
                            //
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(fra));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fra,15));

                            //bruker GoogleDirection med serverkey, definerer innhold og hva vi ønsker å gjøre
                            GoogleDirection.withServerKey("AIzaSyDgMuCPcbDVRdkvY6_W9VNkij4RDRZPYOE")
                                    .from(new LatLng(fraLat, fraLong))
                                    .to(new LatLng(tilLat, tilLong))
                                    .avoid(AvoidType.FERRIES)
                                    .avoid(AvoidType.HIGHWAYS)
                                    .unit(Unit.METRIC)
                                    .transportMode(TransportMode.DRIVING)
                                    .execute(new DirectionCallback()
                                    {
                                        @Override
                                        public void onDirectionSuccess(Direction direction, String rawBody) {
                                            if(direction.isOK()) {
                                                //om directionOK henter den info
                                                Route route = direction.getRouteList().get(0);
                                                Leg leg = route.getLegList().get(0);
                                                String info = String.valueOf(leg.getDistance().getText());
                                                Info durationInfo = leg.getDuration();
                                                String myDur = durationInfo.getText();
                                                //setter textviews til variablen info og myDur
                                                myText.setText("Distanse: "+info+"\n"+"Tid: "+myDur);
                                                //tegner linje fra start til slutt
                                                List<Step> stepList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
                                                ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(getApplicationContext(), stepList, 5, Color.RED, 3, Color.BLUE);
                                                for (PolylineOptions polylineOption : polylineOptionList) {
                                                    mMap.addPolyline(polylineOption);
                                                }
                                            } else {
                                                //om direction ikke er ok
                                                System.out.println("Direction not ok");
                                            }
                                        }
                                        @Override
                                        public void onDirectionFailure(Throwable t) {
                                            //om direction feiler
                                            System.out.println("Directionfailure");
                                        }
                                    });
                            //catch exceptions
                        } catch (IOException ex) {
                            System.out.println(ex);
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println(e);
                        }
                    } else {
                    //søkefeltene er tomme
                        System.out.println("sokefelt er tomme");
                    }
                }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //når map er ferdig lastet inn
        mMap = googleMap;
    }
}
