package david.rosic.shoppinglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class ShoppingListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ShoppingList> mShoppingList;

    public ShoppingListAdapter(Context mContext) {
        this.mContext = mContext;
        mShoppingList = new ArrayList<ShoppingList>();
    }

    @Override
    public int getCount() {
        return mShoppingList.size();
    }

    @Override
    public Object getItem(int position) {
        Object returnValue = null;
        try{
            returnValue = mShoppingList.get(position);
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addShoppingList(ShoppingList shoppingList){
        mShoppingList.add(shoppingList);
        notifyDataSetChanged();
    }

    public void removeShoppingList(ShoppingList shoppingList){
        mShoppingList.remove(shoppingList);
        notifyDataSetChanged();
    }

    public void update(ShoppingList[] shoppingLists){
        mShoppingList.clear();
        if(shoppingLists != null){
            for(ShoppingList shoppingList : shoppingLists){
                mShoppingList.add(shoppingList);
            }
        }
        notifyDataSetChanged();
    }

    private class ViewHolder{
        TextView mNameViewItem;
        TextView mSharedViewItem;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.welcome_act_list_element,null);
            viewHolder = new ViewHolder();
            viewHolder.mNameViewItem = (TextView) convertView.findViewById(R.id.welcome_act_list_element_list_name_tv);
            viewHolder.mSharedViewItem = (TextView) convertView.findViewById(R.id.welcome_act_list_element_list_shared_tv);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ShoppingList shoppingList = (ShoppingList) getItem(position);
        viewHolder.mNameViewItem.setText(shoppingList.getmNaslov());
        if(shoppingList.ismShared()){
            viewHolder.mSharedViewItem.setText(R.string.true_boolean);
        }else{
            viewHolder.mSharedViewItem.setText(R.string.false_boolean);
        }

        return convertView;

    }
}
