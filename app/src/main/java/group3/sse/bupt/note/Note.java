package group3.sse.bupt.note;


import cn.bmob.v3.BmobObject;
import group3.sse.bupt.note.Account.User;

/*笔记类*/
//包装一下JavaBean
public class Note extends BmobObject {
    //本地数据库自动分配的一个id，可能会有冲突
    //所以在同步的时候不同步这个属性
    //云端有个objectId作为唯一标识
    private Long id;
    //笔记内容
    private String content;
    //笔记创建时间
    private String time;

    private Integer tag;//标签分类

    //新增、修改、删除标记
    //优先级删除>新增>修改
    private Boolean add;
    private Boolean edit;
    private Boolean delete;

    //作者
    //是多对一的关系，用pointer表示
    private User user;

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setTag(Integer tag) {
        this.tag = tag;
    }

    public Boolean getAdd() {
        return add;
    }

    public void setAdd(Boolean add) {
        this.add = add;
    }

    public Boolean getEdit() {
        return edit;
    }

    public void setEdit(Boolean edit) {
        this.edit = edit;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
