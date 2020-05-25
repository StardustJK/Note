package group3.sse.bupt.note.Alarm;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Plan {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private long id;//自增长，建立是数据库的时候设置该列自增长
    private String content;
    private Calendar planTime;
    private int isDone;
    private Calendar addTime;


    public Plan(String content, String time,int done) {
        this.content = content;
        setTime(time);
        isDone=done;
        this.addTime=Calendar.getInstance();
    }

    public Plan(){
        this.planTime = Calendar.getInstance();
        this.addTime=Calendar.getInstance();
        isDone=0;
    }

    public void setTime(String format){

        try {
            Date temp = simpleDateFormat.parse(format);
            planTime = Calendar.getInstance();
            planTime.setTime(temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getYear(){
        return planTime.get(Calendar.YEAR);
    }

    public int getMonth(){
        return planTime.get(Calendar.MONTH);
    }

    public int getDay() {
        return planTime.get(Calendar.DAY_OF_MONTH);
    }

    public int getHour() {
        return planTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMinute() {
        return planTime.get(Calendar.MINUTE);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getIsDone() {
        return isDone;
    }
    public void setIsDone(int isDone){
        this.isDone=isDone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Calendar getPlanTime() {
        return planTime;
    }

    public String getTime(){
        return simpleDateFormat.format(planTime.getTime());
    }
    public String getAddTime(){
        return simpleDateFormat.format(addTime.getTime());
    }
    public void setAddTime(String format){
        try {
            Date temp = simpleDateFormat.parse(format);
            addTime = Calendar.getInstance();
            addTime.setTime(temp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
