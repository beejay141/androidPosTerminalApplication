package com.iisysgroup.androidlite;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by Bamitale @Itex on 11/17/2015.
 */
public class SingleImageTitleObject {
    public String title;
    public int icon;
    public String amount;

    public SingleImageTitleObject(String title, int icon, String amount) {
        this.title = title;
        this.icon = icon;
        this.amount = amount;
    }

    public SingleImageTitleObject(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public static class SingleImageTitleAdapter extends BaseAdapter {
        List<SingleImageTitleObject> items;
        Context context;
        int layout = R.layout.network_list_item;

        public SingleImageTitleAdapter(List<SingleImageTitleObject> items, Context context) {
            this.items = items;
            this.context = context;
        }

        public SingleImageTitleAdapter(List<SingleImageTitleObject> items, Context context, int layout) {
            this(items, context);
            this.layout = layout;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(layout, parent, false);

            SingleImageTitleObject object = items.get(position);

            ((TextView) convertView.findViewById(R.id.primaryTitleText)).setText(object.title);
            ((AppCompatImageView) convertView.findViewById(R.id.imageView)).setImageResource(object.icon);

            return convertView;
        }
    }
}
