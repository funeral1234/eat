package com.pca00168.eat;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
public class User {
    //public static void setUserName(String username){    }
    //public static void setUserAvatar(String username){    }
    public static String getUserAvatar(Context context){
        return public_func.readData(context, "avatar");
    }
    public static String getUserName(Context context){
        return public_func.readData(context,"name");
    }
    public static void add_kcal_input(Context context, kcal_food food){
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time",food.time);
        values.put("foodtype", food.type);
        values.put("foodname",food.name);
        values.put("kcal", food.kcal);
        s.insert("input_kcal", null, values);
        s.close();
        public_func.writeData(context,"delta_kcal", String.valueOf(food.kcal));
    }
    public static void add_kcal_output(Context context, kcal_sport sport){
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time",sport.time);
        values.put("sporttype", sport.type);
        values.put("kcal", sport.kcal);
        s.insert("output_kcal", null, values);
        s.close();
        public_func.writeData(context,"delta_kcal", String.valueOf(-sport.kcal));
    }
    public static void delete_kcal_data(Context context, boolean is_input,long timestamp){
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        s.delete( is_input ?  "input_kcal" : "output_kcal",String.format("time=%d",timestamp),null);
        s.close();
    }
    public static void edit_kcal_input(Context context, kcal_food food){
        ContentValues values = new ContentValues();
        values.put("foodtype", food.type);
        values.put("foodname",food.name);
        values.put("kcal", food.kcal);
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        s.update( "input_kcal",values,String.format("time=%d",food.time),null);
        s.close();
    }
    public static void edit_kcal_output(Context context, kcal_sport sport){
        ContentValues values = new ContentValues();
        values.put("sporttype", sport.type);
        values.put("kcal", sport.kcal);
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        s.update( "output_kcal",values,String.format("time=%d",sport.time),null);
        s.close();
    }
    public static void edit_google_fit_step_num(Context context, int step, long today_timestamp){
        ContentValues values = new ContentValues();
        values.put("num", step);
        values.put("time", today_timestamp);
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        s.update( "google_fit_step",values,String.format("time=%d",today_timestamp),null);
        s.close();
    }
    public static kcal_foods load_kcal_input(Context context,int food_type,long from_timestamp,long to_timestamp){//-1:全部
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getReadableDatabase();
        Cursor cursor = s.query(
                "input_kcal",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                String.format("%s time >= %d AND time <= %d",
                        food_type == -1 ? "" : String.format( "foodtype=%d AND",food_type),
                        from_timestamp,
                        to_timestamp),  // The columns for the WHERE clause
                 null,        // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        kcal_foods items = new kcal_foods();
        while(cursor.moveToNext()) {
            kcal_food item=new kcal_food();
            item.time=cursor.getInt(cursor.getColumnIndexOrThrow("time"));
            item.type = cursor.getShort(cursor.getColumnIndexOrThrow("foodtype"));
            item.name = cursor.getString(cursor.getColumnIndexOrThrow("foodname"));
            item.kcal = cursor.getInt(cursor.getColumnIndexOrThrow("kcal"));
            items.add(item);
        }
        cursor.close();
        return items;
    }
    public static kcal_sports load_kcal_output(Context context,int sport_type,long from_timestamp,long to_timestamp){//-1:全部
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getReadableDatabase();
        Cursor cursor = s.query(
                "output_kcal",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                String.format("%s time >= %d AND time <= %d",
                        sport_type == -1 ? "" : String.format( "sporttype=%d AND",sport_type),
                        from_timestamp,
                        to_timestamp),  // The columns for the WHERE clause
                null,        // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        kcal_sports items = new kcal_sports();
        while(cursor.moveToNext()) {
            // 不直接修改靜態 sport_list() 原型，複製出新物件再賦值
            kcal_sport template = kcal_sports.sport_list().get(cursor.getShort(cursor.getColumnIndexOrThrow("sporttype")));
            kcal_sport item = new kcal_sport(template.type, template.kcal, template.name, template.icon_resource_id);
            item.time=cursor.getInt(cursor.getColumnIndexOrThrow("time"));
            item.kcal = cursor.getInt(cursor.getColumnIndexOrThrow("kcal"));
            items.add(item);
        }
        cursor.close();
        return items;
    }
    public static int load_google_fit_step_num(Context context,long today_timestamp){
        SqlDataBaseHelper db=new SqlDataBaseHelper(context);
        SQLiteDatabase s = db.getWritableDatabase();
        Cursor cursor = s.query(
                "google_fit_step",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                String.format("time = %d",today_timestamp),  // The columns for the WHERE clause
                null,        // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        int num = 0;
        if(cursor.getCount()==0){
            ContentValues values = new ContentValues();
            values.put("time",today_timestamp);
            values.put("num",0);
            s.insert("google_fit_step", null, values);
        } else {
            while (cursor.moveToNext()) {
                num = cursor.getInt(cursor.getColumnIndexOrThrow("num"));
            }
        }
        cursor.close();
        s.close();
        return num;
    }
    public static clothings load_clothings(Context context,short main_type){//-1:全部
        ThingDataBaseHelper db=new ThingDataBaseHelper(context);
        SQLiteDatabase s = db.getReadableDatabase();
        Cursor cursor = s.query(
                "clothing",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                main_type == -1 ? null : String.format("maintype=%d ",main_type),  // The columns for the WHERE clause
                null,        // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        clothings items = new clothings();
        while(cursor.moveToNext()) {
            short maintype=(short) cursor.getInt(cursor.getColumnIndexOrThrow("main_type"));
            short subtype=(short) cursor.getInt(cursor.getColumnIndexOrThrow("short_type"));
            items.add(clothings.clothing_list(maintype).get(subtype));
        }
        cursor.close();
        return items;
    }
}
