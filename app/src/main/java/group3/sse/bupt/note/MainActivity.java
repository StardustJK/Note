package group3.sse.bupt.note;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private NoteDatabase dbHelper;
    private NoteAdapter adapter;
    private List<Note> noteList=new ArrayList<>();
    FloatingActionButton btn;
    TextView textView;
    private ListView listView;//一条一条排列
    final String TAG="tag";
    private Context context=this;
    private Toolbar myToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn=findViewById(R.id.floatingActionButton1);
        listView=findViewById(R.id.listView);
        myToolbar=findViewById(R.id.myToolbar);
        adapter=new NoteAdapter(context,noteList);
        refreshListView();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        myToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);//换成菜单图标

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从主页跳转到编辑笔记界面
                Intent intent=new Intent(MainActivity.this,EditActivity.class);
                intent.putExtra("mode",4);//新建笔记
                startActivityForResult(intent,0);//传回结果
            }
        });
    }
    //接收startActivityForResult的结果
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        int returnMode;//-1代表什么都不干，0代表新建笔记，1代表编辑当前笔记
        long note_id;
        returnMode=data.getExtras().getInt("mode",-1);
        note_id=data.getExtras().getLong("id",0);

        if(returnMode==0){
        String content=data.getExtras().getString("content");
        String time=data.getExtras().getString("time");
        int tag=data.getExtras().getInt("tag",1);
        Note newNote=new Note(content,time,tag);
        CRUD op=new CRUD(context);
        op.open();
        op.addNote(newNote);
        op.close();}
        else if(returnMode==1){
            String content=data.getExtras().getString("content");
            String time=data.getExtras().getString("time");
            int tag=data.getExtras().getInt("tag",1);
            Note newNote=new Note(content,time,tag);
            newNote.setId(note_id);
            CRUD op=new CRUD(context);
            op.open();
            op.updateNote(newNote);
            op.close();
        }
        else if(returnMode==2){//删除
            Note curNote=new Note();
            curNote.setId(note_id);
            CRUD op=new CRUD(context);
            op.open();
            op.removeNote(curNote);
            op.close();

        }
        refreshListView();//更改完就刷新一次
        super.onActivityResult(requestCode, resultCode, data);

    }
    public void refreshListView(){
        CRUD op=new CRUD(context);
        op.open();
        if(noteList.size()>0)noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        adapter.notifyDataSetChanged();
    }

    //监听某个元素的点击
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.listView:
                Note curNote = (Note) parent.getItemAtPosition(position);//当前笔记
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);//编辑一个已有笔记模式
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);//从编辑页面返回结果
                break;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }
}
