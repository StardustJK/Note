package group3.sse.bupt.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


public class PlanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private PlanDatabase dbHepler;

    private EditText editText;
    private View inflate;
    private AlertDialog modifyDialog;
    private AlertDialog.Builder alertbuidler;
    private Plan curPlan;

    private int code=1;//1是添加，2是修改；

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
        alertbuidler=new AlertDialog.Builder(PlanActivity.this);
        inflate = LayoutInflater.from(this).inflate(R.layout.dialog_plan_edit, null);
        editText=inflate.findViewById(R.id.et_content);
        alertbuidler.setView(inflate);
        modifyDialog=alertbuidler.setPositiveButton("完成",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i("hcccc","code:"+code);
                        switch (code){
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





        lv=findViewById(R.id.lv);
        adapter=new PlanAdapter(getApplicationContext(),planList);
        refreshView();
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        new_plan=findViewById(R.id.new_plan);
        new_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code=1;
                editText.setText("");
                modifyDialog.show();

            }
        });
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

        code=2;
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
