package com.pca00168.eat.ui.dashboard;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.XXPermissions;
import com.pca00168.eat.Home;
import com.pca00168.eat.R;
import com.pca00168.eat.User;
import com.pca00168.eat.databinding.FragmentDashboardBinding;
import com.pca00168.eat.kcal_sport;
import com.pca00168.eat.kcal_sports;
import com.pca00168.eat.public_func;
import com.pca00168.eat.today_detail;
import com.qw.soul.permission.SoulPermission;
import com.qw.soul.permission.bean.Permission;
import com.qw.soul.permission.callbcak.CheckRequestPermissionListener;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
public class DashboardFragment extends Fragment {
    private View root;
    private ImageView bg;
    private int today_minute,rise_up,sunset;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(DashboardViewModel.class);
        root=FragmentDashboardBinding.inflate(inflater, container, false).getRoot();
        rise_up=public_func.readDataInt(getActivity(),"rise_up_minute");
        sunset=public_func.readDataInt(getActivity(),"sunset_minute");
        bg=root.findViewById(R.id.bg);
        bg.setOnClickListener(v -> {
            String astroJson = public_func.readData(getActivity(), "astro_json");
            if (astroJson == null || astroJson.isEmpty()) {
                android.widget.Toast.makeText(getActivity(), "尚無天文資料，請稍後再試或檢查定位權限", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            showAstroReport(astroJson);
        });
        ConstraintLayout layout=root.findViewById(R.id.kcal_toast_view);
        layout.setVisibility(View.INVISIBLE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            XXPermissions.with(this)
                    .permission(Manifest.permission.ACTIVITY_RECOGNITION)
                    .request(new OnPermissionCallback() {
                        public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                            if (allGranted) {
                                readFitnessData();
                            }
                        }

                        public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                            if (doNotAskAgain) {
                            } else {
                            }
                        }
                    });
            /*SoulPermission.getInstance().checkAndRequestPermission(Manifest.permission.ACTIVITY_RECOGNITION,
                    new CheckRequestPermissionListener() {
                        public void onPermissionOk(Permission permission) {
                            readFitnessData();
                        }
                        public void onPermissionDenied(Permission permission) {
                            Toast.makeText(getActivity(),"deny", Toast.LENGTH_SHORT).show();
                        }
                    });*/
        return root;
    }
    private void readFitnessData() {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
                .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    0x1001,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
            return;
        }
        long startTime = public_func.timestamp_today();
        long endTime = public_func.timestamp_now();
        // 使用 estimated_steps 資料來源（與 Google Fit App 顯示一致）
        DataSource estimatedStepsDataSource = new DataSource.Builder()
                .setAppPackageName("com.google.android.gms")
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .build();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(estimatedStepsDataSource, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.SECONDS)
                .build();
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        int steps = 0;
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            for (DataSet dataSet : bucket.getDataSets()) {
                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        if (field.equals(Field.FIELD_STEPS)) {
                                            steps += dp.getValue(field).asInt();
                                        }
                                    }
                                }
                            }
                        }
                        final int finalSteps = steps;
                        User.edit_google_fit_step_num(getActivity(), finalSteps, startTime);
                        getActivity().runOnUiThread(() -> {
                            TextView step_value = root.findViewById(R.id.step_value);
                            step_value.setText(String.valueOf(finalSteps));
                        });
                        // 步數讀完後，接著讀取各項運動消耗卡路里
                        readFitnessCalories();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception e) {
                        //err(e.getMessage());
                    }
                });
    }


    /** 從 Google Fit 讀取今日各項運動，分別寫入對應的運動類型 */
    private void readFitnessCalories() {
        long startTime = public_func.timestamp_today();
        long endTime = public_func.timestamp_now();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivitySegment(1, TimeUnit.MINUTES)
                .setTimeRange(startTime, endTime, TimeUnit.SECONDS)
                .build();
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        kcal_sports existing = User.load_kcal_output(getActivity(), -1, startTime, endTime);
                        java.util.HashSet<Long> existingTimes = new java.util.HashSet<>();
                        for (kcal_sport sport : existing) {
                            existingTimes.add(sport.time);
                        }

                        boolean hasData = false;
                        int totalNewKcal = 0;
                        for (Bucket bucket : dataReadResponse.getBuckets()) {
                            String activity = bucket.getActivity();
                            // 跳過靜止和未知
                            if ("still".equals(activity) || "unknown".equals(activity)
                                    || "tilting".equals(activity) || "in_vehicle".equals(activity))
                                continue;

                            long fitTime = bucket.getStartTime(TimeUnit.SECONDS);
                            if (existingTimes.contains(fitTime)) {
                                continue; // 已存在：不再重複插入（舊版是一律刪除重插，現在改為保留使用者的修改）
                            }

                            int kcal = 0;
                            for (DataSet dataSet : bucket.getDataSets()) {
                                for (DataPoint dp : dataSet.getDataPoints()) {
                                    for (Field field : dp.getDataType().getFields()) {
                                        if (field.equals(Field.FIELD_CALORIES)) {
                                            kcal += (int) dp.getValue(field).asFloat();
                                        }
                                    }
                                }
                            }

                            if (kcal <= 0) continue;
                            kcal_sport sport = new kcal_sport(
                                    kcal_sport.SportType.fromGoogleFitActivity(activity),
                                    kcal,
                                    fitTime); 
                            sport.is_fit = true;
                            User.add_kcal_output(getActivity(), sport, false);
                            totalNewKcal += kcal;
                            hasData = true;
                        }

                        if (hasData && getActivity() != null) {
                            if (totalNewKcal > 0) {
                                String currentDelta = public_func.readData(getActivity(), "delta_kcal");
                                int current = currentDelta.isEmpty() ? 0 : Integer.parseInt(currentDelta);
                                public_func.writeData(getActivity(), "delta_kcal", String.valueOf(current - totalNewKcal));
                            }
                            getActivity().runOnUiThread(() -> load_data());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    public void onFailure(@NonNull Exception e) {
                        //err(e.getMessage());
                    }
                });
    }


    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
    public void set_bg(){
        boolean night=today_minute<rise_up||today_minute>sunset;
        bg.setImageDrawable(getResources().getDrawable(night?R.drawable.night_bg:R.drawable.morning_bg));
    }
    public void onResume() {
        super.onResume();
        today_minute=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60+Calendar.getInstance().get(Calendar.MINUTE);
        set_bg();
        load_data();
        
        com.hjq.permissions.XXPermissions.with(this)
                .permission(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                .request(new com.hjq.permissions.OnPermissionCallback() {
                    @Override
                    public void onGranted(java.util.List<String> permissions, boolean allGranted) {
                        fetchSunriseSunset();
                    }
                    @Override
                    public void onDenied(java.util.List<String> permissions, boolean doNotAskAgain) {
                        fetchSunriseSunset();
                    }
                });
    }

    private void fetchSunriseSunset() {
        try {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                androidx.core.app.ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                android.location.LocationManager locationManager = (android.location.LocationManager) getActivity().getSystemService(android.content.Context.LOCATION_SERVICE);
                android.location.Location loc = null;
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER);
                    if (loc == null) loc = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);
                    if (loc == null) loc = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                }
                if (loc != null) {
                    String url = "https://api.sunrise-sunset.org/json?lat=" + loc.getLatitude() + "&lng=" + loc.getLongitude() + "&formatted=0";
                    public_func.http_webapi(url, new okhttp3.Headers.Builder().build(), new public_func.WebAPICallback() {
                        @Override
                        public void success(org.json.JSONObject item) throws org.json.JSONException {
                            try {
                                org.json.JSONObject results = item.getJSONObject("results");
                                public_func.writeData(getActivity(), "astro_json", results.toString());
                                String sunriseIso = results.getString("sunrise").replaceAll("([+-]\\d\\d):(\\d\\d)$", "$1$2");
                                String sunsetIso = results.getString("sunset").replaceAll("([+-]\\d\\d):(\\d\\d)$", "$1$2");
                                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US);
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTime(isoFormat.parse(sunriseIso));
                                int riseUpMinute = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
                                cal.setTime(isoFormat.parse(sunsetIso));
                                int sunsetMinute = cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
                                updateSunriseSunset(riseUpMinute, sunsetMinute);
                            } catch (Exception e) {
                                e.printStackTrace();
                                updateSunriseSunset(6 * 60, 18 * 60);
                            }
                        }
                        @Override
                        public void fail(java.io.IOException e) {
                            updateSunriseSunset(6 * 60, 18 * 60);
                        }
                    });
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateSunriseSunset(6 * 60, 18 * 60);
    }

    private void updateSunriseSunset(int riseUpMinute, int sunsetMinute) {
        rise_up = riseUpMinute;
        sunset = sunsetMinute;
        public_func.writeData(getActivity(), "rise_up_minute", rise_up);
        public_func.writeData(getActivity(), "sunset_minute", sunset);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> set_bg());
        }
    }

    private void load_data(){
        TextView to_eat_value=root.findViewById(R.id.to_eat_value);
        to_eat_value.setText(String.valueOf(
                User.load_kcal_input(
                        getActivity(),
                        -1,
                        public_func.timestamp_today(),
                        public_func.timestamp_now()
                ).total_kcal())
        );
        TextView dumbbel_value=root.findViewById(R.id.dumbbel_value);
        dumbbel_value.setText(String.valueOf(
                User.load_kcal_output(
                        getActivity(),
                        -1,
                        public_func.timestamp_today(),
                        public_func.timestamp_now()
                ).total_kcal())
        );
        TextView step_value=root.findViewById(R.id.step_value);
        step_value.setText(String.valueOf(User.load_google_fit_step_num(getActivity(),public_func.timestamp_today())));
        
        ConstraintLayout step_layout = root.findViewById(R.id.step_layout);
        if (step_layout != null) {
            String mode_str = public_func.readData(getActivity(), "google_fit_sync_mode");
            step_layout.setVisibility(mode_str.equals("0") ? View.GONE : View.VISIBLE);
        }
        ConstraintLayout layout=root.findViewById(R.id.kcal_toast_view);
        String delta_kcal=public_func.readData(getActivity(),"delta_kcal");
        if(!delta_kcal.isEmpty()){
            TextView kcal=root.findViewById(R.id.kcal_toast_delta);
            kcal.setText(Integer.parseInt(delta_kcal) <0?delta_kcal.substring(1):delta_kcal);
            TextView add_minus=root.findViewById(R.id.kcal_toast_text);
            add_minus.setText(Integer.parseInt(delta_kcal) <0?"消耗":"增加");
            ImageView icon=root.findViewById(R.id.kcal_toast_icon);
            icon.setImageDrawable(getResources().getDrawable( Integer.valueOf(delta_kcal) <0? R.drawable.dumbbel:R.drawable.apple));
            public_func.writeData(getActivity(),"delta_kcal","");
            TextView look=root.findViewById(R.id.look);
            look.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), today_detail.class);
                    intent.putExtra("request_input",Integer.parseInt(delta_kcal) >0);
                    startActivity(intent);
                }
            });
            layout.setVisibility(View.VISIBLE);
            layout.animate().alpha(1).setDuration(2000).withEndAction(() -> {
                        layout.animate().alpha(0).setDuration(1000).withEndAction(() -> {
                                layout.setVisibility(View.GONE);
                                layout.setAlpha(1);
                        }).start();
            }).start();
        }
    }








    private String formatAstroTime(org.json.JSONObject results, String key, java.text.SimpleDateFormat isoFormat, java.text.SimpleDateFormat timeFormat) {
        try {
            String raw = results.getString(key).replaceAll("([+-]\\d\\d):(\\d\\d)$", "$1$2");
            return timeFormat.format(isoFormat.parse(raw));
        } catch (Exception e) {
            return "--:--:--";
        }
    }

    private void showAstroReport(String jsonStr) {
        if (getActivity() == null) return;
        try {
            org.json.JSONObject results = new org.json.JSONObject(jsonStr);
            java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.US);
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US);
            
            String sunrise = formatAstroTime(results, "sunrise", isoFormat, timeFormat);
            String sunset = formatAstroTime(results, "sunset", isoFormat, timeFormat);
            String solarNoon = formatAstroTime(results, "solar_noon", isoFormat, timeFormat);
            int dayLength = results.getInt("day_length");
            int hours = dayLength / 3600;
            int mins = (dayLength % 3600) / 60;
            
            String civilBegin = formatAstroTime(results, "civil_twilight_begin", isoFormat, timeFormat);
            String civilEnd = formatAstroTime(results, "civil_twilight_end", isoFormat, timeFormat);
            String nauticalBegin = formatAstroTime(results, "nautical_twilight_begin", isoFormat, timeFormat);
            String nauticalEnd = formatAstroTime(results, "nautical_twilight_end", isoFormat, timeFormat);
            String astroBegin = formatAstroTime(results, "astronomical_twilight_begin", isoFormat, timeFormat);
            String astroEnd = formatAstroTime(results, "astronomical_twilight_end", isoFormat, timeFormat);
            
            StringBuilder sb = new StringBuilder();
            sb.append("1. 基本日照與時間資訊\n");
            sb.append("日出與日落：當天太陽於早上 ").append(sunrise).append(" 升起，並於傍晚 ").append(sunset).append(" 落下。\n\n");
            sb.append("日照長度：總日照時間為 ").append(dayLength).append(" 秒，換算約為 ").append(hours).append(" 小時 ").append(mins).append(" 分鐘。\n\n");
            sb.append("正午：太陽到達天頂最高點的時間為 ").append(solarNoon).append("。\n\n");
            
            sb.append("2. 曙暮光階段 (Twilight Phases)\n");
            sb.append("民用曙暮光：").append(civilBegin).append(" ~ ").append(civilEnd).append("\n");
            sb.append("航海曙暮光：").append(nauticalBegin).append(" ~ ").append(nauticalEnd).append("\n");
            sb.append("天文曙暮光：").append(astroBegin).append(" ~ ").append(astroEnd).append("\n\n");
            
            sb.append("備註：本報告未包含月相與精確方位角等需要額外之天文運算的數據。");
            
            new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setTitle("太陽與天文觀測報告")
                .setMessage(sb.toString())
                .setPositiveButton("確定", null)
                .show();
                
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(getActivity(), "解析天文資料失敗", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}