package com.erikligai.doctorplzsaveme.RecordFragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.erikligai.doctorplzsaveme.Models.Problem;
import com.erikligai.doctorplzsaveme.Models.Record;
import com.erikligai.doctorplzsaveme.R;
import com.erikligai.doctorplzsaveme.backend.Backend;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeolocationFragment extends Fragment implements OnMapReadyCallback {

    private int problem_index, record_index;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private LatLng geolocation;
    private Problem problem;
    private Record record;
    private TextView emptyView;

    public GeolocationFragment(){}

    public static GeolocationFragment newInstance(int problem_index, int record_index) {
        GeolocationFragment fragment = new GeolocationFragment();
        Bundle args = new Bundle();
        args.putInt("problem_index", problem_index);
        args.putInt("record_index",record_index);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_record_four, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        problem_index = getArguments().getInt("problem_index");
        record_index = getArguments().getInt("record_index");
        problem = Backend.getInstance().getPatientProblems().get(problem_index);
        record = problem.getRecords().get(record_index);
        geolocation = record.getGeolocation();

        emptyView = view.findViewById(R.id.empty_location_view);

        checkEmpty();
        return view;
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

    //TODO: Get onClick LatLng.

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng edmonton = new LatLng(53.5444, -113.4909);
        if (geolocation != null) {
            mMap.addMarker(new MarkerOptions().position(geolocation).title(problem.getTitle()).snippet(record.getTitle()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geolocation,15));
            //mMap.animateCamera(CameraUpdateFactory.zoomIn());

            //mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(edmonton,15));
            //mMap.animateCamera(CameraUpdateFactory.zoomIn());
            //mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        }

    }

    public void checkEmpty(){
        if (geolocation == null) {
            mapFragment.getView().setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            mapFragment.getView().setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

}
