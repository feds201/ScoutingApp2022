package com.sukhesh.scoutingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sukhesh.scoutingapp.api.BlueAllianceAPI;
import com.sukhesh.scoutingapp.storage.JSONStorage;

import org.json.JSONException;


public class Home extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*
         * Set up the RecyclerView (essentially a list of items) for the list of quals
         */
        // access to this view (see fragment_home.xml)
        // TODO: create a button / input to achieve this!
        View homeView;
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            homeView = inflater.inflate(R.layout.fragment_home, container, false);
        } else {
            homeView = inflater.inflate(R.layout.fragment_home_phone, container, false);
        }

        SharedPreferences sp = requireContext().getSharedPreferences("matches", Context.MODE_PRIVATE);
        new Thread(() -> {
            String eventCode = sp.getString("eventCode", "2021mibg");
            String rawMatches = BlueAllianceAPI.RequestMatchesByEventCode(eventCode, "5ED1uRm7sTzNCXRwuSyPUnFt3uFuDVpO0lZKFQplA2EjCOsqwSWNzQpqwTTRM2ba");
            Log.d("rawMatches", rawMatches);
            BlueAllianceAPI.SendRawMatchesToSharedPreferences(sp, rawMatches); // TODO: possibly error prone based on how long the request takes....
        }).start();

        String rawMatches = BlueAllianceAPI.GetRawMatchesFromSharedPreferences(sp);
        // TODO: ideally the color code would come from a LIST or a DROPDOWN so no one messes it up
        String colorCode = sp.getString("colorCode", "R1");
        Log.d("raw", String.valueOf(rawMatches.length()));
        Log.d("raw", colorCode);
        String[] matches = BlueAllianceAPI.returnMatchListFromRequestString(rawMatches, colorCode);
//        for(String s: matches) {
//            Log.d("matches", s);
//        }

        // access to the view's recycler view object
        RecyclerView qualsList = homeView.findViewById(R.id.rvQuals);
        // set the layout for the recycler
        qualsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        /*
         * Instantiate Match list from quals.xml using Matches object
         */
        // collect the matches from strings.xml

        // if you already loaded the matches, then don't overwrite data
        String rawJSONValue = sp.getString("json", "");
        if (rawJSONValue.equals("")) {
            try {
                JSONStorage.addMatches(sp, matches);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /*
         * Set up what happens when a Qual is pressed
         */
        QualsRecyclerViewAdapter qualsAdapter = new QualsRecyclerViewAdapter(getActivity(), JSONStorage.GetListOfMatches(sp));
        // use our adaptor, see QualsRecyclerViewAdapter, to interact with the Quals RecycleView
        qualsList.setAdapter(qualsAdapter);
        qualsList.setItemAnimator(new DefaultItemAnimator());

        qualsAdapter.setClickListener(position -> {
            String matchName = qualsAdapter.getItem(position);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("currentMatch", matchName);
            editor.apply();

            FragmentTransaction fr = getParentFragmentManager().beginTransaction();
            fr.replace(R.id.body_container, new RapidReactInput());
            fr.commit();
        });
//
        return homeView;

    }
}
