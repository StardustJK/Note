package group3.sse.bupt.note;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class NoteDatabase extends SQLiteOpenHelper {
    public static final String TABLE_NAME="notes";
    public static final String CONTENT="content";
    public static final String ID="_id";
    public static final String TIME="time";
    public static final String TAG="mode";

    public NoteDatabase(Context context) {
        super(context, "notes", null, 1);
    }

    private static final String DB_CREATE="create TABLE "+TABLE_NAME
            +"("
            +ID+" integer primary key autoincrement,"
            +CONTENT+" text not null,"
            +TIME+" text not null,"
            +TAG+" integer default 1)";


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
