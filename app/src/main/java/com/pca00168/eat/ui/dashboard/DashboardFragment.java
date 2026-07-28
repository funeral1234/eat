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
        FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ).build();
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
        // 使用 estimated_steps 資料來源（與 Google Fit App 及 Pikmin Bloom 一致的來源）
        // 若用原始 TYPE_STEP_COUNT_DELTA aggregate 會遭漏算法處理後的預估步數
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
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                TextView step_value = root.findViewById(R.id.step_value);
                                step_value.setText(String.valueOf(finalSteps));
                            }
                        });
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
    private void set_bg(){
        boolean night=today_minute<rise_up||today_minute>sunset;
        bg.setImageDrawable(getResources().getDrawable(night?R.drawable.night_bg:R.drawable.morning_bg));
    }
    public void onResume() {
        super.onResume();
        today_minute=Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60+Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        set_bg();
        load_data();
        /*
        public_func.http_webapi(
                "https://opendata.cwb.gov.tw/api/v1/rest/datastore/A-B0062-001", Headers.of(
                        new String[]{"Authorization","CWB-F93BFC87-7315-4DB8-A6F8-028CFC6AA3C0",
                                "format","JSON",
                                "locationName","",
                        "dataTime","2023-02-17"
                        }
                ),new public_func.WebAPICallback(){
                    @Override
                    public void success(JSONObject item) throws JSONException {
                          JSONArray arr=item.getJSONObject("records").
                                  getJSONObject("locations").getJSONArray("location").getJSONObject(0).
                                  getJSONArray("time").getJSONObject(0).getJSONArray("parameter");
                          for(int i=0;i<arr.length();i++){
                              if(arr.getJSONObject(i).getString("parameterName").equals("日出時刻")){
                                  String time = arr.getJSONObject(i).getString("parameterValue");
                                  rise_up=Integer.parseInt(time.split(":")[0])*60+Integer.parseInt(time.split(":")[1]);
                                  public_func.writeData(getActivity(),"rise_up_minute",rise_up);
                              }
                              if(arr.getJSONObject(i).getString("parameterName").equals("日沒時刻")){
                                  String time = arr.getJSONObject(i).getString("parameterValue");
                                  sunset=Integer.parseInt(time.split(":")[0])*60+Integer.parseInt(time.split(":")[1]);
                                  public_func.writeData(getActivity(),"sunset_minute",sunset);
                              }
                          }
                        set_bg();
                    }
                    @Override
                    public void fail(IOException e) {
                    }
                }
        );*/
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
        ConstraintLayout layout=root.findViewById(R.id.kcal_toast_view);
        String delta_kcal=public_func.readData(getActivity(),"delta_kcal");
        if(delta_kcal!=""){
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
            layout.animate().alpha(1).setDuration(2000).withEndAction(new Runnable() {
                    public void run() {
                        layout.animate().alpha(0).setDuration(1000).withEndAction(new Runnable() {
                            public void run() {
                                layout.setVisibility(View.INVISIBLE);
                            }
                        });
                }
            });
        }
    }











}