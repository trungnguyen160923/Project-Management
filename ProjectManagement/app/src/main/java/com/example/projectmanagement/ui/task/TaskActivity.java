package com.example.projectmanagement.ui.task;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectmanagement.R;

public class TaskActivity extends AppCompatActivity {

    private CheckBox cbDone;
    private TextView tvCardTitle, tvPhaseName;
    private EditText etDescription, etComment;
    private Button btnAttachFile, btnChecklist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        cbDone = findViewById(R.id.cbDone);
        tvCardTitle = findViewById(R.id.tvCardTitle);
        tvPhaseName = findViewById(R.id.tvPhaseName);
        etDescription = findViewById(R.id.etDescription);
        etComment = findViewById(R.id.etComment);
        btnAttachFile = findViewById(R.id.btnAttachFile);
        btnChecklist = findViewById(R.id.btnChecklist);

        // Giả lập dữ liệu
        tvCardTitle.setText("The");
        tvPhaseName.setText("Hehe - Test");

        btnAttachFile.setOnClickListener(v -> {
            Toast.makeText(this, "Chọn file", Toast.LENGTH_SHORT).show();
        });

        btnChecklist.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm checklist", Toast.LENGTH_SHORT).show();
        });
    }
}