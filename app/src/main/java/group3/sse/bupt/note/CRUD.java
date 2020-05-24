package group3.sse.bupt.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

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

public Note addNote(Note note){
    ContentValues contentValues=new ContentValues();
    contentValues.put(NoteDatabase.CONTENT,note.getContent());
    contentValues.put(NoteDatabase.TIME,note.getTime());
    contentValues.put(NoteDatabase.TAG,note.getTag());
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
