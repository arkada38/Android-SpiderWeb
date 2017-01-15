package ru.arkada38.SpiderWeb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import static ru.arkada38.SpiderWeb.Settings.TAG;

public class DrawView extends View {

    enum Resize { INCREASE, DECREASE }

    private float width = 0; // Ширина игрового поля
    private float height = 0; // Высота игрового поля

    private boolean isLvlComplete = false;
    // Номер паучка за одним из 10 указателей
    private int[] pointer = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    private boolean isPointerActive = false; // Есть ли касание паучка у указателя

    private int numberOfLvl; // Номер текущего уровня
    private int maxLvl = 0; // Максимальный пройденный уровень игроком
    private float scale = 1; // Размер паучков

    LvlActivity lvlActivity;
    Paint p, t, c;
    Bitmap bitmapSource, bitmap1, bitmap2, bitmap3, bitmap4;
    ColorFilter filter1, filter2, filter3, filter4;
    Matrix matrix;

    // region Spiders images
    float[] cmData1 = new float[]{
            1, 0, 0, 0, 0,
            0, 1, 0, 0, 0,
            0, 0, 1, 0, 0,
            0, 0, 0, 1, 0};
    float[] cmData2 = new float[]{
            0.7f, 0.2f, 0.1f, 0, 0,
            0.2f, 0.7f, 0.1f, 0, 0,
            0.1f, 0.2f, 0.7f, 0, 0,
            0, 0, 0, 1, 0,};
    float[] cmData3 = new float[]{
            0.8f, 0.1f, 0.1f, 0, 0,
            0.1f, 0.8f, 0.1f, 0, 0,
            0.1f, 0.1f, 0.8f, 0, 0,
            0, 0, 0, 1, 0,};
    float[] cmData4 = new float[]{// Оттенки серого
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0.3f, 0.59f, 0.11f, 0, 0,
            0, 0, 0, 1, 0,};
    // endregion

    float[] spiders = {100, 100, 700, 700}; // Инициализация 2 паучков по X, Y
    int[] web = {0, 1}; // Инициализация паутинок между 1 и 2 паучком
    int[] webType = {0}; // Инициализация пересечений паутинки между паучками (не пересечено)

    public DrawView(LvlActivity lvlActivity, int numberOfLvl) {
        super(lvlActivity);
        this.lvlActivity = lvlActivity;
        this.numberOfLvl = numberOfLvl;

        View canvas = lvlActivity.findViewById(R.id.canvas);

        // Загрузка масштаба и максисального уровня из настроек
        scale = Settings.getScale();
        maxLvl = Settings.getMaxLvl();

        width = canvas.getWidth();
        height = canvas.getHeight();

        // Кисть для отрисовки паутинок
        p = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Кисть для отрисовки номера уровня
        t = new Paint();
        t.setStrokeWidth(15);
        t.setTextSize(367);
        t.setTextAlign(Paint.Align.CENTER);

        // Кисть для отрисовки связанных с выделенными указателями паучками
        c = new Paint();
        c.setARGB(138, 30, 140, 50);

        bitmapSource = BitmapFactory.decodeResource(getResources(), ru.arkada38.SpiderWeb.R.drawable.spiders);
        matrix = new Matrix();
        matrix.reset();
        matrix.setScale(scale, scale);

        filter1 = new ColorMatrixColorFilter(new ColorMatrix(cmData1));
        filter2 = new ColorMatrixColorFilter(new ColorMatrix(cmData2));
        filter3 = new ColorMatrixColorFilter(new ColorMatrix(cmData3));
        filter4 = new ColorMatrixColorFilter(new ColorMatrix(cmData4));

        bitmap1 = Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.getWidth()/2, bitmapSource.getHeight()/2, matrix, true);
        bitmap2 = Bitmap.createBitmap(bitmapSource, bitmapSource.getWidth()/2, 0, bitmapSource.getWidth()/2, bitmapSource.getHeight()/2, matrix, true);
        bitmap3 = Bitmap.createBitmap(bitmapSource, 0, bitmapSource.getHeight()/2, bitmapSource.getWidth()/2, bitmapSource.getHeight()/2, matrix, true);
        bitmap4 = Bitmap.createBitmap(bitmapSource, bitmapSource.getWidth()/2, bitmapSource.getHeight()/2, bitmapSource.getWidth()/2, bitmapSource.getHeight()/2, matrix, true);

        loadLvl(numberOfLvl);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Отрисовка номера уровня
        Rect textBounds = new Rect();
        String lvl = String.valueOf(numberOfLvl + 1);
        t.getTextBounds(lvl, 0, lvl.length(), textBounds);
        // Контур
        t.setStyle(Paint.Style.STROKE);
        t.setARGB(138, 255, 255, 255);
        canvas.drawText(lvl, width / 2, height / 2 + textBounds.height() / 2, t);
        // Заполнение
        t.setStyle(Paint.Style.FILL);
        t.setARGB(138, 130, 140, 150);
        canvas.drawText(lvl, width / 2, height / 2 + textBounds.height() / 2, t);

        // region Отрисовка выделителей связанных с выделенным паучком
        boolean[] ourSpiders = new boolean[spiders.length / 2];
        // Выбираем паучков, захваченных указателями
        for (int numberOfSpider : pointer)
            if (numberOfSpider != -1)
                ourSpiders[numberOfSpider / 2] = true;

        boolean[] ourSpiders2 = ourSpiders.clone();
        // Выбираем дополнительно паучков, связанных паутинками с захваченными указателями
        for (int i = 0; i < ourSpiders.length; i++)
            if (ourSpiders[i])
                for (int j = 0; j < web.length; j += 2) {
                    if (i == web[j] || i == web[j + 1]) {
                        ourSpiders2[web[j    ]] = true;
                        ourSpiders2[web[j + 1]] = true;
                    }
                }

        // Удаляем выделенных указателями паучков
        for (int numberOfSpider : pointer)
            if (numberOfSpider != -1)
                ourSpiders2[numberOfSpider / 2] = false;

        // Рисуем выделитель
        for (int i = 0; i < ourSpiders2.length; i++)
            if (ourSpiders2[i])
                canvas.drawCircle(spiders[i * 2], spiders[i * 2 + 1], (float) (bitmap1.getWidth() / 2 * 1.1), c);

        // endregion

        // region Отрисовка связей
        for (int i = 0; i < web.length; i += 2){

            //Проверка на пересечение
            webType[i / 2] = 0;
            float ax1 = spiders[web[i] * 2];
            float ay1 = spiders[web[i] * 2 + 1];
            float ax2 = spiders[web[i + 1] * 2];
            float ay2 = spiders[web[i + 1] * 2 + 1];
            for (int j = 0; j < web.length; j += 2){
                if (i != j){
                    float bx1 = spiders[web[j] * 2];
                    float by1 = spiders[web[j] * 2 + 1];
                    float bx2 = spiders[web[j + 1] * 2];
                    float by2 = spiders[web[j + 1] * 2 + 1];
                    float v1 = (bx2 - bx1) * (ay1 - by1) - (by2 - by1) * (ax1 - bx1);
                    float v2 = (bx2 - bx1) * (ay2 - by1) - (by2 - by1) * (ax2 - bx1);
                    float v3 = (ax2 - ax1) * (by1 - ay1) - (ay2 - ay1) * (bx1 - ax1);
                    float v4 = (ax2 - ax1) * (by2 - ay1) - (ay2 - ay1) * (bx2 - ax1);
                    if (v1 * v2 < 0 && v3 * v4 < 0 || (ax1 == ax2 && ay1 == ay2)){
                        webType[i / 2] = 1;
                        break;
                    }
                }
            }

            p.setARGB(255, 255, 255, 255);
            p.setStrokeWidth(15);
            canvas.drawLine(
                    spiders[web[i] * 2], spiders[web[i] * 2 + 1], // От первого паучка
                    spiders[web[i + 1] * 2], spiders[web[i + 1] * 2 + 1], // Ко второму
                    p);

            p.setStrokeWidth(7);
            if (webType[i / 2] == 0)
                p.setARGB(155, 88, 88, 88);
            else
                p.setARGB(255, 170, 0, 26);

            canvas.drawLine(
                    spiders[web[i] * 2], spiders[web[i] * 2 + 1], // От первого паучка
                    spiders[web[i + 1] * 2], spiders[web[i + 1] * 2 + 1], // Ко второму
                    p);
        }
        // endregion

        // region Отрисовка паучков
        for (int i = 0; i < spiders.length; i += 2){
            p.setARGB(255, 33, 33, 33);

            if ((i/2)%16 >= 0)
                p.setColorFilter(filter1);
            if ((i/2)%16 > 4)
                p.setColorFilter(filter2);
            if ((i/2)%16 > 8)
                p.setColorFilter(filter3);
            if ((i/2)%16 > 12)
                p.setColorFilter(filter4);

            switch ((i/2)%4){
                case 0:
                    canvas.drawBitmap(bitmap1, spiders[i] - bitmap1.getWidth() / 2, spiders[i + 1] - bitmap1.getHeight() / 2, p);
                    break;
                case 1:
                    canvas.drawBitmap(bitmap2, spiders[i] - bitmap1.getWidth() / 2, spiders[i + 1] - bitmap1.getHeight() / 2, p);
                    break;
                case 2:
                    canvas.drawBitmap(bitmap3, spiders[i] - bitmap1.getWidth() / 2, spiders[i + 1] - bitmap1.getHeight() / 2, p);
                    break;
                case 3:
                    canvas.drawBitmap(bitmap4, spiders[i] - bitmap1.getWidth() / 2, spiders[i + 1] - bitmap1.getHeight() / 2, p);
                    break;
            }
        }
        p.setColorFilter(filter1);
        // endregion

        // Проверка на прохождение уровня
        int sum = 0;
        for (int aWebType : webType) sum += aWebType;
        isLvlComplete = sum == 0;
    }

    public boolean onTouchEvent(MotionEvent event){

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: // нажатие
            case MotionEvent.ACTION_POINTER_DOWN: // последующие касания
                if (!isLvlComplete) {
                    Display display = lvlActivity.getWindowManager().getDefaultDisplay();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    display.getMetrics(outMetrics);
                    for (int i = 0; i < spiders.length; i += 2) {
                        // Если коснулись паучка
                        if (
                                event.getX(event.getActionIndex()) >= spiders[i] - bitmap1.getWidth() / 2 &&
                                event.getX(event.getActionIndex()) <= spiders[i] + bitmap1.getWidth() / 2 &&
                                event.getY(event.getActionIndex()) >= spiders[i + 1] - bitmap1.getHeight() / 2 &&
                                event.getY(event.getActionIndex()) <= spiders[i + 1] + bitmap1.getHeight() / 2
                                ) {
                            // Сохраняем номер паучка соответствующему указателю
                            pointer[event.getActionIndex()] = i;
                            isPointerActive = true;
                            break;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE: // движение
                for (int i = 0; i < event.getPointerCount(); i++) {
                    // Если передвигается указатель с паучком, паучок следует за указателем
                    if (pointer[i] > -1) {
                        spiders[pointer[i]] = Math.max(bitmap1.getWidth() / 2, Math.min(event.getX(i), width - bitmap1.getWidth() / 2));
                        spiders[pointer[i] + 1] = Math.max(bitmap1.getHeight() / 2, Math.min(event.getY(i), height - bitmap1.getHeight() / 2));
                    }
                }
                break;

            case MotionEvent.ACTION_UP: // отпускание
                for (int i = 0; i < 10; i++) {
                    pointer[i] = -1;
                }
                if (isLvlComplete && isPointerActive)
                    onLvlComplete();
                isPointerActive = false;
            case MotionEvent.ACTION_POINTER_UP: // прерывания касаний
                for (int i = event.getActionIndex(); i < 9; i++)
                    pointer[i] = pointer[i + 1];

                // Уровень будет пройден даже при лишних качаниях экрана (без паучков)
                int sum = 0;
                for (int i = 0; i < 10; i++)
                    sum += pointer[i] + 1;

                if (sum == 0 && isLvlComplete && isPointerActive) {
                    onLvlComplete();
                    isPointerActive = false;
                }

                break;
        }

        invalidate();
        return true;
    }

    public void restartLvl() {
        loadLvl(numberOfLvl);
    }

    public void loadLvl(int numberOfLvl){

        // Номер уровня указывается в заголовке
        lvlActivity.setTitle(lvlActivity.getResources().getString(ru.arkada38.SpiderWeb.R.string.Complete1) + " " + (numberOfLvl + 1));

        LvlItem lvlItem = LvlKeeper.getLvl(numberOfLvl);

        float paddingX = Math.max(width - height, 0) / 2;
        float paddingY = Math.max(height - width, 0) / 2;
        float side = Math.min(width, height);

        spiders = new float[lvlItem.getCoordinates().length];
        for (int i = 0; i < lvlItem.getCoordinates().length; i += 2){
            spiders[i] = lvlItem.getCoordinates()[i] * side / 100 + paddingX;
            spiders[i + 1] = lvlItem.getCoordinates()[i+1] * side / 100 + paddingY;
        }

        web = lvlItem.getContacts();
        webType = new int[web.length / 2];

        invalidate();
        Log.d(TAG, getWidth() + " " + getHeight() + " " + this.width + " " + this.height);
    }

    // Изменяет размер паучков и перерисовывает view, сохраняя scale
    public void resize(Resize resize){
        if (resize == Resize.INCREASE) {
            if (scale < 1.7)
                scale += (float) 0.1;
            else
                Toast.makeText(lvlActivity, "It's maximum size", Toast.LENGTH_SHORT).show();
        }
        else {
            if (scale > .2) {
                scale -= (float) 0.1;
            }
            else {
                Toast.makeText(lvlActivity, "It's minimum size", Toast.LENGTH_SHORT).show();
            }
        }

        matrix.reset();
        matrix.setScale(scale, scale);

        bitmap1 = Bitmap.createBitmap(bitmapSource, 0, 0, bitmapSource.getWidth() / 2, bitmapSource.getHeight() / 2, matrix, true);
        bitmap2 = Bitmap.createBitmap(bitmapSource, bitmapSource.getWidth() / 2, 0, bitmapSource.getWidth() / 2, bitmapSource.getHeight()/2, matrix, true);
        bitmap3 = Bitmap.createBitmap(bitmapSource, 0, bitmapSource.getHeight() / 2, bitmapSource.getWidth() / 2, bitmapSource.getHeight()/2, matrix, true);
        bitmap4 = Bitmap.createBitmap(bitmapSource, bitmapSource.getWidth() / 2, bitmapSource.getHeight() / 2, bitmapSource.getWidth()/2, bitmapSource.getHeight() / 2, matrix, true);

        invalidate();

        Settings.setScale(scale);
    }

    public void onLvlComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(lvlActivity);

        // Если пройден не последний уровень
        if (numberOfLvl < LvlKeeper.getNumberOfLvls() - 1) {
            builder.setTitle(getResources().getString(R.string.Congratulations))// Заголовок
                    .setMessage(getResources().getString(ru.arkada38.SpiderWeb.R.string.Complete1) + " " + (numberOfLvl + 1) + " " + getResources().getString(R.string.Complete2))// Описание
                    .setCancelable(false)
                    .setNeutralButton(getResources().getString(R.string.action_restart),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    maxLvl = Math.max(numberOfLvl, maxLvl);
                                    MainActivity.submitLvl();

                                    Settings.setMaxLvl(maxLvl);


                                    loadLvl(numberOfLvl);
                                }
                            })
                    .setPositiveButton(getResources().getString(R.string.NextLvl),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    maxLvl = Math.max(numberOfLvl, maxLvl);
                                    numberOfLvl++;
                                    MainActivity.submitLvl();

                                    Settings.setMaxLvl(maxLvl);

                                    loadLvl(numberOfLvl);
                                }
                            })
            ;
            AlertDialog alertI = builder.create();
            alertI.show();
        }
        else {
            builder.setTitle(getResources().getString(ru.arkada38.SpiderWeb.R.string.Congratulations))// Заголовок
                    .setMessage(getResources().getString(ru.arkada38.SpiderWeb.R.string.GameComplete))// Описание
                    .setCancelable(false)
                    .setNeutralButton(getResources().getString(android.R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    maxLvl = LvlKeeper.getNumberOfLvls();
                                    MainActivity.submitLvl();

                                    Settings.setMaxLvl(LvlKeeper.getNumberOfLvls());

                                    numberOfLvl = 0;
                                    loadLvl(numberOfLvl);
                                }
                            })
                    .setPositiveButton(getResources().getString(R.string.liveFeedback),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent browse = new Intent(Intent.ACTION_VIEW , Uri.parse("https://play.google.com/store/apps/details?id=ru.arkada38.SpiderWeb"));
                                    lvlActivity.startActivity(browse);

                                    Settings.setMaxLvl(LvlKeeper.getNumberOfLvls());

                                    numberOfLvl = 0;
                                    loadLvl(numberOfLvl);
                                }
                            })
            ;
            AlertDialog alertII = builder.create();
            alertII.show();
        }
    }

}