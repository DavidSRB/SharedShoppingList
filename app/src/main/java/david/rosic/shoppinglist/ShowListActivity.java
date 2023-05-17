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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

public class ShowListActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, View.OnClickListener {

    private TaskAdapter adapter;
    private ListView lista;
    private DbHelper dbHelper;
    private String title = "";
    private ImageView btnHome;
    private boolean shared;
    private boolean userOwned;
    public static String TASK_URL = MainActivity.BASE_URL + "/tasks";
    private HttpHelper httpHelper;
    private Button refreshBtn;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_list);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            title = extras.getString("title");
            shared = extras.getBoolean("shared");
            userOwned = extras.getBoolean("userOwned");
            TextView tv = findViewById(R.id.show_list_act_title_tv);
            if (!title.isEmpty()) {
                tv.setText(title);
            }
        }

        lista = findViewById(R.id.show_list_act_list);
        btnHome = findViewById(R.id.toolbar_home);
        addBtn = findViewById(R.id.show_list_act_add_btn);
        refreshBtn = findViewById(R.id.show_list_act_refresh_btn);

        dbHelper = new DbHelper(this, MainActivity.DB_NAME, null, 1);
        httpHelper = new HttpHelper();
        adapter = new TaskAdapter(this, dbHelper, httpHelper);

        if (shared && userOwned) {
            refreshBtn.setVisibility(View.GONE);
            fetchTasks();
        } else if (shared) {
            refreshBtn.setVisibility(View.VISIBLE);
            addBtn.setEnabled(false);
        } else {
            refreshBtn.setVisibility(View.GONE);
            Task[] tasks = dbHelper.getListItems(title);
            adapter.update(tasks);
        }

        lista.setAdapter(adapter);

        lista.setOnItemLongClickListener(this);
        addBtn.setOnClickListener(this);
        btnHome.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        Task task = (Task) adapter.getItem(position);
        long taskId = task.getmId();

        if (shared) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray = httpHelper.getJSONArrayFromURL(TASK_URL + "/" + title);

                        String tasksMongoId = "";

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String name = jsonObject.getString("name");
                            boolean done = jsonObject.getBoolean("done");
                            long mojId = Long.parseLong(jsonObject.getString("taskId"),16);
                            tasksMongoId = jsonObject.getString("_id");

                            if (taskId == mojId) {
                                break;
                            }
                        }
                        boolean returnCode = httpHelper.httpDelete(TASK_URL + "/" + tasksMongoId);
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        //Possible check errors. The httpDelete returnCode & dbHelper.deleteItem result are never checked
        dbHelper.deleteItem(task.getmId());
        adapter.removeTask(task);
        adapter.removeCheck(position);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_list_act_add_btn:
                EditText et = findViewById(R.id.show_list_act_et);
                String itemName = et.getText().toString();
                if (itemName.isEmpty()) {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                Random random = new Random();
                long randomLongId = Math.abs(random.nextInt());

                if (shared) {
                    CreteItemRunnable runnable = new CreteItemRunnable(itemName, title, Long.toHexString(randomLongId), httpHelper, adapter);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }

                boolean successIndicator = dbHelper.createItem(itemName, title, randomLongId);
                if (!shared) {
                    Task task = new Task(et.getText().toString(), false, randomLongId);
                    adapter.addTask(task);
                }
                break;
            case R.id.toolbar_home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case R.id.show_list_act_refresh_btn:
                refreshBtn.setVisibility(View.GONE);
                addBtn.setEnabled(true);
                fetchTasks();

                break;
        }
    }

    private void fetchTasks() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = httpHelper.getJSONArrayFromURL(TASK_URL + "/" + title);

                    Task[] tasks = new Task[jsonArray.length()];

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("name");
                        boolean done = jsonObject.getBoolean("done");
                        long id = Long.parseLong(jsonObject.getString("taskId"), 16);

                        tasks[i] = new Task(name, done, id);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.update(tasks);
                        }
                    });

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class CreteItemRunnable implements Runnable{
        private String mTaskId;
        private String mItemName;
        private String mTitle;
        private HttpHelper mHttpHelper;
        private TaskAdapter mTaskAdapter;

        CreteItemRunnable(String itemName, String title, String taskId, HttpHelper httpHelper, TaskAdapter taskAdapter) {
            mTaskId = taskId;
            mItemName = itemName;
            mTitle = title;
            mHttpHelper = httpHelper;
            mTaskAdapter = taskAdapter;
        }

        public void run() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", mItemName);
                jsonObject.put("list", mTitle);
                jsonObject.put("done", false);
                jsonObject.put("taskId", mTaskId);

                boolean returnCode = mHttpHelper.postJSONObjectFromURL(TASK_URL, jsonObject);

                if (!returnCode) {
                    return;
                }

                //TODO: promeniti ovo
                Task task = new Task(mItemName, false, Long.parseLong(mTaskId, 16));
                ShowListActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mTaskAdapter.addTask(task);
                    }
                });
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }
}