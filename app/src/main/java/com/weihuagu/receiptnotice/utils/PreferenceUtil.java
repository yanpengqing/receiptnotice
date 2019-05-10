package com.weihuagu.receiptnotice.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
public class PreferenceUtil{
        SharedPreferences sharedPref=null;
        Context context=null;
        public PreferenceUtil(Context context){
                this.context=context;
                init();
        }
        public void init(){
                sharedPref=PreferenceManager.getDefaultSharedPreferences(this.context);

        }
        public void setToken(String value){
               sharedPref.edit().putString("token",value).commit();
        }
        public String getToken(){
                return this.sharedPref.getString("token","");
        }
        public PreferenceUtil putString(String key, String value) {
                if (value == null) {
                        sharedPref.edit().remove(key);
                } else {
                        sharedPref.edit().putString(key,value).commit();
                }
                return this;
        }
        public PreferenceUtil putBoolean(String key, Boolean value) {
                if (value == null) {
                        sharedPref.edit().remove(key);
                } else {
                        sharedPref.edit().putBoolean(key,value).commit();
                }
                return this;
        }
        public boolean getBoolean(String key, boolean defVaule) {
                return sharedPref.getBoolean(key, defVaule);
        }
        public String getString(String key, String defVaule) {
                return sharedPref.getString(key, defVaule);
        }
        public String getDeviceid(){
                return this.sharedPref.getString("deviceid","");
        }
        public boolean isEncrypt(){
                return this.sharedPref.getBoolean("isencrypt",false);
        }
        public boolean isEcho(){
                return this.sharedPref.getBoolean("isecho",false);
        }
        public String getEchoServer(){
                return this.sharedPref.getString("echoserver",null);
        }
        public String getEchoInterval(){
                return this.sharedPref.getString("echointerval","");
        }
        public String getEncryptMethod(){
                return this.sharedPref.getString("encryptmethod",null);
        }
        public String getPasswd(){
                return this.sharedPref.getString("passwd",null);
        }

}
