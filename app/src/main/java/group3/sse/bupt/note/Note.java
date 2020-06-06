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

    //用于分类
    private Integer tag;//标签分类

    //新增、修改、删除标记
    //优先级删除>新增>修改
    //数据库那边也要修改
    //标识只有0和1两个值
    private Integer add;
    private Integer edit;
    private Integer delete;

    //作者
    //是多对一的关系，用pointer表示
    //这个属性是用来区分笔记是哪个用户的，本地数据库保存的是用户的id
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

    public Integer getAdd() {
        return add;
    }

    public void setAdd(Integer add) {
        this.add = add;
    }

    public Integer getEdit() {
        return edit;
    }

    public void setEdit(Integer edit) {
        this.edit = edit;
    }

    public Integer getDelete() {
        return delete;
    }

    public void setDelete(Integer delete) {
        this.delete = delete;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
