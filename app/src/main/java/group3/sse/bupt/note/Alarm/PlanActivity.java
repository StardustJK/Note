package group3.sse.bupt.note.Alarm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import group3.sse.bupt.note.Alarm.PlanAdapter;
import group3.sse.bupt.note.R;


public class PlanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private PlanDatabase dbHepler;

    private EditText editText;
    private View inflate;
    private AlertDialog modifyDialog;
    private AlertDialog date_time_picker;
    private Plan curPlan;

    private int mode=1;//1是添加，2是修改；

    private Button btn_time;
    private Button new_plan;
    private Context context=this;
    private ListView lv;
    private PlanAdapter adapter;
    private List<Plan> planList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);

        //dialog
        AlertDialog.Builder alertbuidler = new AlertDialog.Builder(PlanActivity.this);
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_plan_edit, null);
        editText=inflate.findViewById(R.id.et_content);
        btn_time=inflate.findViewById(R.id.btn_time);
        alertbuidler.setView(inflate);
        modifyDialog= alertbuidler.setPositiveButton("完成",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("hcccc","mode:"+mode);
                        switch (mode){
                            case 2:
                                Plan modifiedPlan=new Plan(editText.getText().toString(),curPlan.getTime());
                                modifiedPlan.setId(curPlan.getId());
                                modifyPlan(modifiedPlan);
                                break;
                            case 1:
                                Plan newplan=new Plan();
                                int []dateArray=new int[3];
                                int []timeArray=new int[2];
                                dateArray[0]=newplan.getYear();
                                dateArray[1]=newplan.getMonth()+1;
                                dateArray[2]=newplan.getDay();
                                timeArray[0]=newplan.getHour();
                                timeArray[1]=newplan.getMinute();
                                newplan.setTime(dateArray[0]+"-"+dateArray[1]+"-"+dateArray[2]+" "+timeArray[0]+":"+timeArray[1]);
                                newplan.setContent(editText.getText().toString());
                                newPlan(newplan);
                                break;

                        }


                    }
                }
        ).create();



        //设定时间
        AlertDialog.Builder timebuilder = new AlertDialog.Builder(this);
        View timeView = View .inflate(this, R.layout.dialog_date_time, null);
        final DatePicker datePickerStart =  timeView .findViewById(R.id.date_picker);
        final TimePicker timePickerStart = timeView .findViewById(R.id.time_picker);
        timebuilder.setView(timeView);

        timePickerStart.setIs24HourView(true);
        hideYear(datePickerStart);
//        if (datePickerStart != null) {
//            try {
//                Field f = datePickerStart.getClass().getDeclaredField("mYearSpinner");
//                f.setAccessible(true);
//                LinearLayout l = (LinearLayout) f.get(datePickerStart);
//                l.setVisibility(View.GONE);
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }

        date_time_picker=timebuilder.create();

        lv=findViewById(R.id.lv);
        adapter=new PlanAdapter(getApplicationContext(),planList);
        refreshView();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        new_plan=findViewById(R.id.new_plan);
        new_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode=1;
                editText.setText("");
                modifyDialog.show();

            }
        });

        btn_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date_time_picker.show();
            }
        });
    }

    private void hideYear(DatePicker datePicker) {
        //安卓5.0以上的处理
        int daySpinnerId = Resources.getSystem().getIdentifier("year", "id", "android");
        if (daySpinnerId != 0) {
            View daySpinner = datePicker.findViewById(daySpinnerId);
            if (daySpinner != null) {
                daySpinner.setVisibility(View.GONE);
            }
        }

//        Field[] datePickerFields = datePicker.getClass().getDeclaredFields();
//        for (Field field : datePickerFields) {
//            // 其中mYearSpinner为DatePicker中为“年”定义的变量名
//            if (field.getName().equals("mYearPicker")
//                    || field.getName().equals("mYearSpinner")) {
//                Log.i("hcccc","myearspinner found");
//                field.setAccessible(true);
//                Object dayPicker = new Object();
//                try {
//                    dayPicker = field.get(datePicker);
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                }
//                ((View) dayPicker).setVisibility(View.GONE);
//            }
//        }
    }

    public void newPlan(Plan newPlan){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        dbConnector.addPlan(newPlan);
        dbConnector.close();
        refreshView();
    }

    public void modifyPlan(Plan plan){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        dbConnector.updatePlan(plan);
        dbConnector.close();
        refreshView();
    }

    //添加Plan后刷新界面
    public void refreshView(){
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        if(planList.size()>0) planList.clear();
        planList.addAll(dbConnector.getAllPlans());

        dbConnector.close();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        mode=2;
        //通过dialog修改plan的内容
        switch (parent.getId()){
          case R.id.lv:
              Log.i("hcccc","onclicked");
              curPlan=(Plan) parent.getItemAtPosition(position);
              editText.setText(curPlan.getContent());

              modifyDialog.show();
              break;
      }

    }
}