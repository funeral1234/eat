package com.pca00168.eat;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import java.io.Serializable;
import java.util.ArrayList;
public class today_detail extends Activity {
    private boolean is_request_input;
    private ArrayList<View> cells=new ArrayList<>();
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(is_request_input) load_input_data();
            else load_output_data();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusBarUtils.setWindowStatusBarColor(this, R.color.dialog_transparent);
        setContentView(R.layout.today_detail);
        is_request_input=getIntent().getBooleanExtra("request_input",true);

        if(is_request_input) load_input_data();
        else load_output_data();
    }
    private void load_input_data(){
        LinearLayout io_kcal_today_table = findViewById(R.id.io_kcal_today_table);
        io_kcal_today_table.removeAllViews();
        cells.clear();
        for(kcal_food food :User.load_kcal_input(this,-1,public_func.timestamp_today(),public_func.timestamp_now())){
            View cell = LayoutInflater.from(this).inflate(R.layout.today_detail_table_cell, null);
            cell.setTag(false);
            ((TextView)cell.findViewById(R.id.type)).setText(kcal_foods.foodtype2string(food.type));
            ((ImageView)cell.findViewById(R.id.type_icon)).setImageDrawable(getResources().getDrawable(kcal_foods.foodtype2icon_resource_id(food.type)));
            ((TextView)cell.findViewById(R.id.kcal_value)).setText(String.valueOf(food.kcal));
            ((TextView)cell.findViewById(R.id.name)).setText(food.name);
            cell.findViewById(R.id.edit_btn).setOnClickListener(new View.OnClickListener(){
                public void onClick(View v) {
                    edit_onClick(cell,food);
                }
            });
            cell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    cell_onClick(v);
                }
            });
                io_kcal_today_table.addView(cell);
                cells.add(cell);
        }
    }
    private void load_output_data(){
        LinearLayout io_kcal_today_table = findViewById(R.id.io_kcal_today_table);
        io_kcal_today_table.removeAllViews();
        cells.clear();
        for(kcal_sport sport :User.load_kcal_output(this,-1,public_func.timestamp_today(),public_func.timestamp_now())){
            View cell = LayoutInflater.from(this).inflate(R.layout.today_detail_table_cell, null);
            cell.setTag(false);
            ((TextView)cell.findViewById(R.id.type)).setText(sport.sportType.displayName);
            ((ImageView)cell.findViewById(R.id.type_icon)).setImageDrawable(getResources().getDrawable(sport.sportType.iconResId));
            ((TextView)cell.findViewById(R.id.kcal_value)).setText(String.valueOf(sport.kcal));
            cell.findViewById(R.id.edit_btn).setOnClickListener(v -> edit_onClick(cell, sport));
            cell.setOnClickListener(v -> cell_onClick(v));
            io_kcal_today_table.addView(cell);
            cells.add(cell);
        }
    }
    private void cell_onClick(View c){
        if(!(boolean)c.getTag())
            for(View cell:cells){
                cell.setTag(true);
                change_edit_mode(cell);
            }
        change_edit_mode(c);
    }
    private void change_edit_mode(View cell){
        ImageView edit_btn=cell.findViewById(R.id.edit_btn);
        ConstraintLayout kcal_layout=cell.findViewById(R.id.kcal_layout);
        boolean editmode=!(boolean)cell.getTag();
        if(editmode){
            TranslateAnimation animate = new TranslateAnimation(edit_btn.getWidth(),0,0,0);
            animate.setDuration(300);
            kcal_layout.startAnimation(animate);
        }
        kcal_layout.setTranslationX(editmode?0:edit_btn.getWidth());
        cell.setTag(editmode);
    }
    private void edit_onClick(View cell, Serializable data){
        change_edit_mode(cell);
        if (!is_request_input && data instanceof kcal_sport) {
            kcal_sport sportData = (kcal_sport) data;
            if (sportData.is_fit) {
                Toast.makeText(this, "此為 Google Fit 自動同步紀錄，請至 Google Fit 修改", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(cell.getContext(), edit_today_detail.class);
        intent.putExtra("request_input",is_request_input);
        intent.putExtra("data",data);
        startActivity(intent);
    }
    public void exit_onClick(View v){
        onBackPressed();
    }
}