package ru.arkada38.SpiderWeb;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import static ru.arkada38.SpiderWeb.Settings.MAX_LVL;
import static ru.arkada38.SpiderWeb.Settings.TAG;
import static ru.arkada38.SpiderWeb.Settings.sPref;

public class LvlAdapter extends BaseAdapter {

    private final Context context;
    private final List<LvlItem> lvlItems;

    public LvlAdapter(Context context, List<LvlItem> lvlItems) {
        this.context = context;
        this.lvlItems = lvlItems;

        Log.d(TAG, "LvlAdapter " + sPref.getInt(MAX_LVL, 0));
    }

    @Override
    public int getCount() {
        return lvlItems.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LvlItem lvlItem = lvlItems.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.lvl_item, null);
        }

        final TextView numberOfLvl = (TextView) convertView.findViewById(R.id.numberOfLvl);
        final TextView numberOfSpiders = (TextView) convertView.findViewById(R.id.numberOfSpiders);
        final TextView numberOfConnections = (TextView) convertView.findViewById(R.id.numberOfConnections);
        final TextView isLvlDone = (TextView) convertView.findViewById(R.id.isLvlDone);

        numberOfLvl.setText(String.valueOf(position + 1));
        numberOfSpiders.setText(String.valueOf(lvlItem.getNumberOfSpiders()));
        numberOfConnections.setText(String.valueOf(lvlItem.getNumbersOfConnections()));

        // TODO MaxLvl Visibility and rename fields (string to float error)
        int maxLvl = sPref.getInt(MAX_LVL, 0);
        if (position <= maxLvl && maxLvl > 0)
            isLvlDone.setVisibility(View.VISIBLE);
        else
            isLvlDone.setVisibility(View.INVISIBLE);

        return convertView;
    }
}
