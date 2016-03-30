package com.example.andrea.myapplication.other_class;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.andrea.myapplication.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
 * Created by andrea on 02/04/15.
 */
public class ImageAdapter extends BaseAdapter {

    private ArrayList<String> list = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;


    public ImageAdapter(Context c, ArrayList<String> arrayList) {
        this.list = arrayList;
        this.context = c;
        this.inflater = LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflater.inflate(R.layout.grid_image, viewGroup, false);
        }

        final ProgressBar bar = (ProgressBar) view.findViewById(R.id.pBarGrid);
        bar.setVisibility(View.VISIBLE);

        ImageView imageView = (ImageView) view.findViewById(R.id.grid_pic);
        imageView.setPadding(10, 10, 10, 10);

        Picasso.with(context)
                .load(list.get(i))
                .resize(300, 400)
                .centerCrop()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {

                    }
                });

        return view;
    }

    public void addItemList(ArrayList<String> list, String item) {
        list.add(item);
    }

    public void editItemList(ArrayList<String> list, String item, int position) {
        list.remove(position);
        list.add(position, item);
    }

    public void removeItemList(ArrayList<String> list, int position) {
        list.remove(position);
    }



}
