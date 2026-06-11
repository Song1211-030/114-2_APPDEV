package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String[] FISH_NAMES = {
            "小丑魚", "金魚", "神仙魚", "熱帶魚",
            "鯨魚", "海馬", "龍魚", "錦鯉"
    };
    private static final int[] FISH_IMAGES = {
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_myplaces,
            android.R.drawable.ic_menu_help,
            android.R.drawable.ic_menu_my_calendar,
            android.R.drawable.ic_menu_info_details,
            android.R.drawable.ic_menu_manage
    };
    private static final String[] FISH_RARITIES = {
            "Common", "Common", "Uncommon", "Uncommon",
            "Rare", "Rare", "Epic", "Epic"
    };
    private static final int[] FISH_WEIGHTS = {50, 50, 30, 30, 15, 15, 5, 5};

    private ImageView resultImage;
    private TextView resultName, resultRarity, fishCountText;
    private Button drawButton;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultImage = findViewById(R.id.resultImage);
        resultName = findViewById(R.id.resultName);
        resultRarity = findViewById(R.id.resultRarity);
        fishCountText = findViewById(R.id.fishCountText);
        drawButton = findViewById(R.id.drawButton);

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawFish();
            }
        });

        findViewById(R.id.gotoAquariumButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AquariumActivity.class));
            }
        });

        updateFishCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateFishCount();
    }

    private void drawFish() {
        drawButton.setEnabled(false);
        drawButton.setText("抽魚中...");

        int totalWeight = 200;
        int r = random.nextInt(totalWeight);
        int cumulative = 0;
        int selected = 0;
        for (int i = 0; i < FISH_WEIGHTS.length; i++) {
            cumulative += FISH_WEIGHTS[i];
            if (r < cumulative) {
                selected = i;
                break;
            }
        }

        FishData fish = new FishData(FISH_NAMES[selected], FISH_IMAGES[selected], FISH_RARITIES[selected]);
        FishCollection.fishList.add(fish);

        final int finalSelected = selected;
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                resultImage.setImageResource(FISH_IMAGES[finalSelected]);
                resultName.setText(FISH_NAMES[finalSelected]);
                resultRarity.setText(FISH_RARITIES[finalSelected]);
                updateFishCount();
                drawButton.setEnabled(true);
                drawButton.setText("抽魚");
            }
        }, 500);
    }

    private void updateFishCount() {
        fishCountText.setText("已擁有：" + FishCollection.fishList.size() + " 條魚");
    }
}
