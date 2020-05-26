package group3.sse.bupt.note;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import group3.sse.bupt.note.Alarm.PlanActivity;

public class RecycleBinActivity extends BaseActivity implements AdapterView.OnItemClickListener{
    private NoteDatabase dbHelper;
    private NoteAdapter adapter;
    private TagAdapter tagAdapter;
    private List<Note> noteList = new ArrayList<>();


    FloatingActionButton btn;
    private long id=0;
    private ListView listView;//一条一条排列
    final String TAG = "tag";
    private Context context = this;
    private Toolbar myToolbar;
    private SharedPreferences sharedPreferences;
    private int openMode=0;
    public Intent intent =new Intent();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_bar_note:

                    return true;
                case R.id.bottom_bar_plan:
                    Intent intent=new Intent(RecycleBinActivity.this, PlanActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in,R.anim.anim_exit2);
                    RecycleBinActivity.this.finish();
                    return true;
            }

            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(sharedPreferences.contains("nightMode")) {
            boolean nightMode = sharedPreferences.getBoolean("nightMode", false);
            if(nightMode)setTheme(R.style.NightTheme);
            else setTheme(R.style.DayTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.floatingActionButton1);
        listView = findViewById(R.id.listView);
        myToolbar = findViewById(R.id.myToolbar);
        adapter = new NoteAdapter(context, noteList);
        BottomNavigationView BottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
//        BottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("curTag", 0);
        if(!sharedPreferences.contains("reverseMode"))
            editor.putBoolean("reverseMode", false);
        editor.commit();

        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(ListViewLongClickListener);

        myToolbar.setTitle("全部笔记");

        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        refreshListView();

        if (super.isNightMode())
            myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_black_24dp)); // 三道杠


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从主页跳转到编辑笔记界面
                Intent intent = new Intent(RecycleBinActivity.this, EditActivity.class);
                intent.putExtra("mode", 4);//新建笔记
                startActivityForResult(intent, 1);//传回结果
            }
        });


    }

    @Override
    protected void needRefresh() {

    }

    //接收startActivityForResult的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                int returnMode;//-1代表什么都不干，0代表新建笔记，1代表编辑当前笔记
                long note_id;
                returnMode = data.getExtras().getInt("mode", -1);
                note_id = data.getExtras().getLong("id", 0);

                if (returnMode == 0) {
                    String content = data.getExtras().getString("content");
                    String time = data.getExtras().getString("time");
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, tag);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.addNote(newNote);
                    op.close();
                } else if (returnMode == 1) {
                    String content = data.getExtras().getString("content");
                    String time = data.getExtras().getString("time");
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, tag);
                    newNote.setId(note_id);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.updateNote(newNote);
                    op.close();
                } else if (returnMode == 2) {//删除
                    Note curNote = new Note();
                    curNote.setId(note_id);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.removeNote(curNote);
                    op.close();

                }
                int curTag = sharedPreferences.getInt("curTag", 1);
                if (curTag == 0)
                    refreshListView();//更改完就刷新一次
                else refreshTagListView(curTag);
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case 2://从设置返回到全部笔记，并能自动刷新
                if(data!=null){
                    boolean reverseMode= Objects.requireNonNull(data.getExtras()).getBoolean("reverseMode",false);
                    if(reverseMode)refreshListView();
                }}
    }

    //刷新笔记列表
    public void refreshListView() {
        CRUD op = new CRUD(context);
        op.open();
        if (noteList.size() > 0) {
            noteList.clear();
        }
        noteList.addAll(op.getAllNotes());
        //如果未设置正序显示
        if (!sharedPreferences.getBoolean("reverseMode", false))
            Collections.reverse(noteList);
        myToolbar.setTitle("全部笔记");
        adapter = new NoteAdapter(context, noteList);
        listView.setAdapter(adapter);
        op.close();
        adapter.notifyDataSetChanged();
    }

    //刷新标签中的笔记列表
    public void refreshTagListView(int tag) {
        CRUD op = new CRUD(context);
        op.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        List<Note> temp = new ArrayList<>();
        for (int i = 0; i < noteList.size(); i++) {
            if ((noteList.get(i).getTag() == tag)) {
                Note note = noteList.get(i);
                temp.add(note);
            }
        }
        //如果未设置正序显示
        if (!sharedPreferences.getBoolean("reverseMode", false))
            Collections.reverse(temp);

        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
        listView.setAdapter(tempAdapter);

    }

    //监听主页面笔记列表中某个元素的点击
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.listView:
                Note curNote = (Note) parent.getItemAtPosition(position);//当前笔记
                Intent intent = new Intent(RecycleBinActivity.this, EditActivity.class);
                intent.putExtra("content", curNote.getContent());
                intent.putExtra("id", curNote.getId());
                intent.putExtra("time", curNote.getTime());
                intent.putExtra("mode", 3);//编辑一个已有笔记模式
                intent.putExtra("tag", curNote.getTag());
                startActivityForResult(intent, 1);//从编辑页面返回结果
                break;
        }

    }

    //长按笔记列表中某个元素，删除该笔记
    AdapterView.OnItemLongClickListener ListViewLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
            switch (parent.getId()) {
                case R.id.listView:
                    new AlertDialog.Builder(RecycleBinActivity.this)
                            .setMessage("确定删除该笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Note curNote = (Note) parent.getItemAtPosition(position);//当前笔记
                                    curNote.setId(curNote.getId());
                                    CRUD op = new CRUD(context);
                                    op.open();
                                    op.removeNote(curNote);
                                    op.close();
                                    int curTag = sharedPreferences.getInt("curTag", 1);
                                    if (curTag == 0) refreshListView();
                                    else refreshTagListView(curTag);

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();

                    return true;
            }
            return false;
        }
    };

    @Override//生成页面的toolbar
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //search setting
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView mSearchView = (SearchView) mSearch.getActionView();

        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_foreverdelete:
                    new AlertDialog.Builder(RecycleBinActivity.this)
                            .setMessage("是否永久删除吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (openMode == 4){ // new note
                                        intent.putExtra("mode", -1);
                                        setResult(RESULT_OK, intent);
                                    }
                                    else { // existing note
                                        intent.putExtra("mode", 5);
                                        intent.putExtra("id", id);
                                        setResult(RESULT_OK, intent);
                                    }
                                    finish();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                    break;
                case R.id.menu_recover:
                    new AlertDialog.Builder(RecycleBinActivity.this)
                            .setMessage("是否恢复文件？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (openMode == 4){ // new note
                                        intent.putExtra("mode", -1);
                                        setResult(RESULT_OK, intent);
                                    }
                                    else { // existing note
                                        intent.putExtra("mode", 6);
                                        intent.putExtra("id", id);
                                        setResult(RESULT_OK, intent);
                                    }
                                    finish();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                    break;
        }
        return super.onOptionsItemSelected(item);
    }

}


