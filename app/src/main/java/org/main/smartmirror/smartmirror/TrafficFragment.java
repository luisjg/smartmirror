package org.main.smartmirror.smartmirror;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fragment that handles the traffic information
 */
public class TrafficFragment extends Fragment {
    private Preferences mPreferance;
    private String mCurrentLat;
    private String mCurrentLong;
    private String mWorkLat;
    private String mWorkLong;
    private TextView txtDistance;
    private TextView txtCurrent;
    private TextView txtDelays;


    Handler mHandler = new Handler();
// https://maps.googleapis.com/maps/api/distancematrix/json?origins=34.0636439,-118.2593811&destinations=34.2370851,-118.5272547&departure_time=now&traffic_model=best_guess&key=AIzaSyBumZObXEyI5_7Ie0u8ZnrRKAXzojKpDw8
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferance = Preferences.getInstance(getActivity());
        mCurrentLat = Double.toString(mPreferance.getLatitude());
        mCurrentLong = Double.toString(mPreferance.getLongitude());

        //csun
        mWorkLat = "34.2370851";
        mWorkLong = "-118.5272547";

        //somewhere in la
        mWorkLat = "34.051144";
        mWorkLong = "-118.2366286";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_fragment, container, false);
        txtDistance = (TextView) view.findViewById(R.id.traffic_distance);
        txtCurrent = (TextView) view.findViewById(R.id.traffic_current);
        txtDelays = (TextView) view.findViewById(R.id.traffic_delay);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startTrafficUpdate();
    }

    private void startTrafficUpdate(){
        String distanceMatrixRequest = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s,%s&destinations=%s,%s&departure_time=now&traffic_model=best_guess&units=%s&key=%s";
        String distanceMatrixKey = "AIzaSyBumZObXEyI5_7Ie0u8ZnrRKAXzojKpDw8";
        String distanceMatrixUnit = "metric";
        if(mPreferance.getWeatherUnits().equals(Preferences.ENGLISH)){
            distanceMatrixUnit = "imperial";
        }
        updateTrafficData(String.format(distanceMatrixRequest, mCurrentLat, mCurrentLong, mWorkLat, mWorkLong, distanceMatrixUnit, distanceMatrixKey));

    }

    private void renderTraffic(JSONObject json){
        try {
            JSONObject data = json.getJSONArray("rows")
                    .getJSONObject(0)
                    .getJSONArray("elements")
                    .getJSONObject(0);
            double tripTime = Double.parseDouble(data.getJSONObject("duration").getString("text").substring(0, 2));
            double tripTimeTraffic = Double.parseDouble(data.getJSONObject("duration_in_traffic").getString("text").substring(0,2));
            txtDistance.setText("Distance to work: " + data.getJSONObject("distance").getString("text").substring(0,2) + " miles");
            txtCurrent.setText("Normal travel time: " + tripTime + " minutes");
            txtDelays.setText("Current travel time: " + tripTimeTraffic + " minutes");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateTrafficData(final String request){
        new Thread(){
            public void run(){
                final JSONObject json = FetchURL.getJSON(request);
                if(json == null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    "error no traffic found",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            renderTraffic(json);
                        }
                    });
                }
            }
        }.start();
    }
}