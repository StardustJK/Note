package group3.sse.bupt.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


public class PlanActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

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
        lv.setOnItemClickListener(this);
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
     //   Log.i("hcccc","newplan"+newPlan.getTime());
        newPlan(newPlan);

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

      switch (parent.getId()){
          case R.id.lv:
              Log.i("hcccc","onclicked");
              final Plan curPlan=(Plan) parent.getItemAtPosition(position);
              AlertDialog.Builder modifyDialog=new AlertDialog.Builder(PlanActivity.this);
              final EditText editText=new EditText(PlanActivity.this);
              editText.setText(curPlan.getContent());
              modifyDialog.setTitle("修改plan").setView(editText);

              modifyDialog.setPositiveButton("完成",
                      new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              Plan modifiedplan=new Plan(editText.getText().toString(),curPlan.getTime());
                              modifiedplan.setId(curPlan.getId());

                              modifyPlan(modifiedplan);
                          }
                      }
              ).show();
              break;
      }
    }
}
