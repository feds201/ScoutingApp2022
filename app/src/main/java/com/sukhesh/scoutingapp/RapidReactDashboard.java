package com.sukhesh.scoutingapp;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sukhesh.scoutingapp.fields.ClosedQuestion;
import com.sukhesh.scoutingapp.fields.FiniteInt;
import com.sukhesh.scoutingapp.storage.JSONStorage;

import java.util.ArrayList;

public class RapidReactDashboard extends Fragment {
    long tMillis = 0L;
    long tStart = 0L;
    long tBuff = 0L;
    long tUpdate = 0L;
    boolean isResume = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_rapid_react_dashboard, container, false);
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
                f.tally.setText(String.valueOf(f.value));
                storage.add(matchName, f.name, f.value);
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

        //Seekbar, throw into shared preferences
        SeekBar seekBar = rootView.findViewById(R.id.seekBar);
        TextView tv = rootView.findViewById(R.id.tv);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                tv.setText("Bar Climbed: " + i);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Finish Button
        Button finish = rootView.findViewById(R.id.finish);
        finish.setOnClickListener(view -> getParentFragmentManager().beginTransaction().replace(R.id.body_container, new QRPage()).commit());

        //Stopwatch
        //BIG JANK, fix and make into class l8r
        Chronometer stopwatch = rootView.findViewById(R.id.stopwatch);
        ImageButton btStart = rootView.findViewById(R.id.play_button);
        ImageButton btStop = rootView.findViewById(R.id.reset_button);
        Handler handler = new Handler();


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tMillis = SystemClock.uptimeMillis() - tStart;
                tUpdate = tBuff + tMillis;
                int sec = (int) (tUpdate/1000) % 60;
                int millis = (int) (tUpdate%100);
                stopwatch.setText(String.format("%02d", sec) + ":" + String.format("%02d", millis));
                handler.postDelayed(this, 60);
            }
        };


        btStart.setOnClickListener(view -> {
            if (!isResume) {
                tStart = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                stopwatch.start();
                isResume = true;
                btStop.setVisibility(View.GONE);
                btStart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_icon, null));
            } else {
                tBuff += tMillis;
                handler.removeCallbacks(runnable);
                stopwatch.stop();
                isResume = false;
                btStop.setVisibility(View.VISIBLE);
                btStart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
            }
        });

        btStop.setOnClickListener(view -> {
            if (!isResume) {
                btStart.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play_icon, null));
                tMillis = 0L;
                tStart = 0L;
                tBuff = 0L;
                tUpdate = 0L;
                stopwatch.setText("00:00");
            }
        });

        return rootView;
    }
}
