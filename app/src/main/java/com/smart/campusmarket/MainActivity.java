package com.smart.campusmarket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 1. Declare productList and adapter as class-level fields
    private List<Product> productList;
    private ProductAdapter adapter;

    // Modern way to handle returning data from another Activity
    private final ActivityResultLauncher<Intent> createListingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Product newProduct = (Product) result.getData().getSerializableExtra("new_product");
                    if (newProduct != null) {
                        // 1. Add to the top of our list
                        productList.add(0, newProduct);

                        // 2. Notify the adapter to refresh the UI
                        adapter.notifyItemInserted(0);

                        // 3. Scroll to the top so the user sees their new post
                        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
                        recyclerView.scrollToPosition(0);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply system bar padding to the root View
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Initialize the class field productList (not a local variable)
        productList = new ArrayList<>();
        productList.add(new Product("Casio Calculator (fx-991ES PLUS)", "KSh 1,500", "Smart Wisdom Bookshop", "2h ago", "+254700000004"));
        productList.add(new Product("Java Programming Textbook", "KSh 800", "Engineer Okoth CodeCamp", "5h ago", "+254700000004"));
        productList.add(new Product("HP Laptop 15-inch 8GB RAM", "KSh 25,000", "TeloByte Electronics", "1d ago", "+254700000004"));
        productList.add(new Product("Mini Fridge", "KSh 8,500", "TeloSpace collections", "2d ago", "+254700000004"));
        productList.add(new Product("ESP32 Microcontroller", "KSh 950", "JKUAT", "3h ago", "+254700000004"));
        productList.add(new Product("ZamZam Whoofer", "KSh 25,000", "Jatelo Enterprise", "3h ago", "+254700000004"));

        // 3. Assign to class field adapter
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        // Binding XML views to Java variables
        EditText searchInput = findViewById(R.id.searchInput);
        Button btnSell = findViewById(R.id.btnSell);

        btnSell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CreateListingActivity.class);
            createListingLauncher.launch(intent);
        });
    }
}