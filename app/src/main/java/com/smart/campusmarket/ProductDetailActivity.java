package com.smart.campusmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize Views
        TextView titleText = findViewById(R.id.detailTitle);
        TextView priceText = findViewById(R.id.detailPrice);
        TextView locationText = findViewById(R.id.detailLocation);
        TextView timeText = findViewById(R.id.detailTime);
        Button btnContact = findViewById(R.id.btnContactSeller);

        // 2. Receive the Product object from Intent
        Intent intent = getIntent();
        Product product = (Product) intent.getSerializableExtra("extra_product");

        // 3. Populate UI if product exists
        if (product == null) {
            finish();
            return;
        }

        titleText.setText(product.getTitle());
        priceText.setText(product.getPrice());
        locationText.setText("Location: " + product.getLocation());
        timeText.setText("Posted: " + product.getTimePosted());

        btnContact.setOnClickListener(v ->
                Toast.makeText(this, "Contacting seller for " + product.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }
}