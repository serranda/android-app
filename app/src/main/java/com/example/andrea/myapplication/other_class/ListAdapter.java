package com.example.andrea.myapplication.other_class;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.andrea.myapplication.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/*
 * Created by andrea on 31/03/15.
 */
public class ListAdapter extends BaseAdapter implements Filterable {


    private JSONArray array;
    private LayoutInflater inflater;
    private ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private ArrayList<HashMap<String, String>> temp_list = new ArrayList<>();
    private String name;
    private String surname;
    private int order_value;
    private int show_value;
    private Comparator<HashMap<String, String>> nameComparator =
            new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> h1, HashMap<String, String> h2) {
                    return (h1.get("name") + h1.get("surname")).compareTo(h2.get("name") + h2.get("surname"));
                }
            };
    private Comparator<HashMap<String, String>> surnameComparator =
            new Comparator<HashMap<String, String>>() {
                @Override
                public int compare(HashMap<String, String> h1, HashMap<String, String> h2) {
                    return (h1.get("surname") + h1.get("name")).compareTo(h2.get("surname") + h2.get("name"));
                }
            };


    public ListAdapter(Context context, ArrayList<HashMap<String, String>> arrayList, JSONArray jsonArray, int order, int show)
    {
        list = arrayList;
        temp_list = arrayList;
        array = jsonArray;
        order_value = order;
        show_value = show;
        inflater = LayoutInflater.from(context);

        for (int i=0; i<jsonArray.length(); i++)
        {
                try
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String uid = jsonObject.getString(DatabaseString.TAG_UID);
                    name = jsonObject.getString(DatabaseString.TAG_NAME);
                    surname = jsonObject.getString(DatabaseString.TAG_SURNAME);
                    String url = jsonObject.getString(DatabaseString.TAG_URL);
                    String phone = jsonObject.getString(DatabaseString.TAG_PHONE);

                    url = new StringManager().cut(url);

                    HashMap<String, String> map = new HashMap<>();

                    map.put(DatabaseString.TAG_UID, uid);
                    map.put(DatabaseString.TAG_NAME, name);
                    map.put(DatabaseString.TAG_SURNAME, surname);
                    map.put(DatabaseString.TAG_URL, url);
                    map.put(DatabaseString.TAG_PHONE, phone);

                    list.add(map);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

        }
        sort_list(list, order_value);
        temp_list = list;
    }


    public Filter getFilter()
    {
        return new Filter()
        {
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                FilterResults results = new FilterResults();
                charSequence = charSequence.toString().toLowerCase();
                ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();
                if (charSequence.toString().length() > 0)
                {
                    int i = 0;
                    while (i < list.size())
                    {
                        try
                        {
                            JSONObject o = array.getJSONObject(i);
                            if ((o.getString(DatabaseString.TAG_NAME).toLowerCase().contains(charSequence))
                                    || (o.getString(DatabaseString.TAG_SURNAME).toLowerCase().contains(charSequence)))
                            {
                                String id = o.getString(DatabaseString.TAG_UID);
                                String name = o.getString(DatabaseString.TAG_NAME);
                                String surname = o.getString(DatabaseString.TAG_SURNAME);
                                String url = o.getString(DatabaseString.TAG_URL);
                                String phone = o.getString(DatabaseString.TAG_PHONE);

                                url = new StringManager().cut(url);

                                HashMap<String, String> map = new HashMap<>();

                                map.put(DatabaseString.TAG_UID, id);
                                map.put(DatabaseString.TAG_NAME, name);
                                map.put(DatabaseString.TAG_SURNAME, surname);
                                map.put(DatabaseString.TAG_URL, url);
                                map.put(DatabaseString.TAG_PHONE, phone);

                                arrayList.add(map);
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        sort_list(arrayList, order_value);
                        results.count = arrayList.size();
                        results.values = arrayList;
                        i++;
                    }
                } else
                {
                    results.count = list.size();
                    results.values = list;
                }
                return results;
            }

            protected void publishResults(CharSequence charSequence, FilterResults results)
            {
                temp_list = (ArrayList<HashMap<String, String>>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return temp_list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflater.inflate(R.layout.row, viewGroup, false);
        }

        final ProgressBar bar = (ProgressBar)view.findViewById(R.id.pBarList);
        bar.setVisibility(View.VISIBLE);
        ImageView imageView = (ImageView)view.findViewById(R.id.main_photo);
        Picasso.with(view.getContext())
                .load(DatabaseString.image_folder
                        + (temp_list.get(position)).get(DatabaseString.TAG_NAME)
                        + (temp_list.get(position)).get(DatabaseString.TAG_SURNAME)
                        + (temp_list.get(position)).get(DatabaseString.TAG_UID)
                        + DatabaseString.TAG_SEPARATOR
                        + (temp_list.get(position)).get(DatabaseString.TAG_URL))
                .resize(200, 200)
                .centerCrop()
                .transform(new RoundedTransformation(75, 5))
                .into(imageView, new Callback() {
                    public void onError() {
                    }

                    public void onSuccess() {
                        bar.setVisibility(View.GONE);
                    }
                });
        ((TextView) view.findViewById(R.id.main_uid)).setText(temp_list.get(position).get("id"));
        switch (show_value) {

            case 0:
                name = temp_list.get(position).get(DatabaseString.TAG_NAME);
                surname = temp_list.get(position).get(DatabaseString.TAG_SURNAME);
                break;
            case 1:
                name = temp_list.get(position).get(DatabaseString.TAG_SURNAME);
                surname = temp_list.get(position).get(DatabaseString.TAG_NAME);
                break;
        }
            ((TextView) view.findViewById(R.id.main_name)).setText(name);
            ((TextView) view.findViewById(R.id.main_surname)).setText(surname);

            return view;
    }

    public void sort_list(ArrayList<HashMap<String, String>> arrayList, int i)
    {
        switch(i) {
            case 0:
            {
                Collections.sort(arrayList, nameComparator);
                break;
            }
            case 1:
            {
                Collections.sort(arrayList, surnameComparator);
                break;
            }
        }
        notifyDataSetChanged();
    }
}
