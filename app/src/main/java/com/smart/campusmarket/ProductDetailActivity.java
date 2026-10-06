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

        // 4. Handle Contact Button Click
        // 4. Handle Contact Button Click
        btnContact.setOnClickListener(v -> {
            String phoneNumber = product.getSellerPhone();

            // Construct a pre-filled message
            String message = "Hi, I saw your listing for the " + product.getTitle()
                    + " on CampusMarket. Is it still available?";

            // Construct the WhatsApp API URI
            // Uri.encode() ensures spaces and special characters don't break the URL
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber
                    + "&text=" + android.net.Uri.encode(message);

            // Create an Implicit Intent
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(android.net.Uri.parse(url));

            // Try to launch the Intent, handle the error gracefully if WhatsApp is missing
            try {
                startActivity(intent2);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "WhatsApp is not installed on your device", Toast.LENGTH_LONG).show();
            }
        });
    }
}