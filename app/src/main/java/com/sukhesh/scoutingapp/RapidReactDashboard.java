package com.sukhesh.scoutingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sukhesh.scoutingapp.fields.ClosedQuestion;
import com.sukhesh.scoutingapp.fields.FiniteInt;
import com.sukhesh.scoutingapp.storage.JSONStorage;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RapidReactDashboard extends Fragment {
    //Scrollbar jank stuff
    int tvInt = 0;
    int tvInt2 = 0;

    //Stopwatch global variables
    Chronometer stopwatch;
    boolean running = false;
    long stop;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 600) {
            rootView = inflater.inflate(R.layout.fragment_rapid_react_dashboard, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_rapid_react_dashboard_phone, container, false);
        }
        SharedPreferences sp = requireContext().getSharedPreferences("matches", Context.MODE_PRIVATE);
        String matchName = sp.getString("currentMatch", "Q1");
        JSONStorage storage = new JSONStorage(sp);
        TextView title = rootView.findViewById(R.id.title_dashboard);
        String matchType = storage.getString(matchName, "matchType");
        switch (matchType) {
            case "Q":
                title.setText("Qualification " + storage.getInt(matchName,"matchNumber"));
                break;
            case "PO":
                title.setText("Playoff " + storage.getInt(matchName,"matchNumber"));
                break;
            case "SF":
                title.setText("Semi Final " + storage.getInt(matchName,"matchNumber"));
                break;
            case "F":
                title.setText("Final " + storage.getInt(matchName,"matchNumber"));
                break;
        }

        TextView teamNum = rootView.findViewById(R.id.heading_dashboard_teamNum);
        teamNum.setText(String.valueOf(storage.getInt(matchName,"teamNumber")));

        TextView teamColor = rootView.findViewById(R.id.heading_dashboard_teamColor);
        String robotAllianceInfo = storage.getString(matchName,"robotAllianceInfo");
        switch (robotAllianceInfo.charAt(0)) {
            case 'B':
                teamColor.setText("Blue " + robotAllianceInfo.charAt(1));
                break;
            case 'R':
                teamColor.setText("Red " + robotAllianceInfo.charAt(1));
        }

        ArrayList<View> rawViews = new ArrayList<>();
        rootView.findViewsWithText(rawViews, "FiniteInt", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        ArrayList<FiniteInt> finiteInts = FiniteInt.generateArrayListFromViews(rawViews);

        for(FiniteInt f: finiteInts) {
            f.updateValue(storage, matchName);
            f.plus.setOnClickListener(view -> {
                f.value++;
                f.tally.setText(String.valueOf(f.value));

                storage.add(matchName, f.name, f.value);
            });
            f.minus.setOnClickListener(view -> {
                f.value--;
                //if button press is below zero
                if(f.value < 0) {
                    Toast.makeText(getActivity(), "You cannot go below 0!", Toast.LENGTH_SHORT).show();
                    f.value++;
                } else {
                    f.tally.setText(String.valueOf(f.value));
                    storage.add(matchName, f.name, f.value);
                }
            });
        }

        rawViews.clear();
        rootView.findViewsWithText(rawViews, "ClosedQuestion", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        ArrayList<ClosedQuestion> checkboxes = ClosedQuestion.generateArrayListFromViews(rawViews);
        for(ClosedQuestion c: checkboxes) {
            c.updateValue(storage, matchName);
            c.check.setOnClickListener(view -> {
                c.value = c.check.isChecked();
                storage.add(matchName, c.name, c.value);
            });
        }

        //clever way to make clicking on the text check the checkbox (We can use it if we find problems trying to click the checkbox)
        /*
        TextView leavesTarmacTitle = rootView.findViewById(R.id.leavesTarmacTitle);
        CheckBox checkBox = rootView.findViewById(R.id.leavesTarmac);
        leavesTarmacTitle.setOnClickListener(new View.OnClickListener() {
            int i = 0;
            @Override
            public void onClick(View view) {
                if(i == 1) {
                    checkBox.setChecked(true);
                    i--;
                } else if(i == 0) {
                    checkBox.setChecked(false);
                    i++;
                }
            }
        });
        */

        //Seekbar, throw into shared preferences
        SeekBar seekBar = rootView.findViewById(R.id.seekBar);
        TextView tv = rootView.findViewById(R.id.tv);
        tv.setText("Bar Climbed: " + tvInt);

        SeekBar seekBar2 = rootView.findViewById(R.id.seekBar2);
        TextView tv2 = rootView.findViewById(R.id.title_rating);
        tv2.setText("Overall Rating: " + tvInt2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvInt = i;
                tv.setText("Bar Climbed: " + i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tvInt2 = i;
                tv2.setText("Overall Rating: " + i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Stopwatch
        stopwatch = rootView.findViewById(R.id.stopwatch);
        Button start = rootView.findViewById(R.id.start);
        Button stop = rootView.findViewById(R.id.stop);
        Button reset = rootView.findViewById(R.id.reset);
        //Zayn the long variable at the top declared globally is the number to add to shared pref (its called stop, feel free to change it to something else)
        //Even if the actual stopwatch displays in seconds the long variable contains the milliseconds
        //If you wanna see declare a new global variable of a textview title like endgame title or something and then make it equal the stop variable. It shows the exact time when you press the stop button.
            start.setOnClickListener(view -> {
                startStopwatch();
            });
            stop.setOnClickListener(view -> {
                stopStopwatch();
            });
            reset.setOnClickListener(view -> {
                resetStopwatch();
            });

        //Finish Button
        Button finish = rootView.findViewById(R.id.finish);
        finish.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.body_container, new QRPage()).commit());
        return rootView;
    }

    //Stopwatch methods
    public void startStopwatch() {
        if(!running) {
            stopwatch.setBase(SystemClock.elapsedRealtime() - stop);
            stopwatch.start();
            running = true;
        }
    }

    public void stopStopwatch() {
        if(running) {
            stopwatch.stop();
            stop = SystemClock.elapsedRealtime() - stopwatch.getBase();
            running = false;
        }
    }

    public void resetStopwatch() {
        stopwatch.setBase(SystemClock.elapsedRealtime());
        stop = 0;
    }
}
