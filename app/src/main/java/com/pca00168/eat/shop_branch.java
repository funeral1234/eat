package com.pca00168.eat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Headers;
public class shop_branch extends AppCompatActivity implements public_func.WebAPICallback {
    private JSONArray list;
    private LinearLayout branch_solution;
    private ProgressBar progressBar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        StatusBarUtils.setWindowStatusBarColor(this,R.color.main_yellow);
        setContentView(R.layout.shop_branch);
        branch_solution=findViewById(R.id.branch_solution);
        progressBar=findViewById(R.id.progressBar);
        public_func.http_webapi(
                com.pca00168.eat.public_func.host+"branch-solution.json",Headers.of(),
                this
        );
    }
    private void load_solution() throws JSONException {
        branch_solution.removeAllViews();
        for(int i=0;i<list.length();){
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View branch_solution_cell = layoutInflater.inflate(R.layout.branch_solution_cell, null);
            JSONObject solution=list.getJSONObject(i);
            Picasso.get().load(com.pca00168.eat.public_func.host+solution.getString("pic")).into((ImageView) branch_solution_cell.findViewById(R.id.branch_pic));
            String cost=solution.getInt("cost")==-1?"看廣告":String.format("NT %d",solution.getInt("cost"));
            ((TextView)branch_solution_cell.findViewById(R.id.branch_cost)).setText(cost);
            ((TextView)branch_solution_cell.findViewById(R.id.branch_count)).setText(solution.getString("count"));
            ((TextView)branch_solution_cell.findViewById(R.id.branch_name)).setText(solution.getString("name"));
            branch_solution.addView(branch_solution_cell);
            if(++i<list.length())
                branch_solution.addView(layoutInflater.inflate(R.layout.shop_branch_underline, null));
        }
    }
    @Override
    public void success(JSONObject item) throws JSONException {
        list = item.getJSONArray("items");
        runOnUiThread(() -> {
            ((android.view.ViewGroup)progressBar.getParent()).removeView(progressBar);
            try {
                load_solution();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void fail(IOException e) {
    }
}