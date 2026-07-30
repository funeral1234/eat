package com.pca00168.eat;
import java.util.ArrayList;
public class kcal_sports extends ArrayList<kcal_sport> {
    public int total_kcal(){
        int total=0;
        for(kcal_sport sport:this) total+=sport.kcal;
        return total;
    }
    public static kcal_sports sport_list(){
        kcal_sports arr=new kcal_sports();
        for (kcal_sport.SportType sport : kcal_sport.SportType.values()) {
            arr.add(new kcal_sport(sport, 1));
        }
        return arr;
    }
}