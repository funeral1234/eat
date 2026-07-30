package com.pca00168.eat;
import java.io.Serializable;
public class kcal_sport implements Serializable {
    public SportType sportType;
    public long time = public_func.timestamp_now();
    public int kcal = 0;
    public boolean is_fit = false;

    public kcal_sport(SportType sportType, int kcal) {
        this.sportType = sportType;
        this.kcal = kcal;
    }

    public kcal_sport(SportType sportType, int kcal, long time) {
        this(sportType, kcal);
        this.time = time;
    }

    public enum SportType {
        HIKING("健行", R.drawable.sport_item_hike),
        ROWING("划船機", R.drawable.sport_item_boat),
        STRENGTH("功能性肌力訓練", R.drawable.sport_item_strength_training),
        HIIT("高強度間歇訓練", R.drawable.sport_item_hiit),
        BIKE_INDOOR("室內健身車", R.drawable.sport_item_bike_indoor),
        BIKE_OUTDOOR("室外腳踏車", R.drawable.sport_item_bike),
        SWIM_POOL("封閉水域游泳", R.drawable.sport_item_swim),
        SWIM_OPEN("開放水域游泳", R.drawable.sport_item_swim_outside),
        RUN_INDOOR("室內跑步", R.drawable.sport_item_run),
        RUN_OUTDOOR("室外跑步", R.drawable.sport_item_run),
        WALK_OUTDOOR("室外步行", R.drawable.sport_item_walk),
        WALK_INDOOR("室內步行", R.drawable.sport_item_walk),
        CORE("核心訓練", R.drawable.sport_item_core),
        ELLIPTICAL("橢圓機", R.drawable.sport_item_elliptical),
        YOGA("瑜珈", R.drawable.sport_item_yoga),
        COOLDOWN("緩和運動", R.drawable.sport_item_exercise),
        DANCE("舞蹈", R.drawable.sport_item_dance),
        STEPPER("踏步機", R.drawable.sport_item_stepper);

        public final String displayName;
        public final int iconResId;

        SportType(String displayName, int iconResId) {
            this.displayName = displayName;
            this.iconResId = iconResId;
        }

        public static SportType fromGoogleFitActivity(String activity) {
            if (activity == null) return null;
            switch (activity) {
                case "hiking":                return kcal_sport.SportType.HIKING;
                case "rowing_machine":        return kcal_sport.SportType.ROWING;
                case "strength_training":     return kcal_sport.SportType.STRENGTH;
                case "interval_training":
                case "circuit_training":
                case "high_intensity_interval_training": return kcal_sport.SportType.HIIT;
                case "biking.stationary":     return kcal_sport.SportType.BIKE_INDOOR;
                case "biking":                return kcal_sport.SportType.BIKE_OUTDOOR;
                case "swimming.pool":         return kcal_sport.SportType.SWIM_POOL;
                case "swimming.open_water":
                case "swimming":              return kcal_sport.SportType.SWIM_OPEN;
                case "running.treadmill":     return kcal_sport.SportType.RUN_INDOOR;
                case "running":               return kcal_sport.SportType.RUN_OUTDOOR;
                case "walking":               return kcal_sport.SportType.WALK_OUTDOOR;
                case "walking.treadmill":     return kcal_sport.SportType.WALK_INDOOR;
                case "pilates":
                case "calisthenics":          return kcal_sport.SportType.CORE;
                case "elliptical":            return kcal_sport.SportType.ELLIPTICAL;
                case "yoga":                  return kcal_sport.SportType.YOGA;
                case "stretching":
                case "cooldown":
                case "meditation":            return kcal_sport.SportType.COOLDOWN;
                case "dancing":               return kcal_sport.SportType.DANCE;
                case "stair_climbing":
                case "stair_climbing.machine":return kcal_sport.SportType.STEPPER;
                default:                      return kcal_sport.SportType.HIKING; // 未知運動歸類為健行
            }
        }


        public static SportType fromOrdinal(int ordinal) { // 從 DB 的 ordinal 值還原 SportType
            SportType[] types = values();
            if (ordinal >= 0 && ordinal < types.length)
                return types[ordinal];
            else
                return HIKING;
        }
    }
}