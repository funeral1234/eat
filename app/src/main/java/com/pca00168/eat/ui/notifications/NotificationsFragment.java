package com.pca00168.eat.ui.notifications;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.pca00168.eat.R;
import com.pca00168.eat.User;
import com.pca00168.eat.databinding.FragmentNotificationsBinding;
import com.pca00168.eat.kcal_food;

import java.util.ArrayList;
public class NotificationsFragment extends Fragment implements TextWatcher {
    private View root;
    private TextInputEditText input_kcal;
    private TextInputEditText input_food_name;
    private  short food_type=0;
    private Button confirm_add_btn;
    private ArrayList<ImageView> input_food;
    
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(NotificationsViewModel.class);
        root = FragmentNotificationsBinding.inflate(inflater, container, false).getRoot();
        confirm_add_btn = (Button)root.findViewById(R.id.confirm_add_btn);
        input_kcal = (TextInputEditText)root.findViewById(R.id.input_kcal);
        input_food_name = (TextInputEditText)root.findViewById(R.id.input_food_name);
        confirm_add_btn.setOnClickListener(v -> {
            check_field_empty();
            if(confirm_add_btn.isClickable()){
                kcal_food item=new kcal_food();
                item.type = (short)(food_type-1);
                item.name = input_food_name.getText().toString();
                item.kcal = Integer.parseInt(input_kcal.getText().toString());
                User.add_kcal_input(getActivity(),item);
                getActivity().onBackPressed();
            }
        });


        input_kcal.addTextChangedListener(this);
        input_food_name.addTextChangedListener(this);

        input_food= new ArrayList<ImageView>();
        input_food.add(root.findViewById(R.id.input_meal));
        input_food.add(root.findViewById(R.id.input_dessert));
        input_food.add(root.findViewById(R.id.input_drink));
        input_food.add(root.findViewById(R.id.input_other));
        for (ImageView input_food_view : input_food) {
            input_food_view.setOnClickListener(v -> {
                for (ImageView view : input_food) {
                    view.setBackground(getResources().getDrawable(R.drawable.input_food));
                }
                v.setBackground(getResources().getDrawable(R.drawable.input_food_clicked));
                food_type = (short)(input_food.indexOf(v) + 1);
                check_field_empty();
            });
        }
        return root;
    }
    private void check_field_empty(){
        boolean no_empty=input_food_name.getText().length()>0 && input_kcal.getText().length()>0 && food_type>0;
        confirm_add_btn.setBackground(getResources().getDrawable( no_empty ? R.drawable.conform_add_orange : R.drawable.conform_add_gray));
        confirm_add_btn.setClickable(no_empty);
    }
    
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { check_field_empty(); }
    
    @Override
    public void afterTextChanged(Editable editable) {}
}