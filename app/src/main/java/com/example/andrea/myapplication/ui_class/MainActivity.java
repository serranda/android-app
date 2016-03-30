package com.example.andrea.myapplication.ui_class;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.andrea.myapplication.R;
import com.example.andrea.myapplication.other_class.DatabaseString;
import com.example.andrea.myapplication.other_class.JSONParser;
import com.example.andrea.myapplication.other_class.ListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;



public class MainActivity extends AppCompatActivity {

    private ListAdapter adapter;
    private ProgressDialog dialog;
    private ListView lv;
    private int order_value;
    private SearchView searchView;
    private int show_value;
    private String uid = "-1";
    private JSONArray users;
    private ArrayList<HashMap<String, String>> usersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        order_value = Integer.parseInt(pref.getString("list_order", ""));
        show_value = Integer.parseInt(pref.getString("show_order", ""));

        usersList = new ArrayList<>();

        new LoadAllUsers().execute();

        lv = (ListView) findViewById(R.id.main_list);
        lv.setDivider(null);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                uid = ((TextView) view.findViewById(R.id.main_uid)).getText().toString();

                Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                intent.putExtra(DatabaseString.TAG_UID, uid);
                intent.putExtra(getString(R.string.show_key), show_value);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                MainActivity.this.startActivityForResult(intent, 99);

            }
        });

        lv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                searchView.onActionViewCollapsed();
                return false;
            }
        });

        pref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(getString(R.string.list_key))) {
                    order_value = Integer.parseInt(sharedPreferences.getString("list_order", ""));
                    recreate();
                }

                if (key.equals(getString(R.string.show_key))) {
                    show_value = Integer.parseInt(sharedPreferences.getString("show_order", ""));
                    recreate();
                }
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);
        searchView = ((SearchView) menu.findItem(R.id.list_search).getActionView());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);

        ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text))
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT) {
                            ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                    .hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        }
                        return true;
                    }
                });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    searchView.onActionViewCollapsed();
                }
            }
        });
        this.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String s) {
                if (users != null) {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
            public boolean onQueryTextSubmit(String s)
            {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                return true;
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void addContact (View view){

        Intent intent = new Intent(getApplicationContext(), AddActivity.class);
        intent.putExtra(DatabaseString.TAG_UID, uid).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }


    class LoadAllUsers extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Caricamento in corso");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            JSONParser jParser = new JSONParser();

            JSONObject json = jParser.getJSONFromUrl(DatabaseString.url_all_users);

            try {

                int success = json.getInt(DatabaseString.TAG_SUCCESS);

                if (success == 1) {

                    users = json.getJSONArray(DatabaseString.TAG_USERS);
                    
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            dialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (users!=null) {
                        adapter = new ListAdapter(getApplicationContext(), usersList, users, order_value, show_value);

                        lv.setAdapter(adapter);
                    }
                }
            });
        }
    }


}