package david.rosic.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowListActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, View.OnClickListener {

    private TaskAdapter adapter;
    private ListView lista;
    private DbHelper dbHelper;
    private String title = "";
    private ImageView btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            title = extras.getString("title");
            TextView tv = findViewById(R.id.show_list_act_title_tv);
            if(!title.isEmpty()){
                tv.setText(title);
            }
        }

        lista = findViewById(R.id.show_list_act_list);
        btnHome = findViewById(R.id.toolbar_home);
        Button btn = findViewById(R.id.show_list_act_btn);

        dbHelper = new DbHelper(this, MainActivity.DB_NAME, null, 1);
        adapter = new TaskAdapter(this, dbHelper);

        Task[] tasks = dbHelper.getListItems(title);
        adapter.update(tasks);
        lista.setAdapter(adapter);

        lista.setOnItemLongClickListener(this);
        btn.setOnClickListener(this);
        btnHome.setOnClickListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Task task = (Task) adapter.getItem(position);
        if(dbHelper.deleteItem(task.getmId())){
            adapter.removeTask(task);
            adapter.removeCheck(position);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.show_list_act_btn:
                EditText et = findViewById(R.id.show_list_act_et);
                String itemName = et.getText().toString();
                if(itemName.isEmpty()){
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                long id = dbHelper.createItem(itemName, title);
                Task task = new Task(et.getText().toString(),false, id);
                et.setText("");
                adapter.addTask(task);
                break;
            case R.id.toolbar_home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}