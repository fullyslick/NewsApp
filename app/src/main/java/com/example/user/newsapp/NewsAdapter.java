package com.example.user.newsapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Alexander Rashkov on 6/16/2017.
 */

public class NewsAdapter extends ArrayAdapter<News> {

    /**
     * Constructs a new {@link NewsAdapter}.
     *
     * @param context of the app
     * @param news    is the list of news, which is the data source of the adapter
     */
    public NewsAdapter(Activity context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        // Find the news at the given position in the list of books
        News currentNews = getItem(position);

        //Find the TextView with ID "item_title" from news_list_item.xml
        TextView titleTextView = (TextView) listItemView.findViewById(R.id.item_title);
        //Get the Title from the currentNews object and set this text on the TextView.
        titleTextView.setText(currentNews.getTitle());

        //Find the TextView with ID "item_section" from news_list_item.xml
        TextView sectionTextView = (TextView) listItemView.findViewById(R.id.item_section);

        //Get the Section from the currentNews object and set this text on the TextView.
        sectionTextView.setText(currentNews.getSection());

        //Find the TextView with ID "item_date" from news_list_item.xml
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.item_date);

        //Get the Section from the currentNews object and set this text on the TextView.
        dateTextView.setText(currentNews.getDate());
        return listItemView;
    }
}
