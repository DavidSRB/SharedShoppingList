package david.rosic.shoppinglist;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Task> mTasks;
    private ArrayList<Boolean> mChecked;
    private DbHelper mdbHelper;

    public TaskAdapter(Context context, DbHelper mdbHelper) {
        mTasks = new ArrayList<Task>();
        mChecked = new ArrayList<Boolean>();
        this.mContext = context;
        this.mdbHelper = mdbHelper;
    }

    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public Object getItem(int position) {
        Object returnValue = null;
        try {
            returnValue = mTasks.get(position);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addTask(Task task) {
        mTasks.add(task);
        mChecked.add((Boolean) task.ismChecked());
        notifyDataSetChanged();
    }

    public void removeTask(Task task) {
        mTasks.remove(task);
        notifyDataSetChanged();
    }

    public void removeCheck(int position) {
        mChecked.remove(position);
        notifyDataSetChanged();
    }

    public void update(Task[] tasks) {
        mTasks.clear();
        mChecked.clear();
        if (tasks != null) {
            for (Task task : tasks) {
                addTask(task);
            }
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView mNameViewItem;
        CheckBox mCheckViewItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mNameViewItem = (TextView) convertView.findViewById(R.id.show_list_act_list_item_tv);
            viewHolder.mCheckViewItem = (CheckBox) convertView.findViewById(R.id.show_list_act_list_item_cb);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Task task = (Task) getItem(position);
        Boolean checked;
        try {
            checked = (Boolean) mChecked.get(position);
            task.setmChecked((boolean) checked);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        viewHolder.mNameViewItem.setText(task.getmName());
        if (task.ismChecked()) {
            viewHolder.mCheckViewItem.setChecked(true);
        } else {
            viewHolder.mCheckViewItem.setChecked(false);
        }

        viewHolder.mCheckViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.setmChecked(!task.ismChecked());
                mChecked.set(position, (Boolean) task.ismChecked());
                mdbHelper.setItemState(task.getmId(), task.ismChecked());
            }
        });

        viewHolder.mCheckViewItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    viewHolder.mNameViewItem.setPaintFlags(viewHolder.mNameViewItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    viewHolder.mNameViewItem.setPaintFlags(viewHolder.mNameViewItem.getPaintFlags() & Paint.ANTI_ALIAS_FLAG);
                }
            }
        });

        if (task.ismChecked()) {
            viewHolder.mNameViewItem.setPaintFlags(viewHolder.mNameViewItem.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            viewHolder.mNameViewItem.setPaintFlags(viewHolder.mNameViewItem.getPaintFlags() & Paint.ANTI_ALIAS_FLAG);
        }

        return convertView;
    }
}
