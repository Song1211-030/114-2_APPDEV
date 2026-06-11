package com.example.myapplication;

import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AquariumActivity extends AppCompatActivity {

    private FrameLayout tank;
    private TextView fishCountText;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable gameLoop;
    private boolean running = true;
    private Random random = new Random();
    private List<FishActor> fishActors = new ArrayList<>();
    private List<FoodItem> foodItems = new ArrayList<>();

    private static class FishActor {
        ImageView view;
        FishData data;
        float x, y, tx, ty;
        float speed = 1.2f;
        int dir = 1;
    }

    private static class FoodItem {
        View view;
        float x, y;
        float speed;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aquarium);

        tank = findViewById(R.id.tank);
        fishCountText = findViewById(R.id.fishCountText);

        tank.post(new Runnable() {
            @Override
            public void run() {
                refreshFishViews();
                startGameLoop();
            }
        });

        findViewById(R.id.feedAllButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropFood();
            }
        });

        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = false;
                handler.removeCallbacks(gameLoop);
                finish();
            }
        });
    }

    private void startGameLoop() {
        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                updateFish();
                updateFood();
                checkEat();
                handler.postDelayed(this, 30);
            }
        };
        handler.post(gameLoop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tank.getWidth() > 0) {
            refreshFishViews();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;
        handler.removeCallbacks(gameLoop);
    }

    private void refreshFishViews() {
        tank.removeAllViews();
        fishActors.clear();
        foodItems.clear();

        int count = FishCollection.fishList.size();
        fishCountText.setText("魚缸裡有 " + count + " 條魚");

        int tankW = Math.max(tank.getWidth(), 600);
        int tankH = Math.max(tank.getHeight(), 600);

        for (FishData data : FishCollection.fishList) {
            ImageView iv = new ImageView(this);
            int s = data.getSize();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(s, s);
            iv.setLayoutParams(params);
            iv.setImageResource(data.getImageResId());

            FishActor a = new FishActor();
            a.view = iv;
            a.data = data;
            a.x = 30 + random.nextFloat() * (tankW - s - 60);
            a.y = 30 + random.nextFloat() * (tankH - s - 60);
            pickNewTarget(a, tankW, tankH);

            iv.setX(a.x);
            iv.setY(a.y);
            tank.addView(iv);
            fishActors.add(a);
        }
    }

    private void pickNewTarget(FishActor a, int tankW, int tankH) {
        int s = a.data.getSize();
        a.tx = 30 + random.nextFloat() * Math.max(tankW - s - 60, 100);
        a.ty = 30 + random.nextFloat() * Math.max(tankH - s - 60, 100);
    }

    private void dropFood() {
        int tankW = Math.max(tank.getWidth(), 600);
        int count = 3 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            final View food = new View(this);
            int fs = 18;
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(fs, fs);
            food.setLayoutParams(p);

            ShapeDrawable d = new ShapeDrawable(new OvalShape());
            d.getPaint().setColor(Color.rgb(255, 180, 50));
            food.setBackground(d);

            float fx = 40 + random.nextFloat() * (tankW - 80);
            food.setX(fx);
            food.setY(-fs * (i + 1) * 2);
            tank.addView(food);

            FoodItem fi = new FoodItem();
            fi.view = food;
            fi.x = fx;
            fi.y = -fs * (i + 1) * 2;
            fi.speed = 2.5f + random.nextFloat();
            foodItems.add(fi);
        }
    }

    private void updateFish() {
        int tankW = Math.max(tank.getWidth(), 600);
        int tankH = Math.max(tank.getHeight(), 600);

        for (FishActor a : fishActors) {
            float dx = a.tx - a.x;
            float dy = a.ty - a.y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < 8) {
                pickNewTarget(a, tankW, tankH);
            }

            if (dist > 0) {
                float step = Math.min(a.speed, dist);
                a.x += (dx / dist) * step;
                a.y += (dy / dist) * step;
            }

            a.view.setX(a.x);
            a.view.setY(a.y);

            if (dx < 0 && a.dir == 1) {
                a.dir = -1;
                a.view.setScaleX(-1);
            } else if (dx > 0 && a.dir == -1) {
                a.dir = 1;
                a.view.setScaleX(1);
            }
        }
    }

    private void updateFood() {
        int tankH = tank.getHeight();

        Iterator<FoodItem> it = foodItems.iterator();
        while (it.hasNext()) {
            FoodItem fi = it.next();
            fi.y += fi.speed;
            fi.view.setY(fi.y);

            if (fi.y > tankH + 40) {
                tank.removeView(fi.view);
                it.remove();
            }
        }
    }

    private void checkEat() {
        Iterator<FoodItem> fit = foodItems.iterator();
        while (fit.hasNext()) {
            FoodItem fi = fit.next();
            FishActor nearest = null;
            float nearDist = 150;

            for (FishActor a : fishActors) {
                float dx = a.x - fi.x;
                float dy = a.y - fi.y;
                float d = (float) Math.sqrt(dx * dx + dy * dy);

                if (d < nearDist) {
                    nearDist = d;
                    nearest = a;
                }
            }

            if (nearest != null) {
                nearest.tx = fi.x;
                nearest.ty = fi.y;
                nearest.speed = 2.5f;
            }

            if (nearDist < 35 && nearest != null) {
                tank.removeView(fi.view);
                fit.remove();
                nearest.data.feed();
                nearest.speed = 1.2f;

                int newSize = nearest.data.getSize();
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) nearest.view.getLayoutParams();
                lp.width = newSize;
                lp.height = newSize;
                nearest.view.setLayoutParams(lp);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        running = false;
        handler.removeCallbacks(gameLoop);
    }
}
