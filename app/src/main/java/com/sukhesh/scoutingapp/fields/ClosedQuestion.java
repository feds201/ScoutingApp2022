package com.sukhesh.scoutingapp.fields;

import android.view.View;
import android.widget.CheckBox;

import com.sukhesh.scoutingapp.storage.JSONStorage;

import java.util.ArrayList;

public class ClosedQuestion {
    public final String name;
    public final CheckBox check;

    public boolean value;

    ClosedQuestion(String name, CheckBox check) {
        this.name = name;
        this.check = check;
        this.value = false;
    }

    public static ArrayList<ClosedQuestion> generateArrayListFromViews(ArrayList<View> views) {
        ArrayList<ClosedQuestion> closedQuestions = new ArrayList<>();
        for(int i = 0; i < views.size(); i++) {
            ClosedQuestion f = new ClosedQuestion(
                    views.get(i).getContentDescription().toString().split(" ")[1],
                    (CheckBox)views.get(i));
            closedQuestions.add(f);
        }

        return closedQuestions;
    }

    public void updateValue(JSONStorage js, String matchName) {
        this.value = js.getBoolean(matchName, this.name);
        if(this.value) {
            this.check.toggle();
        }
    }
}
