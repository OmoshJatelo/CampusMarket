package com.smart.campusmarket;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Product> productList;
    private ProductAdapter adapter;
    private RecyclerView recyclerView;
    private EditText searchInput;

    // Receives the new post from CreateListingActivity
    private final ActivityResultLauncher<Intent> createListingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Product newProduct = IntentCompat.getSerializableExtra(
                            result.getData(), "new_product", Product.class);
                    if (newProduct != null) {
                        // Clear any search so the user sees their new post
                        searchInput.setText("");
                        adapter.addProduct(newProduct);
                        recyclerView.scrollToPosition(0);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        productList.add(new Product("Casio Calculator (fx-991ES PLUS)", "KSh 1,500", "Electronics", "Smart Wisdom Bookshop", "2h ago", "+254700000004"));
        productList.add(new Product("Java Programming Textbook", "KSh 800", "Books", "Engineer Okoth CodeCamp", "5h ago", "+254700000004"));
        productList.add(new Product("HP Laptop 15-inch 8GB RAM", "KSh 25,000", "Electronics", "TeloByte Electronics", "1d ago", "+254700000004"));
        productList.add(new Product("Mini Fridge", "KSh 8,500", "Utensils", "TeloSpace collections", "2d ago", "+254700000004"));
        productList.add(new Product("ESP32 Microcontroller", "KSh 950", "Electronics", "JKUAT", "3h ago", "+254700000004"));
        productList.add(new Product("ZamZam Whoofer", "KSh 25,000", "Electronics", "Jatelo Enterprise", "3h ago", "+254700000004"));

        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });

        // Category buttons: tap to filter, tap again to clear
        setupCategoryButton(R.id.btnBooks, "Books");
        setupCategoryButton(R.id.btnElectronics, "Electronics");
        setupCategoryButton(R.id.btnUtensils, "Utensils");
        setupCategoryButton(R.id.btnClothes, "Clothes");

        Button btnSell = findViewById(R.id.btnSell);
        btnSell.setOnClickListener(v -> createListingLauncher.launch(
                new Intent(MainActivity.this, CreateListingActivity.class)));
    }

    private void setupCategoryButton(int buttonId, String category) {
        findViewById(buttonId).setOnClickListener(v -> {
            boolean alreadySelected = searchInput.getText().toString()
                    .trim().equalsIgnoreCase(category);
            searchInput.setText(alreadySelected ? "" : category);
            searchInput.setSelection(searchInput.getText().length());
        });
    }
}