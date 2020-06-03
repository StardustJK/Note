package group3.sse.bupt.note.Alarm;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class PlanDatabase extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "plans";
    public static final String CONTENT = "content";
    public static final String ISDONE = "isdone";
    public static final String ID = "_id";
    public static final String PLAN_TIME = "plan_time";
    public static final String ADD_TIME = "add_time";
    public static final String MODE = "mode";

    public PlanDatabase(Context context){
        super(context,"plans",null,3);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CONTENT + " TEXT NOT NULL,"
                +ISDONE+" INTEGER NOT NULL,"
                +ADD_TIME+" TEXT NOT NULL,"
                + PLAN_TIME + " TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists "+TABLE_NAME);
            onCreate(db);
        } catch (SQLException e) {
        e.printStackTrace();
        }

    }
}