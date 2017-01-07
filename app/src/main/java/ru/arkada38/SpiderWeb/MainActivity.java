package ru.arkada38.SpiderWeb;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final static String NUMBER_OF_LVL = "ru.arkada38.SpiderWeb.NumberOfLvl";
    static final String TAG = "SpiderWeb";

    GridView mainGridView;
    LvlAdapter lvlAdapter;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LvlKeeper.initLvls();

        mainGridView = (GridView) findViewById(R.id.gridView);
        lvlAdapter = new LvlAdapter(this, LvlKeeper.getLvls());
        mainGridView.setAdapter(lvlAdapter);
        intent = new Intent(this, LvlActivity.class);

        mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, String.valueOf(i));

                intent.putExtra(NUMBER_OF_LVL, i);
                startActivity(intent);
            }
        });
    }

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leaderboard:
                Toast.makeText(this, "leaderboard", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.achievements:
                Toast.makeText(this, "achievements", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.about:
                Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion
}