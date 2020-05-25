package group3.sse.bupt.note.Alarm;

import android.app.AlarmManager;

import android.content.Context;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import java.util.List;

import group3.sse.bupt.note.R;

public class PlanAdapter extends BaseAdapter implements Filterable {

    private Context mContext;

    private List<Plan> backupList;//用来备份原始数据
    private List<Plan> planList;//这个数据是会改变的，所以要有个变量来备份一下原始数据
    PlanAdapter.MyFilter mFilter;
    CheckBox checkBox;
    AlarmUtils alarmUtils;

    public PlanAdapter(Context context,List<Plan> planList){
        this.mContext=context;
        this.planList=planList;
        backupList=planList;

        alarmUtils=new AlarmUtils(mContext);

    }

    @Override
    public int getCount() {
        return planList.size();
    }

    @Override
    public Object getItem(int position) {
        return planList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        mContext.setTheme(R.style.DayTheme);
        View v = View.inflate(mContext, R.layout.adapter_plan_layout, null);
        TextView tv_content = (TextView)v.findViewById(R.id.tv_content);
        TextView tv_time = (TextView)v.findViewById(R.id.tv_time);

        //Set text for TextView
        tv_time.setText(planList.get(position).getTime());

        //Save plan id to tag
        v.setTag(planList.get(position).getId());

        checkBox=v.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Plan plan=planList.get(position);
                    plan.setIsDone(1);
                    modifyPlan(plan);
                    alarmUtils.cancelAlarm(plan);
                }
                else {
                    Plan plan=planList.get(position);
                    plan.setIsDone(0);
                    modifyPlan(plan);
                    alarmUtils.startAlarm(plan);
                }
            }
        });

        //已完成
        if(planList.get(position).getIsDone()==1){
            checkBox.setChecked(true);
            String content=planList.get(position).getContent();
            SpannableString sp = new SpannableString(content);

            sp.setSpan(new StrikethroughSpan(), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv_content.setText(sp);
        }
        else{
            checkBox.setChecked(false);
            tv_content.setText(planList.get(position).getContent());
        }


        return v;
    }

    public void modifyPlan(Plan plan){
        DBConnector dbConnector=new DBConnector(mContext);
        dbConnector.open();
        dbConnector.updatePlan(plan);
        dbConnector.close();
        refreshView();
    }


    @Override
    public Filter getFilter() {
        if (mFilter ==null){
            mFilter = new PlanAdapter.MyFilter();
        }
        return mFilter;
    }
    public void refreshView(){
        DBConnector dbConnector=new DBConnector(mContext);
        dbConnector.open();
        if(planList.size()>0) planList.clear();
        planList.addAll(dbConnector.getAllPlans());

        dbConnector.close();
        notifyDataSetChanged();
    }

    class MyFilter extends Filter {
        //我们在performFiltering(CharSequence charSequence)这个方法中定义过滤规则
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults result = new FilterResults();
            List<Plan> list;
            if (TextUtils.isEmpty(charSequence)) {//当过滤的关键字为空的时候，我们则显示所有的数据
                list = backupList;
            } else {//否则把符合条件的数据对象添加到集合中
                list = new ArrayList<>();
                for (Plan plan : backupList) {
                    if (plan.getContent().contains(charSequence)) {
                        list.add(plan);
                    }

                }
            }
            result.values = list; //将得到的集合保存到FilterResults的value变量中
            result.count = list.size();//将集合的大小保存到FilterResults的count变量中

            return result;
        }
        //在publishResults方法中告诉适配器更新界面
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            planList = (List<Plan>)filterResults.values;
            if (filterResults.count>0){
                notifyDataSetChanged();//通知数据发生了改变
            }else {
                notifyDataSetInvalidated();//通知数据失效
            }
        }
    }
}
