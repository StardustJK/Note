package group3.sse.bupt.note;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class PlanActivity extends AppCompatActivity {

    private PlanDatabase dbHepler;

    private Button new_plan;
    private Context context=this;
    private ListView lv;
    private PlanAdapter adapter;
    private List<Plan> planList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        lv=findViewById(R.id.lv);
        adapter=new PlanAdapter(getApplicationContext(),planList);
        refreshView();
        lv.setAdapter(adapter);
        new_plan=findViewById(R.id.new_plan);
        new_plan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PlanActivity.this,EditPlan.class);
                startActivityForResult(intent,1);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        //new plan
        String content=data.getExtras().getString("content",null);
        String time=data.getExtras().getString("time",null);
        Plan newPlan=new Plan(content,time);
        Log.i("hcccc","newplan"+newPlan.getTime());
        DBConnector dbConnector=new DBConnector(context);
        dbConnector.open();
        dbConnector.addPlan(newPlan);
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
}
