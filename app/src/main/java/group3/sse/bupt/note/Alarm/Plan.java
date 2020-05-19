package group3.sse.bupt.note.Alarm;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Plan {

    private long id;//自增长，建立是数据库的时候设置该列自增长
    private String content;
    private Calendar planTime;
    private boolean isDone;


    public Plan(String content, String time) {
        this.content = content;
        setTime(time);
        isDone=false;
    }

    public Plan(){
        this.planTime = Calendar.getInstance();
        isDone=false;
    }

    public void setTime(String format){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

    public boolean getIsDone() {
        return isDone;
    }
    public void setIsDone(boolean isDone){
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return simpleDateFormat.format(planTime.getTime());
    }
}
