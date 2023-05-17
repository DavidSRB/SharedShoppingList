package david.rosic.shoppinglist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class NewListActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv;
    private EditText et;
    private ImageView btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_list);

        Button btn_title = findViewById(R.id.new_list_act_title_btn);
        Button btn_save = findViewById(R.id.new_list_act_save_btn);
        tv = findViewById(R.id.new_list_act_title_tv);
        et = findViewById(R.id.new_list_act_title_et);
        btnHome = findViewById(R.id.toolbar_home);

        btn_title.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btnHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.new_list_act_title_btn:
                if (et.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.empty_title_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                tv.setText(et.getText());
                break;
            case R.id.new_list_act_save_btn:
                String title = tv.getText().toString();
                if (title.equals(R.string.naslov)) {
                    Toast.makeText(this, R.string.list_no_title_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                DbHelper dbHelper = new DbHelper(this, MainActivity.DB_NAME, null, 1);
                if (dbHelper.doesListExist(title)) {
                    Toast.makeText(this, R.string.list_exists_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent();

                RadioButton yes = findViewById(R.id.new_list_act_yes_radio_btn);
                RadioButton no = findViewById(R.id.new_list_act_no_radio_btn);
                if (yes.isChecked()) {
                    intent.putExtra("shared", true);
                } else if (no.isChecked()) {
                    intent.putExtra("shared", false);
                } else {
                    Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("title", title);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
            case R.id.toolbar_home:
                intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
        }
    }
}