package com.example.andrea.myapplication.ui_class;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andrea.myapplication.R;
import com.example.andrea.myapplication.other_class.DatabaseString;
import com.example.andrea.myapplication.other_class.ExpandableHeightGridView;
import com.example.andrea.myapplication.other_class.ImageAdapter;
import com.example.andrea.myapplication.other_class.JSONParser;
import com.example.andrea.myapplication.other_class.StringManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.http.client.methods.HttpPostHC4;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;


public class AddActivity extends AppCompatActivity {

    private boolean EDIT = false;
    private boolean MAIN = false;
    private final int RESULT_LOAD_IMAGE = 9999;
    private final int TAKE_PHOTO_CODE = 9998;
    private ImageAdapter adapter;
    private ArrayList<String> adapterList;
    private ImageView imageView;
    private EditText inputName;
    private EditText inputPhone;
    private EditText inputSurname;
    private ArrayList<Boolean> isEditedList;
    private JSONParser jsonParser = new JSONParser();
    private ProgressDialog pDialog;
    private int position;
    private ProgressBar progressBar;
    private TextView text;
    private String uid;
    private Uri uri_extra;
    private ArrayList<String> urlList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        inputName = (EditText) findViewById(R.id.add_name);
        inputSurname = (EditText) findViewById(R.id.add_surname);
        inputPhone = (EditText) findViewById(R.id.add_phone);

        progressBar = (ProgressBar) findViewById(R.id.pBarAdd);

        ExpandableHeightGridView gridView = (ExpandableHeightGridView)findViewById(R.id.add_gridview);
        gridView.setExpanded(true);

        adapterList = new ArrayList<>();
        urlList = new ArrayList<>();
        isEditedList = new ArrayList<>();

        adapter = new ImageAdapter(getApplicationContext(), adapterList);

        gridView.setAdapter(this.adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                EDIT = true;
                MAIN = false;
                position = i;
                showEditMenu(parent, position);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_action_accept_dark);
        uid = getIntent().getStringExtra(DatabaseString.TAG_UID);
        imageView = ((ImageView)findViewById(R.id.add_mainpic));
        text = ((TextView)findViewById(R.id.add_otherpic_txt));
        text.setVisibility(View.INVISIBLE);
        if (uid.matches("-1")) {
            setTitle(R.string.title_activity_add);
            imageView.setTag(R.mipmap.ic_action_person_light);
        } else {
            String name = getIntent().getStringExtra(DatabaseString.TAG_NAME);
            String surname = getIntent().getStringExtra(DatabaseString.TAG_SURNAME);
            String url = getIntent().getStringExtra(DatabaseString.TAG_URL);
            String phone = getIntent().getStringExtra(DatabaseString.TAG_PHONE);

            setTitle(R.string.title_edit_contact);

            inputName.setText(name);
            inputSurname.setText(surname);
            inputPhone.setText(phone);
            urlList = new ArrayList<>(Arrays.asList(new StringManager().divide(url)));
            isEditedList = new ArrayList<>();
            for (int i = 0; i<urlList.size(); i++) {
                this.isEditedList.add(Boolean.FALSE);
            }
            String folder = name + surname + uid + DatabaseString.TAG_SEPARATOR;
            for (int i = 0; i<urlList.size(); i++) {
                String real_url = DatabaseString.image_folder + folder + urlList.get(i);
                this.urlList.remove(i);
                this.urlList.add(i, real_url);
            }
            progressBar.setVisibility(View.VISIBLE);
            Picasso.with(getApplicationContext())
                    .load(urlList.get(0))
                    .resize(300, 300).centerInside()
                    .into(this.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
            imageView.setTag(urlList.get(0));
            urlList.remove(0);
            for (int i = 0; i<urlList.size(); i++) {
                adapterList.add(i, urlList.get(i));
            }
            adapter = new ImageAdapter(getApplicationContext(), adapterList);
            gridView.setAdapter(this.adapter);
            dataSetChanged();
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MAIN = true;
                if (imageView.getTag().equals(R.mipmap.ic_action_person_light)) {
                    EDIT = false;
                    position = 0;
                    showAddMenu(v);
                } else {
                    EDIT = true;
                    position = 0;
                    showEditMenu(v, position);
                }
            }
        });
    }

    public void showAddMenu(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.popup_menu_add, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_photo:
                        more_pic();
                        break;
                }
                return false;
            }
        });
        menu.show();

    }

    public void showEditMenu(View view, final int position) {

        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.popup_menu_edit, menu.getMenu());
        if (MAIN) {
            menu.getMenu().findItem(R.id.delete_photo).setVisible(false);
        }
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_photo:
                        deleteImage(position);
                        break;
                    case R.id.edit_photo:
                        more_pic();
                }
                return false;
            }
        });
        menu.show();
    }

    public void dataSetChanged() {
        if (adapterList.size() == 0) {
            text.setVisibility(View.INVISIBLE);
        } else {
            text.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    public void more_pic() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.choose_an_option);
        builder.setItems(R.array.options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        pickImage();
                        break;
                    case 1:
                        takeImage();
                        break;
                }
            }
        });
        builder.create().show();
    }

    public void deleteImage(int i)
    {
        adapter.removeItemList(adapterList, i);
        urlList.remove(i);
        dataSetChanged();
    }

    public void pickImage() {
        startActivityForResult(new Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI), RESULT_LOAD_IMAGE);
    }

    public void takeImage() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photoFile = null;
        try {
            photoFile = createImageFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (photoFile != null) {
            intent.putExtra("output", Uri.fromFile(photoFile));
            uri_extra = Uri.fromFile(photoFile);
            startActivityForResult(intent, TAKE_PHOTO_CODE);
        }
    }

    public File createImageFile() throws IOException {
        String path = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date());
        path = Environment.DIRECTORY_DCIM + "/IMG_" + path;
        return new File(new File(Environment.getExternalStorageDirectory().getPath()), path + ".jpg");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            switch (requestCode) {
                case RESULT_LOAD_IMAGE: {
                    if ((resultCode == -1) && (!EDIT) && (!MAIN)) {
                        adapter.addItemList(adapterList, data.getDataString());
                        dataSetChanged();
                        urlList.add(getRealPath(data.getData()));
                        if (!isEditedList.isEmpty()) {
                            isEditedList.add(Boolean.TRUE);
                        }
                        return;
                    } else if ((resultCode == -1) && (EDIT) && (!MAIN)) {
                        adapter.editItemList(adapterList, data.getDataString(), position);
                        dataSetChanged();
                        urlList.remove(position);
                        urlList.add(position, getRealPath(data.getData()));
                        if (!isEditedList.isEmpty()) {
                            isEditedList.remove((position + 1));
                            isEditedList.add((position + 1), Boolean.TRUE);
                        }
                        return;
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        Picasso.with(getApplicationContext())
                                .load(data.getData())
                                .resize(300, 300)
                                .centerInside()
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                        imageView.setTag(getRealPath(data.getData()));
                        imageView.invalidate();
                        if (!isEditedList.isEmpty()) {
                            isEditedList.remove(0);
                            isEditedList.add(0, Boolean.TRUE);
                        }
                    }
                }
                break;

                case TAKE_PHOTO_CODE: {
                    if ((resultCode == -1) && (!EDIT) && (!MAIN)) {
                        adapter.addItemList(adapterList, String.valueOf(uri_extra));
                        dataSetChanged();
                        urlList.add(uri_extra.getPath());
                        if (!isEditedList.isEmpty()) {
                            isEditedList.add(Boolean.TRUE);
                        }
                    } else if ((resultCode == -1) && (EDIT) && (!MAIN)) {
                        adapter.editItemList(adapterList, String.valueOf(uri_extra), position);
                        dataSetChanged();
                        urlList.remove(position);
                        urlList.add(position, uri_extra.getPath());
                        if (!isEditedList.isEmpty()) {
                            isEditedList.remove((position + 1));
                            isEditedList.add((position + 1), Boolean.TRUE);
                        }
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        Picasso.with(getApplicationContext())
                                .load(uri_extra)
                                .resize(300, 300)
                                .centerInside()
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                        imageView.setTag(uri_extra.getPath());
                        if (!isEditedList.isEmpty()) {
                            isEditedList.remove(0);
                            isEditedList.add(0, Boolean.TRUE);
                        }
                    }
                }
                break;
            }
        }
    }

    public String getRealPath(Uri uri) {
        String[] strings = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, strings, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case android.R.id.home :
                save();
                break;
            case R.id.action_add_image :
                EDIT = false;
                MAIN = false;
                more_pic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void save() {
        if (checkInfoUser()) {
            if (uid.matches("-1")) {
                new CreateNewUser().execute();
            } else {
                new UpdateUserDetails().execute();
            }
        }
    }

    public boolean checkInfoUser() {
        String name = this.inputName.getText().toString();
        String surname = this.inputSurname.getText().toString();
        String phone = this.inputPhone.getText().toString();
        if ((name.isEmpty())
                || (surname.isEmpty())
                || (phone.isEmpty())
                || (this.imageView.getTag().equals(R.mipmap.ic_action_person_light))) {
            Toast.makeText(getApplicationContext(), "Impossibile salvare il contatto: " +
                    "controlla di aver compilato correttamente i parametri richiesti", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    public String createName(String string) {
        return string.substring(string.lastIndexOf("/") + 1);
    }



    class CreateNewUser extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AddActivity.this);
            pDialog.setMessage("Aggiunta Contatto In Corso");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
            urlList.add(0, String.valueOf(imageView.getTag()));
            for (int i=0; i<urlList.size(); i++){
                new uploadFile().execute(urlList.get(i));
                String str = createName((urlList.get(i)));
                urlList.remove(i);
                urlList.add(i, str);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringManager manager = new StringManager();
                String name = inputName.getText().toString();
                String surname = inputSurname.getText().toString();
                String url = manager.merge(Arrays.copyOf(urlList.toArray(), urlList.size(), String[].class));
                String phone = inputPhone.getText().toString();

                String params = String.format("name=%s&surname=%s&url=%s&phone=%s",
                        URLEncoder.encode(name, "UTF-8"),
                        URLEncoder.encode(surname, "UTF-8"),
                        URLEncoder.encode(url, "UTF-8"),
                        URLEncoder.encode(phone, "UTF-8"));

                JSONObject json = jsonParser.makeHttpRequest(DatabaseString.url_create_user, "POST", params);
                Log.d("JSON", String.valueOf(json));
                int success = json.getInt(DatabaseString.TAG_SUCCESS);
                if (success == 1) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
            catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();
        }
    }

    class UpdateUserDetails extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog =  new ProgressDialog(AddActivity.this);
            pDialog.setMessage("Modifica Contatto in Corso");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
            urlList.add(0, String.valueOf(imageView.getTag()));
            for (int i = 0; i<urlList.size(); i++) {
                if (isEditedList.get(i)) {
                    new uploadFile().execute(urlList.get(i));
                } else {
                    new moveFile().execute(createName(urlList.get(i)));
                }
                String str;
                str = createName(urlList.get(i));
                urlList.remove(i);
                urlList.add(i, str);
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringManager manager = new StringManager();
                String name = inputName.getText().toString();
                String surname = inputSurname.getText().toString();
                String phone = inputPhone.getText().toString();
                String url = manager.merge(Arrays.copyOf(urlList.toArray(), urlList.size(), String[].class));
                String params = String.format("id=%s&name=%s&surname=%s&url=%s&phone=%s",
                        URLEncoder.encode(uid, "UTF-8"),
                        URLEncoder.encode(name, "UTF-8"),
                        URLEncoder.encode(surname, "UTF-8"),
                        URLEncoder.encode(url, "UTF-8"),
                        URLEncoder.encode(phone, "UTF-8"));
                JSONObject json = jsonParser.makeHttpRequest(DatabaseString.url_update_user, "POST", params);
                int success = json.getInt(DatabaseString.TAG_SUCCESS);
                if (success == 1) {
                    Intent intent = new Intent(getApplicationContext(), UserInfoActivity.class);
                    intent.putExtra("id", uid).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setResult(99, intent);
                    startActivity(intent);
                }
            }
            catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();
        }
    }

    class uploadFile extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String pathToFile = strings[0];
            Log.d("PATH", pathToFile);
            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            ByteArrayBody body = new ByteArrayBody(stream.toByteArray(), createName(pathToFile));
            builder.addPart("uploaded_file", body);
            HttpPostHC4 httpPostHC4 = new HttpPostHC4(DatabaseString.url_upload_image);
            httpPostHC4.setEntity(builder.build());
            try {
                HttpClientBuilder.create().build().execute(httpPostHC4);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class moveFile extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String imageName = strings[0];

            Log.d("IMAGE_NAME", imageName);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("image_name", imageName);
            builder.addTextBody("id", uid);
            HttpPostHC4 httpPostHC4 = new HttpPostHC4(DatabaseString.url_move_image);
            httpPostHC4.setEntity(builder.build());
            try {
                HttpClientBuilder.create().build().execute(httpPostHC4);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
