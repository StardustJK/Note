package group3.sse.bupt.note.Alarm;

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
        PlanDatabase.PLAN_TIME,
        PlanDatabase.ISDONE,
        PlanDatabase.ADD_TIME,
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
        contentValues.put(PlanDatabase.PLAN_TIME,plan.getTime());
        contentValues.put(PlanDatabase.ISDONE,plan.getIsDone());
        contentValues.put(PlanDatabase.ADD_TIME,plan.getAddTime());
        long insertId=db.insert(PlanDatabase.TABLE_NAME,null,contentValues);
        plan.setId(insertId);
        return plan;

    }
    public Plan getPlan(long id){
        //get a plan from database using cursor index
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,PlanDatabase.ID + "=?",
                new String[]{String.valueOf(id)},null,null, null, null);
        if (cursor != null) cursor.moveToFirst();
        Plan e = new Plan(cursor.getString(cursor.getColumnIndex(PlanDatabase.CONTENT)),
                cursor.getString(cursor.getColumnIndex(PlanDatabase.PLAN_TIME)),
                cursor.getInt(cursor.getColumnIndex(PlanDatabase.ISDONE)));
        return e;
    }

    public List<Plan> getAllPlans(){
        Cursor cursor = db.query(PlanDatabase.TABLE_NAME,columns,null,null,null, null, PlanDatabase.ISDONE+","+PlanDatabase.ADD_TIME+" DESC");

        List<Plan> plans = new ArrayList<>();
        if(cursor.getCount() > 0){
            while(cursor.moveToNext()){
                Plan plan = new Plan();
                plan.setId(cursor.getLong(cursor.getColumnIndex(PlanDatabase.ID)));
                plan.setContent(cursor.getString(cursor.getColumnIndex(PlanDatabase.CONTENT)));
                plan.setTime(cursor.getString(cursor.getColumnIndex(PlanDatabase.PLAN_TIME)));
                plan.setIsDone(cursor.getInt(cursor.getColumnIndex(PlanDatabase.ISDONE)));
                plan.setAddTime(cursor.getString(cursor.getColumnIndex(PlanDatabase.ADD_TIME)));

                plans.add(plan);
            }
        }
        return plans;
    }
    public int updatePlan(Plan plan) {
        //update the info of an existing plan
        ContentValues values = new ContentValues();
        values.put(PlanDatabase.CONTENT, plan.getContent());
        values.put(PlanDatabase.PLAN_TIME, plan.getTime());
        values.put(PlanDatabase.ISDONE, plan.getIsDone());
        values.put(PlanDatabase.ADD_TIME,plan.getAddTime());

        // updating row
        return db.update(PlanDatabase.TABLE_NAME, values,
                PlanDatabase.ID + "=?",new String[] { String.valueOf(plan.getId())});
    }
    public void removePlan(Plan plan) {
        //remove a plan according to ID value
        db.delete(PlanDatabase.TABLE_NAME, PlanDatabase.ID + "=" + plan.getId(), null);
    }
}

