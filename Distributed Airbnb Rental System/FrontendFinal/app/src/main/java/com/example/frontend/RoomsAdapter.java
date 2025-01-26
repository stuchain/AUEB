package com.example.frontend;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class RoomsAdapter extends BaseAdapter {

    // override other abstract methods here
    private LayoutInflater inflater;

    private ArrayList<Room> items;

    public RoomsAdapter(LayoutInflater inflater, ArrayList<Room> items){
        this.inflater = inflater;
        this.items = items;
    }
    // Getters
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, container, false);
        }

        TextView textView = convertView.findViewById(R.id.list_text);
        textView.setText(items.get(position).getRoomName());            // set name of room to text view

        Resources resources = inflater.getContext().getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(resources, getImageResourceByName(items.get(position).getRoomImage(), inflater.getContext()));

        ImageView imageView = convertView.findViewById(R.id.list_image);
        if(bitmap != null) {
            Log.d("DEBUG", "bitmap loaded successfully");
        } else {
            Log.d("DEBUG", "failed to load bitmap");
        }
        imageView.setImageBitmap(bitmap);
        //imageView.setImageResource(getImageResourceByName(items.get(position).getRoomImage(), this.inflater.getContext()));
        return convertView;
    }
    private int getImageResourceByName(String name, Context context) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }
}