package com.smart.campusmarket;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Groups several of the user's own "for sale" posts into one move-out sale with a pickup date.
 * Post the items first (normal Sell screen), then bundle them here.
 */
public class CreateBundleActivity extends AppCompatActivity {

    private static final String PREFS = "campus_market_prefs";
    private static final String KEY_LAST_PHONE = "last_phone";
    private static final int MIN_ITEMS = 2;
    private static final long DAY_MS = 24L * 60L * 60L * 1000L;

    private final ListingRepository listings = new ListingRepository();
    private final BundleRepository bundles = new BundleRepository();
    private final List<Product> available = new ArrayList<>();   // my active, un-bundled sale posts

    private boolean[] checked = new boolean[0];
    private long pickupDate = 0;
    private String uid;

    private TextInputLayout layoutTitle, layoutLocation, layoutPhone;
    private TextInputEditText editTitle, editDescription, editPhone;
    private MaterialAutoCompleteTextView dropLocation;
    private TextView textPickupDate, textSelectedItems;
    private MaterialButton btnCreate;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        uid = user.getUid();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_bundle);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createBundleMain), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        layoutTitle = findViewById(R.id.layoutBundleTitle);
        layoutLocation = findViewById(R.id.layoutBundleLocation);
        layoutPhone = findViewById(R.id.layoutBundlePhone);
        editTitle = findViewById(R.id.editBundleTitle);
        editDescription = findViewById(R.id.editBundleDescription);
        editPhone = findViewById(R.id.editBundlePhone);
        dropLocation = findViewById(R.id.dropBundleLocation);
        textPickupDate = findViewById(R.id.textPickupDate);
        textSelectedItems = findViewById(R.id.textSelectedItems);
        btnCreate = findViewById(R.id.btnCreateBundle);
        progress = findViewById(R.id.progressBundle);

        dropLocation.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.listing_locations)));

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editPhone.setText(prefs.getString(KEY_LAST_PHONE, ""));

        findViewById(R.id.btnPickDate).setOnClickListener(v -> pickDate());
        findViewById(R.id.btnChooseItems).setOnClickListener(v -> chooseItems());
        btnCreate.setOnClickListener(v -> submit());

        loadAvailableItems();
    }

    // ------------------------------------------------------------------- data

    private void loadAvailableItems() {
        listings.fetchMine(uid, new ListingRepository.Callback<List<Product>>() {
            @Override
            public void onResult(List<Product> mine) {
                available.clear();
                for (Product p : mine) {
                    if (p.isActive() && !p.isWanted() && p.getBundleId() == null) available.add(p);
                }
                checked = new boolean[available.size()];
                updateSelectedText();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CreateBundleActivity.this,
                        getString(R.string.my_error, e.getMessage()), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ----------------------------------------------------------------- pickers

    private void pickDate() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setStart(today)
                .setValidator(DateValidatorPointForward.now())
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.bundle_pick_date)
                .setSelection(today)
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            pickupDate = selection;
            textPickupDate.setText(getString(R.string.bundle_date_label,
                    MoveOutBundle.formatDate(pickupDate)));
            textPickupDate.setVisibility(View.VISIBLE);
        });
        picker.show(getSupportFragmentManager(), "pickupDate");
    }

    private void chooseItems() {
        if (available.isEmpty()) {
            Toast.makeText(this, R.string.bundle_items_none, Toast.LENGTH_LONG).show();
            return;
        }
        String[] labels = new String[available.size()];
        for (int i = 0; i < labels.length; i++) {
            Product p = available.get(i);
            labels[i] = p.getTitle() + " (" + p.getPrice() + ")";
        }
        boolean[] working = checked.clone();
        new AlertDialog.Builder(this)
                .setTitle(R.string.bundle_items_dialog_title)
                .setMultiChoiceItems(labels, working, (dialog, which, isChecked) ->
                        working[which] = isChecked)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    checked = working;
                    updateSelectedText();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int selectedCount() {
        int n = 0;
        for (boolean c : checked) if (c) n++;
        return n;
    }

    private void updateSelectedText() {
        int n = selectedCount();
        textSelectedItems.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        textSelectedItems.setText(getString(R.string.bundle_items_selected, n));
    }

    // ------------------------------------------------------------------ submit

    private void submit() {
        layoutTitle.setError(null);
        layoutLocation.setError(null);
        layoutPhone.setError(null);

        String title = text(editTitle);
        String location = text(dropLocation);
        String phone = PhoneUtil.normalize(text(editPhone));
        boolean ok = true;

        if (title.length() < 3) {
            layoutTitle.setError(getString(R.string.bundle_error_title));
            ok = false;
        }
        if (location.isEmpty()) {
            layoutLocation.setError(getString(R.string.listing_error_location));
            ok = false;
        }
        if (phone == null) {
            layoutPhone.setError(getString(R.string.listing_error_phone));
            ok = false;
        }
        if (pickupDate <= 0 || pickupDate + DAY_MS < System.currentTimeMillis()) {
            Toast.makeText(this, R.string.bundle_error_date, Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (selectedCount() < MIN_ITEMS) {
            Toast.makeText(this, R.string.bundle_error_items, Toast.LENGTH_SHORT).show();
            ok = false;
        }
        if (!ok) return;

        List<String> ids = new ArrayList<>();
        for (int i = 0; i < checked.length; i++) {
            if (checked[i]) ids.add(available.get(i).getId());
        }

        MoveOutBundle bundle = new MoveOutBundle(UUID.randomUUID().toString(), uid, title,
                text(editDescription), location, phone, pickupDate,
                System.currentTimeMillis(), ids.size());

        setBusy(true);
        bundles.create(bundle, ids, new ListingRepository.Done() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateBundleActivity.this, R.string.bundle_created,
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setBusy(false);
                Toast.makeText(CreateBundleActivity.this,
                        getString(R.string.bundle_error_save, e.getMessage()),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnCreate.setEnabled(!busy);
    }

    private String text(TextView view) {
        CharSequence value = view.getText();
        return value == null ? "" : value.toString().trim();
    }
}