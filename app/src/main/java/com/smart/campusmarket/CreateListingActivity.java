package com.smart.campusmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // <-- NEW IMPORT
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateListingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_listing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createMain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText editTitle = findViewById(R.id.editProductTitle);
        EditText editPrice = findViewById(R.id.editProductPrice);
        EditText editLocation = findViewById(R.id.editProductLocation);
        EditText editPhone = findViewById(R.id.editSellerPhone);
        Button btnSubmit = findViewById(R.id.btnSubmitListing);

        btnSubmit.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String price = editPrice.getText().toString().trim();
            String location = editLocation.getText().toString().trim();

            String phoneInput = editPhone.getText().toString().trim();
            // Keep the fallback just in case they leave it blank
            String phone = phoneInput.isEmpty() ? "+254700000000" : phoneInput;

            // Basic Validation
            if (title.isEmpty() || price.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- THE CONFIRMATION DIALOG ---
            // 1. Format the data into a readable summary
            String summary = "Item: " + title + "\n"
                    + "Price: " + price + "\n"
                    + "Location: " + location + "\n"
                    + "WhatsApp: " + phone;

            // 2. Build and show the dialog
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Listing Details")
                    .setMessage(summary)
                    .setCancelable(false) // Forces them to press one of the buttons below
                    .setPositiveButton("Confirm", (dialog, which) -> {
                        // User verified the data. Proceed with saving!
                        Product newProduct = new Product(title, price, location, "Just now", phone);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("new_product", newProduct);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .setNegativeButton("Edit", (dialog, which) -> {
                        // User wants to change something. Just close the popup.
                        dialog.dismiss();
                    })
                    .show();
        });
    }
}