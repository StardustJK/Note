package group3.sse.bupt.note;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditPlan extends AppCompatActivity {

    private EditText et_content;

    private Plan plan;
    private int []dateArray=new int[3];
    private int []timeArray=new int[2];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plan);
        init();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent();
            intent.putExtra("content",et_content.getText().toString());
            intent.putExtra("time",dateArray[0]+"-"+dateArray[1]+"-"+dateArray[2]+" "+timeArray[0]+":"+timeArray[1]);

            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private void init(){
        plan=new Plan();
        dateArray[0]=plan.getYear();
        dateArray[1]=plan.getMonth()+1;
        dateArray[2]=plan.getDay();
        timeArray[0]=plan.getHour();
        timeArray[1]=plan.getMinute();

        et_content=findViewById(R.id.et_content);

    }
}
