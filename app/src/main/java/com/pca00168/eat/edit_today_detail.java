package com.pca00168.eat;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
public class edit_today_detail extends Activity {
    private boolean is_request_input;
    private kcal_food food;
    private kcal_sport sport;
    private TextInputEditText kcal_value,name;
    private Button confirm_edit_btn;
    private ArrayList<View> cells=new ArrayList<>();
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        StatusBarUtils.setWindowStatusBarColor(this, R.color.transparent);
        setContentView(R.layout.edit_today_detail);
        kcal_value=findViewById(R.id.kcal_value);
        name=findViewById(R.id.name);
        confirm_edit_btn=findViewById(R.id.confirm_edit_btn);
        is_request_input=getIntent().getBooleanExtra("request_input",true);
       if(is_request_input){
           food= (kcal_food)getIntent().getSerializableExtra("data");
           name.setText(food.name);
           kcal_value.setText(String.valueOf(food.kcal));
           load_food_types();
       }else{
           sport= (kcal_sport) getIntent().getSerializableExtra("data");
           ((ImageView)findViewById(R.id.apple)).setImageDrawable(getResources().getDrawable(R.drawable.dumbbel));
           findViewById(R.id.food_name).setVisibility(View.GONE);
           kcal_value.setText(String.valueOf(sport.kcal));
           load_sport_types();
       }
        kcal_value.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {  check_field_empty();}
            public void afterTextChanged(Editable editable) {}
        });
        name.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {check_field_empty();}
            public void afterTextChanged(Editable editable) {}
        });
    }
    private void check_field_empty(){
        boolean no_empty=(!is_request_input||name.getText().length()>0) && kcal_value.getText().length()>0;
        confirm_edit_btn.setBackground(getResources().getDrawable( no_empty ? R.drawable.conform_add_orange : R.drawable.conform_add_gray));
        confirm_edit_btn.setClickable(no_empty);
    }
    private void load_food_types(){
        LinearLayout type_table=findViewById(R.id.type_table);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for(short t=0;t<4;t++){
            View cell = layoutInflater.inflate(R.layout.edit_kcal_type_item, null);
            cell.setTag(t);
            TextView type = cell.findViewById(R.id.type);
            type.setText(kcal_foods.foodtype2string(t));
            ImageView icon=cell.findViewById(R.id.icon);
            icon.setImageDrawable(getResources().getDrawable(kcal_foods.foodtype2icon_resource_id(t)));
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            type_table.addView(cell);
            cells.add(cell);
            cell_select(cell,food.type==t);
            cell.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    for(View c:cells) cell_select(c,false);
                    cell_select(cell,true);
                }
            });
        }
    }
    private void load_sport_types() {
        LinearLayout type_table = findViewById(R.id.type_table);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        for (kcal_sport s:kcal_sports.sport_list()) {
            View cell = layoutInflater.inflate(R.layout.edit_kcal_type_item, null);
            cell.setTag(s.sportType.ordinal());
            TextView type = cell.findViewById(R.id.type);
            type.setText(s.sportType.displayName);
            ImageView icon = cell.findViewById(R.id.icon);
            icon.setImageDrawable(getResources().getDrawable(s.sportType.iconResId));
            ImageViewCompat.setImageTintList(icon, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
            type_table.addView(cell);
            cells.add(cell);
            cell_select(cell, s.sportType == sport.sportType);
            cell.setOnClickListener(new View.OnClickListener() {
                
                public void onClick(View v) {
                    for (View c : cells) cell_select(c, false);
                    cell_select(cell, true);
                }
            });
        }
    }
    private void cell_select(View cell, boolean selected){
        cell.setAlpha(selected?1:(float)0.4);
        if(selected){
            if(is_request_input) food.type = (short)cell.getTag();
            else sport.sportType = kcal_sport.SportType.fromOrdinal((int)cell.getTag());
        }
    }
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(
                            findViewById(R.id.scrollView),
                            "scrollX",
                            0,(int)cells.get(is_request_input ? food.type : sport.sportType.ordinal()).getX()-50)
                            .setDuration(300);
        objectAnimator.start();
    }
    public void delete_click(View view){
        User.delete_kcal_data(this, is_request_input, is_request_input ? food.time : sport.time);
        onBackPressed();
    }
    public void okClick(View view){
        if(is_request_input){
            food.name=name.getText().toString();
            food.kcal=Integer.parseInt(kcal_value.getText().toString());
            User.edit_kcal_input(this,food);
        }else{
            sport.kcal=Integer.parseInt(kcal_value.getText().toString());
            User.edit_kcal_output(this,sport);
        }
        hide_keyboard(null);
        onBackPressed();
    }
    public void exit_onClick(View v){
        onBackPressed();
    }
    public void hide_keyboard(View v){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager= (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow( view.getWindowToken(), 0);
            kcal_value.clearFocus();
            name.clearFocus();
        }
    }
    public void onBackPressed(){
        if( kcal_value.isFocused() || name.isFocused() )
            hide_keyboard(null);
        else
            super.onBackPressed();
    }
}