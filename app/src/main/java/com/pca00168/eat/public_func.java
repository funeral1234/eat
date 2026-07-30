package com.pca00168.eat;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.EditText;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class public_func {
    public static String host="https://raw.githubusercontent.com/w852027/w852027.github.io/main/eat/";
    public static int getDrawableId(Context context, String var) {
        try {  return context.getResources().getIdentifier(var, "drawable", context.getPackageName());  } catch (Exception e) {  return 0;  }
    }
    public static String readData(Context context,String key){
        if (key.length() == 0) return "";
        return context.getSharedPreferences("Data", Context.MODE_PRIVATE).getString(key,"");//回傳在"Saved"索引之下的資料；若無儲存則回傳""
    }
    public static int readDataInt(Context context,String key){
        if (key.length() == 0) return 0;
        return context.getSharedPreferences("Data", Context.MODE_PRIVATE).getInt(key,0);//回傳在"Saved"索引之下的資料；若無儲存則回傳""
    }
    public static boolean readDataBoolean(Context context,String key){
        if (key.length() == 0) return false;
        return context.getSharedPreferences("Data", Context.MODE_PRIVATE).getBoolean(key,false);
    }
    public static boolean writeData(Context context,String key,String value){
        if (key.length() == 0)return false;
        /**創建SharedPreferences，索引為"Data"*/
        SharedPreferences sharedPreferences= context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//取得SharedPreferences.Editor編輯內容
        if(value.length()==0)editor.remove(key);// 移除key
        else editor.putString(key,value);//放入字串，並定義索引為key
        /**提交；提交結果將會回傳一布林值，若不需要提交結果，則可使用.apply()*/
        return editor.commit();
    }
    public static boolean writeData(Context context,String key,int value){
        if (key.length() == 0)return false;
        SharedPreferences sharedPreferences= context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();//取得SharedPreferences.Editor編輯內容
        if(value==0)editor.remove(key);
        else editor.putInt(key,value);
        return editor.commit();
    }
    public static boolean writeData(Context context,String key,boolean value){
        if (key.length() == 0)return false;
        SharedPreferences sharedPreferences= context.getSharedPreferences("Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        return editor.commit();
    }
    public interface  WebAPICallback {
        void success(JSONObject item) throws JSONException;
        void fail(IOException e);
    }
    public static void http_webapi(String url, Headers parameters, WebAPICallback completion){
        Call call = (new OkHttpClient().newBuilder().build()).newCall(
                new Request.Builder().url(url).headers(parameters).build()
        );
        call.enqueue(httpCallback(completion));
    }
    public static void http_webapi(String url, RequestBody parameters, WebAPICallback completion){
        Call call = (new OkHttpClient().newBuilder().build()).newCall(
                new Request.Builder().url(url).post(parameters).build()
        );
        call.enqueue(httpCallback(completion));
    }
    private static Callback httpCallback(WebAPICallback completion){
        return new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    completion.success(new JSONObject(response.body().string()));
                }
                catch(JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onFailure(Call call, IOException e) {
                completion.fail(e);
            }
        };
    }
    public static String date2string(Date date){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return df.format(c.getTime());
    }
    public static Date string2date(String date){
        return java.util.Calendar.getInstance().getTime();
    }

    //true:month false:day
    public static String timestamp_get(long timestamp,boolean month){
        SimpleDateFormat df = new SimpleDateFormat( month?"MM":"dd");
        Date date=new Date();
        date.setTime(timestamp*1000);
        return df.format(date);
    }
    public static long timestamp_now(){
        return (new Date()).getTime()/1000;
    }
    public static long timestamp_today(){
        Calendar calendar = Calendar.getInstance(); // 獲得當前的時間
        calendar.set(Calendar.HOUR_OF_DAY, 0);      // 設定時為00
        calendar.set(Calendar.MINUTE, 0);           // 設定分為00
        calendar.set(Calendar.SECOND, 0);           // 設定秒為00
        calendar.set(Calendar.MILLISECOND, 0);      // 設定毫秒為000
        return calendar.getTime().getTime()/1000;   // 返回時間的timestamp
    }
    public static long timestamp_a_week_ago(){
        return timestamp_today()-604800;
    }
    interface dialogResultCallBack {
        void OK(String text);
        void Cancel();
    }
    public static void TextInput_Dialog(Context context,String title,dialogResultCallBack Done) {
        TextInput_Dialog(context,title,"確定","取消",Done);
    }
    public static void TextInput_Dialog(Context context,String title,String okText,String cancelText,dialogResultCallBack Done) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText editText = new EditText(context); //final一個editText
        builder.setView(editText);
        builder.setTitle(title);
        builder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(MainActivity.this, editText.getText().toString()+"您好~", Toast.LENGTH_SHORT).show();

                Done.OK(editText.getText().toString());//將get到的文字轉成字串才可以給Toast顯示哦
            }
        });
        builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialogInterface, int i) {
                Done.Cancel();
            }
        });
        builder.create().show();
    }
    
    public interface dialogResultIntCallBack {
        void OK(int index);
        void Cancel();
    }
    
    public static void SingleChoice_Dialog(Context context, String title, String[] items, int checkedItem, dialogResultIntCallBack Done) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        final int[] selected = {checkedItem};
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selected[0] = which;
            }
        });
        builder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Done.OK(selected[0]);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Done.Cancel();
            }
        });
        builder.create().show();
    }
}