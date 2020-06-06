package group3.sse.bupt.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import group3.sse.bupt.note.CloudSync.SyncUtils;

public class CRUD {
SQLiteOpenHelper dbHandler;
SQLiteDatabase db;

private static final String[] columns={
        NoteDatabase.ID,
        NoteDatabase.CONTENT,
        NoteDatabase.TIME,
        NoteDatabase.TAG
};
public CRUD(Context context){
    dbHandler=new NoteDatabase(context);
}
public void open(){
    db=dbHandler.getWritableDatabase();
}
public void close(){
    dbHandler.close();
}

//新建笔记
    //在原来代码基础上修改，传入的这个note对象，应该是指定了内容、时间、标签三个属性
public Note addNote(Note note){
    boolean isLogin = false;
    //判断是否登录，如果登录的话，要给笔记加上用户id。
    if (SyncUtils.isLogin()){
        note.setUser(SyncUtils.getCurrentUser());
        isLogin =true;
    }
    ContentValues contentValues=new ContentValues();
    contentValues.put(NoteDatabase.CONTENT,note.getContent());
    contentValues.put(NoteDatabase.TIME,note.getTime());
    contentValues.put(NoteDatabase.TAG,note.getTag());
    //如果用户是登录状态，可以获取到当前用户，将用户的id存到本地数据中
    if (isLogin){
        contentValues.put(NoteDatabase.USER_ID,note.getUser().getObjectId());
    }
    //云端数据库不关心本地id，但是本地数据库一定要保存云端id
    if (isLogin){
        note=SyncUtils.addNote(note);
    }
    contentValues.put(NoteDatabase.OBJECT_ID,note.getObjectId());
    contentValues.put(NoteDatabase.ADD,note.getAdd());
    //插入新数据，数据库自动分配id
    long insertID=db.insert(NoteDatabase.TABLE_NAME,null,contentValues);
    note.setId(insertID);
    return note;

}


//通过id查询Note
public Note getNote(long id){
    Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.ID+"=?",
            new String[]{String.valueOf(id)},null,null,null,null);
    if(cursor!=null){
        cursor.moveToFirst();
    }
    Note e=new Note(cursor.getString(1),cursor.getString(2),cursor.getInt(3));
    return e;
}


//获取全部笔记
public List<Note> getAllNotes(){
    Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,null,
            null,null,null,null);
    List<Note> notes=new ArrayList<>();
    if(cursor.getCount()>0){
        while(cursor.moveToNext()){
            Note note=new Note();
            note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
            note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
            note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
            note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
            notes.add(note);
        }
    }
    return notes;
}


//更新笔记
    public int updateNote(Note note) {
        //update the info of an existing note
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.TAG, note.getTag());
        // updating row
        return db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?",new String[] { String.valueOf(note.getId())});
    }
    //删除笔记
    public void removeNote(Note note) {
        //remove a note according to ID value
        db.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(), null);
    }
    //删除一个分类下的所有笔记
    public void removeAllNoteByTag(int tag){
    db.delete(NoteDatabase.TABLE_NAME,NoteDatabase.TAG+"="+tag,null);
    }

}
