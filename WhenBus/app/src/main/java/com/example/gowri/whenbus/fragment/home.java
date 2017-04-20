package com.example.gowri.whenbus.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.gowri.whenbus.Home;
import com.example.gowri.whenbus.LoginActivity;
import com.example.gowri.whenbus.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static java.sql.Types.NULL;


public class home extends Fragment implements LocationListener{

    private OnFragmentInteractionListener mListener;

    private EditText search;

    //Parallax
    private TextView stickyView;
    private ListView listView;
    private TextView heroImageView;

    private View stickyViewSpacer;

    private int MAX_ROWS = 20;

    private View v;

    private ProgressDialog pDialog;
    private RequestQueue queue;

    private boolean success;
    private boolean req=false;
    private boolean dest_done = false;
    private boolean search_done = false;



    LocationManager locationManager;
    String provider;
    Criteria criteria;
    public static double dest_lon,dest_lat,user_lat,user_lon;

    public static double nearby_lat,nearby_lon;
    public static String nearby_name;
    public static String nearby_id;

    public static JSONArray buslist;

    public static boolean background_start = false;

    public home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_home, container, false);

        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

//
//        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//        }

        search = (EditText)v.findViewById(R.id.search);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.search || actionId== EditorInfo.IME_NULL) {
                    if(event.getAction() == KeyEvent.ACTION_DOWN) {

                        //Get user current location
                        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                        criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        provider = locationManager.getBestProvider(criteria, false);
                        if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                                        ,10);

                        locationManager.requestLocationUpdates(5, 0, criteria, home.this, null);

                        // Progress
                        Home home = (Home)getActivity();
                        home.setprogress(true,"Loading...");


                        //HTTP request for lat and lng of destination
                        destination dest = new destination(v.getText().toString());
                        dest.execute((Void) null);

                    }


                }
                return false;
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    private void loadparallax(Boolean success){

        if(!success){

            FrameLayout frame = (FrameLayout) v.findViewById(R.id.search_frame);
            if(frame.getVisibility()==View.VISIBLE){
                frame.setVisibility(View.INVISIBLE);
            }

            return;
        }

        FrameLayout frame = (FrameLayout) v.findViewById(R.id.search_frame);
        if(frame.getVisibility()==View.INVISIBLE){
            frame.setVisibility(View.VISIBLE);
        }

        /* Initialise list view, hero image, and sticky view */
        listView = (ListView) v.findViewById(R.id.listView);
        heroImageView = (Button) v.findViewById(R.id.heroImageView);
        stickyView = (TextView) v.findViewById(R.id.stickyView);

        /* Inflate list header layout */
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View listHeader = inflater.inflate(R.layout.list_header, null);
        stickyViewSpacer = listHeader.findViewById(R.id.stickyViewPlaceholder);

        heroImageView.setText("Nearby stop:\n"+nearby_name);
        heroImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home home = (Home)getActivity();
                home.loadmap();
                return;
            }
        });



        if(listView.getHeaderViewsCount()!=0){
            View old = listView.findViewWithTag("Oldheader");
            if(old!=null){
                listView.removeHeaderView(old);
            }
        }

        listHeader.setTag("Oldheader");
        /* Add list view header */
        listView.addHeaderView(listHeader);

        /* Handle list View scroll events */
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* Check if the first item is already reached to top.*/
                if (listView.getFirstVisiblePosition() == 0) {
                    View firstChild = listView.getChildAt(0);
                    int topY = 0;
                    if (firstChild != null) {
                        topY = firstChild.getTop();
                    }

                    int heroTopY = stickyViewSpacer.getTop();
                    stickyView.setY(Math.max(0, heroTopY + topY));

                    /* Set the image to scroll half of the amount that of ListView */
                    heroImageView.setY(topY * 0.5f);
                }
            }
        });


        /* Populate the ListView with sample data */
        List<String> modelList = new ArrayList<>();

        {


            for (int i = buslist.length()-1; i >=0; i--) {
                try {
                    modelList.add("Bus No " + buslist.getJSONObject(i).getString("bus_no").replace("_f"," ").replace("_b"," ") +"\nETA "+buslist.getJSONObject(i).getString("arrival_time")+" hrs");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ArrayAdapter adapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.list_row,modelList);
            listView.setAdapter(adapter);

            //Stop progress
            Home home = (Home) getActivity();
            home.setprogress(false,"");

            //speed detect
            home.startbackground();

        }



    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private class search extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            success= false;

            String url = "https://cs3410-whenbus.herokuapp.com/bus";
            JSONObject user_log = new JSONObject();

            try {
                user_log.put("gps_lat_u", user_lat);
                user_log.put("gps_lon_u", user_lon);
                user_log.put("gps_lat_d", dest_lat);
                user_log.put("gps_lon_d", dest_lon);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("Search_query_input",user_log.toString());

            JsonObjectRequest login_post_req = new JsonObjectRequest(url, user_log, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    Log.d("Search_query_output",jsonObject.toString());
                    try {
                        if (jsonObject.getString("success").equals("true")){
                            nearby_lat = jsonObject.getJSONObject("message").getDouble("stop_lat");
                        nearby_lon = jsonObject.getJSONObject("message").getDouble("stop_lon");
                        nearby_name = jsonObject.getJSONObject("message").getString("stop_name");
                        nearby_id = jsonObject.getJSONObject("message").getString("stop_id");
                        buslist = jsonObject.getJSONObject("message").getJSONArray("bus_details");
                        Log.d("Search_results", nearby_name);
                        Log.d("Search_results", buslist.toString());
                        success = true;
                    }else  if(jsonObject.getJSONObject("message").getString("msg").equals("6")){
                            Home home = (Home) getActivity();
                            home.setprogress(false,"");
                            Toast.makeText(getActivity().getApplicationContext(),"No busses found!",Toast.LENGTH_LONG).show();
                        }else if(jsonObject.getJSONObject("message").getString("msg").equals("7")){
                            Home home = (Home) getActivity();
                            home.setprogress(false,"");
                            Toast.makeText(getActivity().getApplicationContext(),"No nearby stop found!",Toast.LENGTH_LONG).show();
                        } else if(jsonObject.getJSONObject("message").getString("msg").equals("8")){
                            Home home = (Home) getActivity();
                            home.setprogress(false,"");
                            Toast.makeText(getActivity().getApplicationContext(),"Destination not found!",Toast.LENGTH_LONG).show();
                        } else if(jsonObject.getJSONObject("message").getString("msg").equals("9")){
                            Home home = (Home) getActivity();
                            home.setprogress(false,"");
                            Toast.makeText(getActivity().getApplicationContext(),"Route not found",Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {

                    }
                    req=true;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("Onerror", "volley");
                    Home home = (Home) getActivity();
                    home.setprogress(false,"");
                    Toast.makeText(getActivity().getApplicationContext(),"Network error!",Toast.LENGTH_LONG).show();
                    req=true;
                }
            });

            login_post_req.setTag(LoginActivity.class.getSimpleName());
            queue.add(login_post_req);


            while (!req){
            }
            req=false;
            Log.i("Test1","hey");
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.d("Search_over",success.toString());
            loadparallax(success);

        }

        @Override
        protected void onCancelled() {
            ;
        }
    }

    private class destination extends AsyncTask<Void, Void, Boolean> {

        private String dest;

        destination(String dest) {
            this.dest = dest;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
            }

            Location location = locationManager.getLastKnownLocation(provider);
            if(location==null) {
                Toast.makeText(getContext().getApplicationContext(),"Couldnt detect location",Toast.LENGTH_LONG).show();
                dest = "no";
                return false;
            }else{
                user_lat = location.getLatitude();
                user_lon = location.getLongitude();
            }


            success = false;
            req = false;

            dest = dest.replace(" ","%20");
            String url = "http://maps.google.com/maps/api/geocode/json?address="+dest+"&sensor=false";

            JsonObjectRequest get_dest_lat_lon = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    {
                        Log.d("Destination",jsonObject.toString());
                        try {
                                dest_lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lat");
                                dest_lon = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                                        .getJSONObject("geometry").getJSONObject("location")
                                        .getDouble("lng");
                                success = true;
                                Log.d("Destination","hahahaha");

                        } catch (JSONException e) {
                            success = false;
                        }

                    }
                    req=true;
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("Volley", "Destination");
                    req=true;
                }
            });

            get_dest_lat_lon.setTag(Home.class.getSimpleName());
            queue.add(get_dest_lat_lon);

            while (!req){
                ;
            }
            req=false;
            Log.d("Destination", String.valueOf(success));
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                search task = new search();
                task.execute((Void)null);
            }else if(!dest.equals("no")){
                Home home = (Home) getActivity();
                home.setprogress(false,"");
                Toast.makeText(getActivity().getApplicationContext(),"Destination not found",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            ;
        }
    }


}