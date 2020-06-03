package group3.sse.bupt.note;



/*笔记类*/
public class Note {
    private long id;
    private String content;
    private String time;
    private int tag;//标签分类

    public Note(){}
    public Note(String content,String time,int tag){
        this.content=content;
        this.time=time;
        this.tag=tag;
    }


    @Override
    public String toString(){
        return content+"\n"+time.substring(5,16)+" "+id;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
