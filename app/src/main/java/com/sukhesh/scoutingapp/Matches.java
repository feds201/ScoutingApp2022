package com.sukhesh.scoutingapp;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

public class Matches {

    ArrayList<Match> matchList;

    Matches(String[] rawMatches) {
        this.matchList = parseRawMatches(rawMatches);
    }

    public ArrayList<Match> parseRawMatches(String[] rawMatches) {
        ArrayList<String> intermediaryStringRepresentation = new ArrayList<>();
        Collections.addAll(intermediaryStringRepresentation, rawMatches);

        ArrayList<Match> finalMatches = new ArrayList<>();
        for(String match: intermediaryStringRepresentation) {
            String[] fields = match.split(",");
            if(fields.length != 5) {
                continue;
            }
            finalMatches.add(new Match(
                    fields[0],
                    Integer.parseInt(fields[1]),
                    Integer.parseInt(fields[2]),
                    fields[3],
                    fields[4]
            ));
        }
        return finalMatches;
    }

    public Match findMatchByMatchNumber(int number) {
        for(Match m: this.matchList) {
            if (m.matchNumber == number) {
                return m;
            }
        }
        return null;
    }

    public Match findMatchByMatchName(String matchName) {
        for(Match m: this.matchList) {
            if (m.matchName().equals(matchName)) {
                return m;
            }
        }
        return null;
    }

    public ArrayList<String> listOfMatchTypeAndNumber() {
        ArrayList<String> finalArray = new ArrayList<>();
        for(Match m: this.matchList) {
            finalArray.add(m.matchName());
        }
        return finalArray;
    }


    public void storeMatchByNumber(int matchNumber, SharedPreferences sp) {
        Match m = this.findMatchByMatchNumber(matchNumber);
        if (m != null) {
            m.storeMatch(sp);
        }
    }

    public void storeMatchByMatchName(String matchName, SharedPreferences sp) {
        Match m = this.findMatchByMatchName(matchName);
        if (m != null) {
            m.storeMatch(sp);
        }
    }
}
