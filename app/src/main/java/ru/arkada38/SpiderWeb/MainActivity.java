package ru.arkada38.SpiderWeb;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import static ru.arkada38.SpiderWeb.Settings.MAX_LVL;
import static ru.arkada38.SpiderWeb.Settings.NUMBER_OF_LVL;
import static ru.arkada38.SpiderWeb.Settings.sPref;

public class MainActivity extends AppCompatActivity {

    GridView mainGridView;
    LvlAdapter lvlAdapter;
    Intent intent;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        Settings.sPref = getPreferences(Context.MODE_PRIVATE);

        LvlKeeper.initLvls();

        mainGridView = (GridView) findViewById(R.id.gridView);
        lvlAdapter = new LvlAdapter(this, LvlKeeper.getLvls());
        mainGridView.setAdapter(lvlAdapter);
        intent = new Intent(this, LvlActivity.class);

        mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Можно проходить пройденные уровни и ближайщий непройденный уровень
                if (i <= sPref.getInt(MAX_LVL, 0) + 1) {
                    intent.putExtra(NUMBER_OF_LVL, i);
                    startActivity(intent);
                }
                else
                    Toast.makeText(context, R.string.on_enter_to_close_lvl, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        // При возвращении на главную страницу после прохождения уровней,
        // игрок должен увидеть изменения
        lvlAdapter.notifyDataSetChanged();
        super.onResume();
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