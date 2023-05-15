package david.rosic.shoppinglist;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Fragment_Login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Login extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String LOGIN_URL = MainActivity.BASE_URL + "/login";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_Login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Login.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Login newInstance(String param1, String param2) {
        Fragment_Login fragment = new Fragment_Login();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment__login, container, false);
        HttpHelper httpHelper = new HttpHelper();

        EditText etUsername = v.findViewById(R.id.frag_login_et_username);
        EditText etPass = v.findViewById(R.id.frag_login_et_pass);

        Button btnLogin = v.findViewById(R.id.frag_login_btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String password = etPass.getText().toString();

                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            JSONObject requestJSON = new JSONObject();
                            requestJSON.put("username", username);
                            requestJSON.put("password", password);
                            boolean resultHTTP = httpHelper.postJSONObjectFromURL(LOGIN_URL,requestJSON);
                            if(resultHTTP) {
                                Intent intent = new Intent(getActivity(),WelcomeActivity.class);
                                intent.putExtra("username",username);
                                intent.putExtra("password",password);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        startActivity(intent);
                                    }
                                });
                            }else{
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), R.string.login_error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        return v;
    }
}