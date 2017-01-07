package ru.arkada38.SpiderWeb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Random;

public class LvlActivity extends AppCompatActivity {

    LinearLayout layout;
    ViewTreeObserver observer;
    private DrawView canvas;
    private int numberOfLvl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lvl);

        // Установка градиентного фона
        ImageView imageView = (ImageView) findViewById(R.id.background);
        imageView.setImageResource(R.drawable.shape);

        // region Установка изображения в нижнем правом углу
        imageView = (ImageView) findViewById(R.id.back_image);
        // Получаем id фоновой картинки
        Random r = new Random();
        int i = r.nextInt(8) + 1;
        int image = getResources().getIdentifier("back" + i, "drawable",
                getApplicationContext().getPackageName());
        // Отображаем картинку
        imageView.setImageResource(image);
        // Устанавливаем fade in анимацию
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imageView.setAnimation(anim);
        // endregion

        // Получаем номер уровня, который нужно загрузить
        Intent intent = getIntent();
        numberOfLvl = intent.getIntExtra(MainActivity.NUMBER_OF_LVL, 0);

        layout = (LinearLayout) findViewById(R.id.canvas);

        observer = layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (canvas == null) {
                            canvas = new DrawView(LvlActivity.this, numberOfLvl);

                            layout.removeAllViews();
                            // Помещаем view с паучками в layout текущего activity
                            layout.addView(canvas);
                        }
                    }
                });
    }

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lvl_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.increase:
                canvas.resize(DrawView.Resize.INCREASE);
                return true;
            case R.id.decrease:
                canvas.resize(DrawView.Resize.DECREASE);
                return true;
            case R.id.restart:
                canvas.restartLvl();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

}
