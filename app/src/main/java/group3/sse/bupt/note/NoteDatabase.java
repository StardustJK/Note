package group3.sse.bupt.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteDatabase extends SQLiteOpenHelper {
    //表名
    public static final String TABLE_NAME="notes";
    //笔记内容
    public static final String CONTENT="content";
    //笔记的本地ID
    public static final String ID="_id";
    //笔记创建时间
    public static final String TIME="time";
    //标签
    public static final String TAG="mode";
    //云端唯一标识符id
    public static final String OBJECT_ID="objectId";
    //标识，新建
    public static final String ADD="addSign";
    //标识，修改
    public static final String EDIT="editSign";
    //标识，删除
    public static final String DELETE="deleteSign";
    //用户id
    public static final String USER_ID="userId";

    public NoteDatabase(Context context) {
        super(context, "notes", null, 1);
    }

    private static final String DB_CREATE="create TABLE "+TABLE_NAME
            +"("
            +ID+" integer primary key autoincrement,"
            +CONTENT+" text not null,"
            +TIME+" text not null,"
            +TAG+" integer default 1,"
            +OBJECT_ID+" text default null,"
            +ADD+" integer default 0,"
            +EDIT+" integer default 0,"
            +DELETE+" integer default 0,"
            +USER_ID+" text default null)";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
