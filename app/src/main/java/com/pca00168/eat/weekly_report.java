package com.pca00168.eat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
public class weekly_report extends AppCompatActivity {
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        StatusBarUtils.setWindowStatusBarColor(this,R.color.main_yellow);
        setContentView(R.layout.weekly_report);
        TextView date=findViewById(R.id.week_date);
        date.setText(String.format("%s.%s - %s.%s",
                public_func.timestamp_get( public_func.timestamp_a_week_ago(),true),
                public_func.timestamp_get( public_func.timestamp_a_week_ago(),false),
                public_func.timestamp_get( public_func.timestamp_now(),true),
                public_func.timestamp_get( public_func.timestamp_now(),false)
        ));
        load_input_data();
        load_output_data();
    }
    private void load_input_data(){
        LinearLayout input_kcal_table = findViewById(R.id.input_kcal_table);
        for (short type=0;type<=3;type++) {
            kcal_foods foods=User.load_kcal_input(this,type,public_func.timestamp_a_week_ago(),public_func.timestamp_now());
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View cell = layoutInflater.inflate(R.layout.weekly_report_table_cell, null);
            ((TextView)cell.findViewById(R.id.type)).setText(kcal_foods.foodtype2string(type));
            ((ImageView)cell.findViewById(R.id.type_icon)).setImageDrawable(getResources().getDrawable(kcal_foods.foodtype2icon_resource_id(type)));
            ((TextView)cell.findViewById(R.id.count_value)).setText(String.valueOf(foods.size()));
            ((TextView)cell.findViewById(R.id.kcal_value)).setText(String.valueOf(foods.total_kcal()));
            cell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                }
            });
            input_kcal_table.addView(cell);
        }
    }
    private void load_output_data(){
        LinearLayout output_kcal_table = findViewById(R.id.output_kcal_table);
        for (short type=0;type<kcal_sports.sport_list().size();type++) {
            kcal_sports sports=User.load_kcal_output(this,type,public_func.timestamp_a_week_ago(),public_func.timestamp_now());
            if(sports.size()==0)continue;
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View cell = layoutInflater.inflate(R.layout.weekly_report_table_cell, null);
            ((TextView)cell.findViewById(R.id.count)).setText("次");
            ((TextView)cell.findViewById(R.id.type)).setText(sports.get(0).sportType.displayName);
            ((ImageView)cell.findViewById(R.id.type_icon)).setImageDrawable(getResources().getDrawable(sports.get(0).sportType.iconResId));
            ((TextView)cell.findViewById(R.id.count_value)).setText(String.valueOf(sports.size()));
            ((TextView)cell.findViewById(R.id.kcal_value)).setText(String.valueOf(sports.total_kcal()));
            cell.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                }
            });
            output_kcal_table.addView(cell);
        }
    }
}