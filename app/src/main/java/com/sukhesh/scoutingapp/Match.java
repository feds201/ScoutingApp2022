package com.sukhesh.scoutingapp;

import android.content.SharedPreferences;

import java.lang.reflect.*;


public class Match {
    String matchType;
    int matchNumber;
    int teamNumber;
    String teamName;
    String robotAllianceInfo;

    Match(String matchType, int matchNumber, int teamNumber, String teamName, String robotAllianceInfo) {
        this.matchType = matchType;
        this.matchNumber = matchNumber;
        this.teamNumber = teamNumber;
        this.teamName = teamName;
        this.robotAllianceInfo = robotAllianceInfo;
    }

    public String matchName() {
        return this.matchType + " " + this.matchNumber;
    }

    public static Match MatchFromSharedPreferences(SharedPreferences sp) {
        String matchType = sp.getString(Match.variableNameToConstName("matchType"), "Qualification");
        int matchNumber = sp.getInt(Match.variableNameToConstName("matchNumber"), 1);
        int teamNumber = sp.getInt(Match.variableNameToConstName("typeNumber"), 201);
        String teamName = sp.getString(Match.variableNameToConstName("teamName"), "The FEDS");
        String robotAllianceInfo = sp.getString(Match.variableNameToConstName("robotAllianceInfo"), "Red 1");
        return new Match(matchType, matchNumber, teamNumber, teamName, robotAllianceInfo);
    }

    public void storeMatch(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        Field[] fieldList = this.getClass().getDeclaredFields();
        for(Field fld: fieldList) {
            String typeName = fld.getType().getTypeName();
            String constName = Match.variableNameToConstName(fld.getName());
            switch (typeName) {
                case "java.lang.String":
                    try {
                        editor.putString(constName, fld.get(this).toString());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                case "int":
                    try {
                        editor.putInt(constName, Integer.parseInt(fld.get(this).toString()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                case "double":
                case "float":
                    try {
                        editor.putFloat(constName, Float.parseFloat(fld.get(this).toString()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        editor.apply();
    }

    private static String variableNameToConstName(String name) {
        // essentially, if the variable name was matchType, this returns MATCH_TYPE
        String constName = "";
        for(int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLowerCase(c)) {
                constName += Character.toUpperCase(c);
            } else if (Character.isUpperCase(c)) {
                constName += "_";
                constName += c;
            }
        }
        return constName;
    }

    // TODO: extract this to class citrusCiruitStyleMessage and pass in fields in initializer
    public String citrusCircuitStyleMessage() {
        Field[] fieldList = this.getClass().getDeclaredFields();
        String returnString = "";
        for(int i = 0; i < fieldList.length; i++) {
            Field fld = fieldList[i];
            try {
                String s = numToTwoDigitCode(i) + fld.get(this).toString() + "$";
                returnString += s;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return returnString;
    }


    public String encodeVariableName(String varName) {
        Field[] fieldList = this.getClass().getDeclaredFields();
        for (int i = 0; i < fieldList.length; i++) {
            if (fieldList[i].getName().equals(varName)) {
                return numToTwoDigitCode(i);
            }
        }
        return null;
    }

    private String numToTwoDigitCode(int num) {
        char secondChar = (char)((int)'A' + num % 26);
        char firstChar = (char)((int)'A' + num / 26);
        return "" + firstChar + secondChar;
    }

    private int twoDigitCodeToNum(String code){
        if(code.length() != 2) {
            return -1;
        }
        return (26 * ((int)Character.toUpperCase(code.charAt(0)) - (int)'A')) +
                     ((int)Character.toUpperCase(code.charAt(1)) - (int)'A');
    }

    public String decodeVariableNames(String encodedVarName) {
        int num = twoDigitCodeToNum(encodedVarName);
        Field[] fieldList = this.getClass().getDeclaredFields();
        return fieldList[num].getName();
    }
}
