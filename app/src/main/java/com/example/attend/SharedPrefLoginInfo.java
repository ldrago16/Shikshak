package com.example.attend;

import android.content.Context;
import android.content.SharedPreferences;

final public class SharedPrefLoginInfo {

    private SharedPreferences sharedPref;
    private Context context;
    private final String ID_KEY = "userId";
    private final String Pass_KEY = "userPassword";

    SharedPrefLoginInfo(Context context){
        this.context = context;
        sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.login_preference),Context.MODE_PRIVATE);
    }

    private void writeLoginStatus(boolean status){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getResources().getString(R.string.login_preference_status), status);
        editor.apply();
    }

    public boolean readLoginStatus(){
        return sharedPref.getBoolean(context.getResources().getString(R.string.login_preference_status), false);
    }

    public void setUserIdPassword(String id, String password){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(ID_KEY, id);
        editor.putString(Pass_KEY, password);
        writeLoginStatus(true);
        editor.apply();
    }

    public void clearUserIdNameDetail(){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        writeLoginStatus(false);
    }

    public String getUserId(){
        return sharedPref.getString(ID_KEY, "notAvailable");
    }

    public String getUserPassword(){
        return sharedPref.getString(Pass_KEY, "notAvailable");
    }
}
