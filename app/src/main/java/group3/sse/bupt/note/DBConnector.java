package group3.sse.bupt.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBConnector {
    SQLiteOpenHelper dbHandler;
    SQLiteDatabase db;

    private static final String[] columns={
        PlanDatabase.ID,
        PlanDatabase.CONTENT,
        PlanDatabase.TIME,
    };

    public DBConnector(Context context){
        dbHandler=new PlanDatabase(context);
    }

    public void open(){
        db=dbHandler.getWritableDatabase();
    }

    public void close(){
        dbHandler.close();
    }

    public Plan addPlan(Plan plan){
        ContentValues contentValues=new ContentValues();
        contentValues.put(PlanDatabase.CONTENT,plan.getContent());
        Log.i("hcccc","plan.gettime1"+plan.getPlanTime().getTime());
        Log.i("hcccc","plan.gettime2"+plan.getTime());
        contentValues.put(PlanDatabase.TIME,plan.getTime());
        long insertId=db.insert(PlanDatabase.TABLE_NAME,null,contentValues);
        plan.setId(insertId);
        return plan;

    }
    public Plan getPlan(long id){
        //get a plan from database using cursor index
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,PlanDatabase.ID + "=?",
                new String[]{String.valueOf(id)},null,null, null, null);
        if (cursor != null) cursor.moveToFirst();
        Plan e = new Plan(cursor.getString(cursor.getColumnIndex(PlanDatabase.CONTENT)),cursor.getString(cursor.getColumnIndex(PlanDatabase.TIME)));
        return e;
    }

    public List<Plan> getAllPlans(){
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,null,null,null, null, null);

        List<Plan> plans = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Plan plan = new Plan();
                plan.setId(cursor.getLong(cursor.getColumnIndex(PlanDatabase.ID)));
                plan.setContent(cursor.getString(cursor.getColumnIndex(PlanDatabase.CONTENT)));
                plan.setTime(cursor.getString(cursor.getColumnIndex(PlanDatabase.TIME)));
                plans.add(plan);
            }
        }
        return plans;
    }
    public int updatePlan(Plan plan) {
        //update the info of an existing plan
        ContentValues values = new ContentValues();
        values.put(PlanDatabase.CONTENT, plan.getContent());
        values.put(PlanDatabase.TIME, plan.getTime());
        // updating row
        return db.update(PlanDatabase.TABLE_NAME, values,
                PlanDatabase.ID + "=?",new String[] { String.valueOf(plan.getId())});
    }
    public void removePlan(Plan plan) {
        //remove a plan according to ID value
        db.delete(PlanDatabase.TABLE_NAME, PlanDatabase.ID + "=" + plan.getId(), null);
    }
}

class PlanDatabase extends SQLiteOpenHelper{
    public static final String TABLE_NAME = "plans";
    public static final String CONTENT = "content";
    public static final String ID = "_id";
    public static final String TIME = "time";
    public static final String MODE = "mode";

    public PlanDatabase(Context context){
        super(context,"plans",null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+ TABLE_NAME
                + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + CONTENT + " TEXT NOT NULL,"
                + TIME + " TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}