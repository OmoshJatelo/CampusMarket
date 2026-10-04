package com.smart.campusmarket;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); //tells android to use activity_main.xml as the UI for this activity


        // Apply system bar padding to the root ScrollView (id: main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //populate the list and attach the adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerViewProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Product> products = new ArrayList<>();
        products.add(new Product("Casio Calculator (fx-991ES PLUS)", "KSh 1,500", "Smart Wisdom Bookshop", "2h ago"));
        products.add(new Product("Java Programming Textbook", "KSh 800", "Engineer Okoth CodeCamp", "5h ago"));
        products.add(new Product("HP Laptop 15-inch 8GB RAM", "KSh 25,000", "TeloByte Electronics", "1d ago"));
        products.add(new Product("Mini Fridge", "KSh 8,500", "TeloSpace collections", "2d ago"));
        products.add(new Product("ESP32 Microcontroller", "KSh 950", "JKUAT", "3h ago"));
        products.add(new Product("ZamZam Whoofer", "KSh 25,000", "Jatelo Eterprise", "3h ago"));

        ProductAdapter adapter = new ProductAdapter(products);
        recyclerView.setAdapter(adapter);

        // Binding XML views to Java variables
           //get the search Box
        EditText searchInput = findViewById(R.id.searchInput);
           //get the sell button
        Button btnSell = findViewById(R.id.btnSell);

        // Set click listener on Sell button
        btnSell.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening Sell Item Screen...", Toast.LENGTH_SHORT).show();
        });


    }
}

