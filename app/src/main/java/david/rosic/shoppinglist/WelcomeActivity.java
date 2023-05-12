package david.rosic.shoppinglist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final int REQUEST_CODE_NEW_LIST = 1;

    private ShoppingListAdapter adapter;
    private DbHelper dbHelper;
    private static boolean showSharedLists = true;
    private String username = "";
    private Button seeLists;
    private ImageView btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            username = extras.getString("username");
            TextView userTv = findViewById(R.id.welcome_act_user_tv);
            if(!username.isEmpty()){
                userTv.setText(username);
            }
        }

        btnHome = findViewById(R.id.toolbar_home);
        Button newListBtn = findViewById(R.id.welcome_act_new_list_button);
        seeLists = findViewById(R.id.welcome_act_see_lists_button);
        seeLists.setText(R.string.see_my_lists);
        showSharedLists = true;
        ListView lista = findViewById(R.id.welcome_act_list);

        adapter = new ShoppingListAdapter(this);
        lista.setAdapter(adapter);

        dbHelper = new DbHelper(this, MainActivity.DB_NAME, null, 1);
        ShoppingList[] shoppingLists = dbHelper.getAllAccessibleLists(username);
        adapter.update(shoppingLists);

        newListBtn.setOnClickListener(this);
        seeLists.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        lista.setOnItemClickListener(this);
        lista.setOnItemLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.welcome_act_new_list_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.newListAlertDialogTitle);
                builder.setMessage(R.string.newListAlertDialogMessage);
                builder.setPositiveButton(R.string.yesText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(WelcomeActivity.this,NewListActivity.class);
                        startActivityForResult(intent,REQUEST_CODE_NEW_LIST);
                    }
                });
                builder.show();
                break;
            case R.id.welcome_act_see_lists_button:
                showSharedLists = !showSharedLists;
                if(showSharedLists){
                    ShoppingList[] shoppingLists = dbHelper.getAllAccessibleLists(username);
                    adapter.update(shoppingLists);
                    seeLists.setText(R.string.see_my_lists);
                }else{
                    ShoppingList[] shoppingLists = dbHelper.getAllUserLists(username);
                    adapter.update(shoppingLists);
                    seeLists.setText(R.string.see_shared_lists);
                }
                break;
            case R.id.toolbar_home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this,ShowListActivity.class);
        ShoppingList shoppingList = (ShoppingList) adapter.getItem(position);
        intent.putExtra("title", shoppingList.getmNaslov());
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ShoppingList shoppingList = (ShoppingList) adapter.getItem(position);

        if (dbHelper.deleteList(shoppingList.getmNaslov(), username)){
            adapter.removeShoppingList(shoppingList);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NEW_LIST) {
            if (resultCode == RESULT_OK) {
                String title = data.getStringExtra("title");
                boolean shared = data.getBooleanExtra("shared", false);

                if(dbHelper.createList(title, username, shared)){
                    adapter.addShoppingList(new ShoppingList(title,shared));
                }
            }
        }
    }
}