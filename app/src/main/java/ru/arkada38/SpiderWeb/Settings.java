package ru.arkada38.SpiderWeb;

import android.content.SharedPreferences;

final class Settings {
    static SharedPreferences sPref;

    final static String NUMBER_OF_LVL = "ru.arkada38.SpiderWeb.NumberOfLvl";
    final static String TAG = "SpiderWeb";
    final static String LICENCE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgBaMfw3RzDtAsLD385LueZB3FIsDB8fifvuynt+xgAkOnoQOlpd/wRwTgkz2MDPc/NhMkpLUK5I6ZlCu4D/cM9hlEcsisBEE8zjxT1bjPOF41NHkcuWMPr0ZrUi0Ls7FypLLcyTYKtXjkoCEFVUNRrhcFtim4NjI0hKGJ3M/tePSsyuowZ3YtsCc/WyM+sCxKPy9gk66rL95vjDEJzEYfeI4qphwX5EGGqvVIdDSvJ6DCQEu+1bt4fkhlW1DZJ4rnwRhF4uz68BGaR1ApP+tu/u+hpyb0h+o3G1d5m+iPgDjGGSWwM5XLNr6jzbJYaX8AvimHNKmW4/4kXtEZoJ2QQIDAQAB";

    private final static String MAX_LVL = "maxLvl"; // Максимальный пройденный уровень игроком
    private final static String SCALE = "1"; // Размер паучков

    public static void setMaxLvl(int maxLvl){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(MAX_LVL, maxLvl);
        ed.apply();
    }

    public static int getMaxLvl(){
        int maxLvl;
        try {
            maxLvl = sPref.getInt(MAX_LVL, 0);
        } catch (Exception e) {
            maxLvl = 0;
        }
        return maxLvl;
    }

    public static void setScale(float scale){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putFloat(SCALE, scale);
        ed.apply();
    }

    public static float getScale(){
        float scale;
        try {
            scale = sPref.getFloat(SCALE, 1);
        } catch (Exception e) {
            scale = 1;
        }
        return scale;
    }
}
