package com.example.concentration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.annotation.RequiresApi;

public class MainActivity extends Activity {
    private static int ROW_COUNT = -1;
    private static int COL_COUNT = -1;
    private static int REMAINDER = -1;
    public static boolean PORTRAIT_MODE = true;

    private Context context;
    private Drawable backImage;
    private int[][] cards;

    private List<Drawable> images;
    private Card firstCard;
    private Card secondCard;
    private ButtonListener buttonListener;

    private static final Object lock = new Object();

    int turns;
    private TableLayout mainTable;
    private UpdateCardsHandler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        handler = new UpdateCardsHandler();
        loadImages();
        setContentView(R.layout.activity_main);

        OrientationEventListener orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                PORTRAIT_MODE = ((orientation < 100) || (orientation > 280));
                if (PORTRAIT_MODE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                Log.w("Orient", orientation + " PORTRAIT_MODE = " + PORTRAIT_MODE);
            }
        };

        orientationListener.enable();
        backImage = getResources().getDrawable(R.drawable.yugi_card_back);

        buttonListener = new ButtonListener();

        mainTable = findViewById(R.id.TableLayout03);


        context = mainTable.getContext();

        Spinner s = findViewById(R.id.Spinner01);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);


        s.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> arg0,
                    View arg1, int pos, long arg3) {

                ((Spinner) findViewById(R.id.Spinner01)).setSelection(0);

                int c, r;

                switch (pos) {
                    case 1:
                        c = 2;
                        r = 2;
                        break;
                    case 2:
                        c = 2;
                        r = 3;
                        break;
                    case 3:
                        c = 4;
                        r = 2;
                        break;
                    case 4:
                        c = 5;
                        r = 2;
                        break;
                    case 5:
                        c = 4;
                        r = 3;
                        break;
                    case 6:
                        c = 7;
                        r = 2;
                        break;
                    case 7:
                        c = 4;
                        r = 4;
                        break;
                    case 8:
                        c = 9;
                        r = 2;
                        break;
                    case 9:
                        c = 5;
                        r = 4;
                        break;
                    default:
                        return;
                }
                newGame(c, r);

            }


            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }

        });
    }

    private void newGame(int c, int r) {
        ROW_COUNT = r;
        COL_COUNT = c;

        cards = new int[COL_COUNT][ROW_COUNT];


        mainTable.removeView(findViewById(R.id.TableRow01));
        mainTable.removeView(findViewById(R.id.TableRow02));

        TableRow tr = findViewById(R.id.TableRow03);
        tr.removeAllViews();


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int displayHeight = size.y;


        mainTable = new TableLayout(context);
        mainTable.setMinimumHeight(displayHeight);

        tr.setWeightSum(1);

        mainTable.setWeightSum(ROW_COUNT+1);
        mainTable.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
//        TableLayout.LayoutParams outParams = new TableLayout.LayoutParams();
        tr.addView(mainTable);
        tr.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);

        for (int y = 0; y < ROW_COUNT; y++) {

            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.weight = 1;
//            params.height = 0;
//            params.bottomMargin = 10;
            TableRow tempRow = createRow(y);
            mainTable.addView(tempRow, params);
        }

        firstCard = null;
        loadCards();

        turns = 0;
        ((TextView) findViewById(R.id.tv1)).setText("Tries: " + turns);
    }

    private void loadImages() {
        images = new ArrayList<>();
        images.add(getResources().getDrawable(R.drawable.bongo_cat));
        images.add(getResources().getDrawable(R.drawable.boot_cat));
        images.add(getResources().getDrawable(R.drawable.coughing_cat));
        images.add(getResources().getDrawable(R.drawable.fall_cat));
        images.add(getResources().getDrawable(R.drawable.fly_cat));
        images.add(getResources().getDrawable(R.drawable.grumpy_cat));
        images.add(getResources().getDrawable(R.drawable.pocket_kitty));
        images.add(getResources().getDrawable(R.drawable.rail_cat));
        images.add(getResources().getDrawable(R.drawable.sad_cat));
        images.add(getResources().getDrawable(R.drawable.singing_cat));
        images.add(getResources().getDrawable(R.drawable.smudge_cat));
    }

    private void loadCards(){
        try{
            int size = ROW_COUNT*COL_COUNT;

            Log.i("loadCards()","size=" + size);

            ArrayList<Integer> list = new ArrayList<>();

            for(int i=0;i<size;i++){
                list.add(i);
            }


            Random r = new Random();

            for(int i=size-1;i>=0;i--){
                int t=0;

                if(i>0){
                    t = r.nextInt(i);
                }

                t= list.remove(t);
                cards[i%COL_COUNT][i/COL_COUNT]=t%(size/2);

                Log.i("loadCards()", "card["+(i%COL_COUNT)+
                        "]["+(i/COL_COUNT)+"]=" + cards[i%COL_COUNT][i/COL_COUNT]);
            }
        }
        catch (Exception e) {
            Log.e("loadCards()", e+"");
        }

    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(float dp, Context context) {
        return (int) (dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private TableRow createRow(int y) {
        TableRow row = new TableRow(context);
        row.setHorizontalGravity(Gravity.CENTER);

        for (int x = 0; x < COL_COUNT; x++) {
            View view = createImageButton(x, y);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.weight = 1;
            params.height = convertDpToPixel(105, context);
            params.width = convertDpToPixel(70, context);
            row.addView(view, params);
        }
        return row;
    }

    private View createImageButton(int x, int y) {
        Button button = new Button(context);
        button.setBackgroundDrawable(backImage);
        button.setId(100 * x + y);
        button.setOnClickListener(buttonListener);
        return button;
    }

    class ButtonListener implements OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized (lock) {
                if (firstCard != null && secondCard != null) {
                    return;
                }
                int id = v.getId();
                int x = id / 100;
                int y = id % 100;
                turnCard((Button) v, x, y);
            }

        }

        private void turnCard(Button button, int x, int y) {
            button.setBackgroundDrawable(images.get(cards[x][y]));

            if (firstCard == null) {
                firstCard = new Card(button, x, y);
            } else {

                if (firstCard.x == x && firstCard.y == y) {
                    return; //the user pressed the same card
                }

                secondCard = new Card(button, x, y);

                turns++;
                ((TextView) findViewById(R.id.tv1)).setText("Tries: " + turns);


                TimerTask tt = new TimerTask() {

                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
                        try {
                            synchronized (lock) {
                                handler.sendEmptyMessage(0);
                            }
                        } catch (Exception e) {
                            Log.e("E1", Objects.requireNonNull(e.getMessage()));
                        }
                    }
                };

                Timer t = new Timer(false);
                t.schedule(tt, 1300);
            }


        }

    }

    class UpdateCardsHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            synchronized (lock) {
                checkCards();
            }
        }

        public void checkCards() {
            if (cards[secondCard.x][secondCard.y] == cards[firstCard.x][firstCard.y]) {
                firstCard.button.setVisibility(View.INVISIBLE);
                secondCard.button.setVisibility(View.INVISIBLE);
            } else {
                secondCard.button.setBackgroundDrawable(backImage);
                firstCard.button.setBackgroundDrawable(backImage);
            }

            firstCard = null;
            secondCard = null;
        }
    }
}