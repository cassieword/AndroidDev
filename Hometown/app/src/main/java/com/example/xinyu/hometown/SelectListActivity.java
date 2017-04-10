package com.example.xinyu.hometown;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by xinyu on 3/19/17.
 */

public class SelectListActivity extends AppCompatActivity implements ListView.OnScrollListener, UserListFragment.OnUserSelectedListener, Spinner.OnItemSelectedListener{
    UserListFragment userList;
    List<UserInfo> userInfoList;
    List<UserInfo> filteredUserInfoList;
    Spinner countrySpinner;
    Spinner stateSpinner;
    Spinner yearSpinner;
    RequestQueue queue;
    String[] countryArray;
    String countrySelected = "all";
    String[] stateArray;
    String stateSelected = "all";
    ArrayList<String> yearArray;
    String yearSelected = "all";
    ListView tempListView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_list);
        countrySpinner = (Spinner)findViewById(R.id.spinner_country);
        countrySpinner.setOnItemSelectedListener(this);
        stateSpinner = (Spinner)findViewById(R.id.spinner_state);
        stateSpinner.setOnItemSelectedListener(this);
        yearSpinner = (Spinner)findViewById(R.id.spinner_year);
        yearSpinner.setOnItemSelectedListener(this);
        filteredUserInfoList = new ArrayList<UserInfo>();
        userInfoList = new ArrayList<UserInfo>();
        getUserInfoRequest();
        getCountryRequest();
        getYearRequest();
    }
    int currentPage = 0;
    boolean loading = false;
    public void getUserInfoRequest() {
        Log.i("rew", "Start");
        if(loading) {
            return;
        }
        loading = true;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.d("rew", response.toString());
                FragmentManager fm = getFragmentManager();
                userInfoList = new ArrayList<UserInfo>();
                Gson gson = new Gson();
                if (response != null) {
                    int len = response.length();
                    UserInfo[] users = gson.fromJson(response.toString(), UserInfo[].class);
                    for (int i = 0; i < len; i++) {
                        userInfoList.add(users[i]);
                    }
                }
                if (fm.findFragmentById(R.id.selected_list) == null) {
                    userList = new UserListFragment();
                    userList.userInfos = userInfoList;
                    fm.beginTransaction().add(R.id.selected_list, userList).commit();
                } else {
                    userList.userInfos.addAll(userInfoList);
                    userInfoList = userList.userInfos;
                    userList.reload();
                }
                currentPage += 1;
                loading = false;
            } };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
                loading = false;
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/users?page=" + currentPage + "&reverse=true";
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }
    ////////
    public void userListViewCreated() {
        tempListView  = userList.getListView();
        tempListView.setOnScrollListener(this);
    }
    private int preLast;

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                && (tempListView.getLastVisiblePosition() - tempListView.getHeaderViewsCount() -
                tempListView.getFooterViewsCount()) >= (userInfoList.size() - 1)) {
            System.out.println("Test user list view");
            getUserInfoRequest();
            // Now your listview has hit the bottom
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }

    ////////
    public void getCountryRequest() {
        Log.i("rew", "Start");
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.d("rew", response.toString());
                countryArray = new String[response.length()+1];
                countryArray[0] = "all";
                if (response != null) {
                    int len = response.length();
                    for (int i = 0; i < len; i++) {
                        countryArray[i+1] = response.optString(i);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectListActivity.this, android.R.layout.simple_spinner_item, countryArray);
                countrySpinner.setAdapter(adapter);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }
    public void getStateRequest(String countrySelected) {
        if(countrySelected.equals("all")) {
            stateArray = new String[1];
            stateArray[0] = "all";
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectListActivity.this, android.R.layout.simple_spinner_item, stateArray);
            stateSpinner.setAdapter(adapter);
            return;
        }
        Log.i("rew", "Start");
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.d("rew", response.toString());
                stateArray = new String[response.length()+1];
                stateArray[0] = "all";
                if (response != null) {
                    int len = response.length();
                    for (int i = 0; i < len; i++) {
                        stateArray[i+1] = response.optString(i);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SelectListActivity.this, android.R.layout.simple_spinner_item, stateArray);
                stateSpinner.setAdapter(adapter);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rew", error.toString());
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+countrySelected;
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this); queue.add(getRequest);
    }
    public void getYearRequest() {
        yearArray = new ArrayList<String>();
        yearArray.add("all");
        for (int i = 1970; i <= 2017; i++) {
            yearArray.add(Integer.toString(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearArray);
        yearSpinner.setAdapter(adapter);
    }
    public void onUserSelected(UserInfo ListName) {}
    @Override
    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        if(parentView == countrySpinner) {
            countrySelected = countryArray[position];
            getStateRequest(countrySelected);
        }
        else if(parentView == stateSpinner) {
            stateSelected = stateArray[position];
        }
        else if(parentView == yearSpinner) {
            yearSelected = yearArray.get(position);
        }
        updateData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        public void updateData() {
            filteredUserInfoList.clear();
            for(int i = 0; i < userInfoList.size(); i++) {
                UserInfo userInfo = userInfoList.get(i);
                if((userInfo.country.equals(countrySelected)||countrySelected.equals("all"))&& (userInfo.state.equals(stateSelected)||stateSelected.equals("all"))
                        && (userInfo.year.equals(yearSelected)||yearSelected.equals("all"))){
                    filteredUserInfoList.add(userInfo);
            }
        }
        if(userList != null && filteredUserInfoList != null) {
            userList.updateData(filteredUserInfoList);
        }
    }
}
