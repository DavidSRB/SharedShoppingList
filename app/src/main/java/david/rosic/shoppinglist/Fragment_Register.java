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
 * Use the {@link Fragment_Register#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_Register extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String REGISTER_URL = MainActivity.BASE_URL + "/users";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Fragment_Register() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_Register.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_Register newInstance(String param1, String param2) {
        Fragment_Register fragment = new Fragment_Register();
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment__register, container, false);
        HttpHelper httpHelper = new HttpHelper();

        EditText editTxtUsername = v.findViewById(R.id.frag_reg_et_username);
        EditText editTxtEmail = v.findViewById(R.id.frag_reg_et_email);
        EditText editTxtPass = v.findViewById(R.id.frag_reg_et_pass);

        Button btn = v.findViewById(R.id.frag_reg_btn_reg);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // Get the values from the EditText fields
                String username = editTxtUsername.getText().toString();
                String email = editTxtEmail.getText().toString();
                String password = editTxtPass.getText().toString();

                // Check if the fields are empty
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the email is valid using a regular expression
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(getActivity(), R.string.enter_valie_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the password is at least 6 characters long
                if (password.length() < 6) {
                    Toast.makeText(getActivity(), R.string.pass_length, Toast.LENGTH_SHORT).show();
                    return;
                }


                new Thread(new Runnable() {
                    public void run() {
                        try {
                            JSONObject requestJSON = new JSONObject();
                            requestJSON.put("username", username);
                            requestJSON.put("password", password);
                            requestJSON.put("email", email);
                            boolean resultHTTP = httpHelper.postJSONObjectFromURL(REGISTER_URL,requestJSON);
                            if(resultHTTP) {
                                Intent intent = new Intent(getActivity(),WelcomeActivity.class);
                                intent.putExtra("username",username);
                                intent.putExtra("email",email);
                                intent.putExtra("password",password);
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        startActivity(intent);
                                    }
                                });
                            }else{
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getActivity(), R.string.registration_error, Toast.LENGTH_SHORT).show();
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