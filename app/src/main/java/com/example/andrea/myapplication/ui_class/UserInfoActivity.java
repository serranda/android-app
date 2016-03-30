
package com.example.andrea.myapplication.ui_class;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.andrea.myapplication.other_class.DatabaseString;
import com.example.andrea.myapplication.other_class.ExpandableHeightGridView;
import com.example.andrea.myapplication.other_class.ImageAdapter;
import com.example.andrea.myapplication.other_class.JSONParser;
import com.example.andrea.myapplication.R;
import com.example.andrea.myapplication.other_class.StringManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;


public class UserInfoActivity extends AppCompatActivity {

    private String uid;
    private String url;
    private String name;
    private String name_default;
    private String surname;
    private String surname_default;
    private String phone;
    private String folder;
    private ProgressDialog pDialog;
    private JSONParser jsonParser = new JSONParser();
    private TextView txtName;
    private TextView txtSurname;
    private TextView txtPhone;
    private int show_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        uid = getIntent().getStringExtra(DatabaseString.TAG_UID);
        show_value = getIntent().getIntExtra(getString(R.string.show_key), Integer.valueOf(getString(R.string.show_order_default)));

        new GetUserDetails().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {

            case (R.id.delete_contact):
                delete();
                break;

            case (R.id.edit_contact):
                Intent intent = new Intent(this, AddActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(DatabaseString.TAG_UID, uid);
                intent.putExtra(DatabaseString.TAG_NAME, name_default);
                intent.putExtra(DatabaseString.TAG_SURNAME, surname_default);
                intent.putExtra(DatabaseString.TAG_URL, url);
                intent.putExtra(DatabaseString.TAG_PHONE, phone);

                startActivityForResult(intent, 99);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==99){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(99, intent);
        startActivity(intent);

        super.onBackPressed();
    }

    public void delete (){
        new DeleteUser().execute();
    }

    class GetUserDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UserInfoActivity.this);
            pDialog.setMessage("Caricamento Dettagli Contatto");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        protected String doInBackground(String... strings) {

            int success;

            try {

                String params = String.format("id=%s", URLEncoder.encode(uid, "UTF-8"));

                JSONObject json = jsonParser.makeHttpRequest(DatabaseString.url_user_detail, "GET", params);

                success = json.getInt(DatabaseString.TAG_SUCCESS);

                if (success == 1) {

                    JSONArray userObj = json.getJSONArray(DatabaseString.TAG_USER);

                    JSONObject user = userObj.getJSONObject(0);

                    txtName = (TextView) findViewById(R.id.info_name);
                    txtSurname = (TextView) findViewById(R.id.info_surname);
                    txtPhone = (TextView) findViewById(R.id.info_phone);

                    name_default = user.getString(DatabaseString.TAG_NAME);
                    surname_default = user.getString(DatabaseString.TAG_SURNAME);
                    phone = user.getString(DatabaseString.TAG_PHONE);
                    url = user.getString(DatabaseString.TAG_URL);

                    folder = user.getString(DatabaseString.TAG_NAME)
                            + user.getString(DatabaseString.TAG_SURNAME)
                            + uid
                            + DatabaseString.TAG_SEPARATOR;

                    switch (show_value){
                        case 0:
                            name = user.getString(DatabaseString.TAG_NAME);
                            surname = user.getString(DatabaseString.TAG_SURNAME);
                            break;
                        case 1:
                            name = user.getString(DatabaseString.TAG_SURNAME);
                            surname = user.getString(DatabaseString.TAG_NAME);
                            break;
                    }
                }
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(String file_url) {

            StringManager manager = new StringManager();

            txtName.setText(name);
            txtSurname.setText(surname);
            txtPhone.setText(phone);

            ExpandableHeightGridView grid = (ExpandableHeightGridView) findViewById(R.id.info_grid);
            grid.setExpanded(true);

            ArrayList<String> photo = new ArrayList<>(Arrays.asList(manager.divide(url)));

            final ProgressBar bar = (ProgressBar) findViewById(R.id.pBarInfo);
            bar.setVisibility(View.VISIBLE);

            ImageView imagePhoto = (ImageView) findViewById(R.id.info_mainpic);

            Picasso.with(getApplicationContext())
                    .load(DatabaseString.image_folder + folder + photo.get(0))
                    .resize(1000, 1000)
                    .centerCrop()
                    .into(imagePhoto, new Callback() {
                @Override
                public void onSuccess() {
                    bar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {

                }
            });

            photo.remove(0);

            if (!photo.isEmpty()) {

                ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext(), photo);
                for (int i=0; i<photo.size(); i++){
                    imageAdapter.editItemList(photo, DatabaseString.image_folder+folder+photo.get(i), i);
                }

                grid.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
            }

            setTitle(name + " " + surname);
            ImageView phone_pic = (ImageView) findViewById(R.id.phone_pic);
            phone_pic.setImageResource(R.mipmap.ic_action_call_light);
            pDialog.dismiss();
        }
    }

    class DeleteUser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(UserInfoActivity.this);
            pDialog.setMessage("Cancellazione Utente");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            int success;
            try {
                String params = String.format("id=%s&name=%s&surname=%s",
                        URLEncoder.encode(uid, "UTF-8"),
                        URLEncoder.encode(name_default, "UTF-8"),
                        URLEncoder.encode(surname_default, "UTF-8"));

                JSONObject json = jsonParser.makeHttpRequest( DatabaseString.url_delete_user,
                        "POST", params);

                success = json.getInt(DatabaseString.TAG_SUCCESS);
                if (success == 1) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setResult(99, intent);
                    startActivity(intent);
                }
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
        }
    }
}
