package com.example.englisheatleak;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.ImageView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private CharacterController characterController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView characterView = findViewById(R.id.characterView);
        characterController = new CharacterController(characterView, findViewById(R.id.shadowView));

        Button btnCorrect = findViewById(R.id.btnCorrect);
        Button btnWrong = findViewById(R.id.btnWrong);

        // ปิดปุ่มระหว่างกินและหลังตัวละครตาย
        characterController.setInputStateListener(enabled -> {
            btnCorrect.setEnabled(enabled);
            btnWrong.setEnabled(enabled);
        });

// ทดสอบตอบถูก
        btnCorrect.setOnClickListener(v -> {
            characterController.onCorrectAnswer();
        });

// ทดสอบตอบผิด
        btnWrong.setOnClickListener(v -> {
            characterController.onWrongAnswer();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        characterController.release();
    }
}