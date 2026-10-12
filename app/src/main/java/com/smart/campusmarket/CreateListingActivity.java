package com.smart.campusmarket;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.os.BundleCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class CreateListingActivity extends AppCompatActivity {

    private static final int MAX_PHOTOS = 5;

    /** Set to false while testing if you don't want to pick a photo every time. */
    private static final boolean REQUIRE_PHOTO_FOR_SALE = true;

    private static final String PREFS = "campus_market_prefs";
    private static final String KEY_LAST_PHONE = "last_phone";
    private static final String STATE_PHOTOS = "state_photos";
    private static final String STATE_WANTED = "state_wanted";

    private final ArrayList<Uri> selectedPhotos = new ArrayList<>();
    private boolean isWanted = false;
    private View firstInvalidView;

    private TextView txtScreenTitle, txtPhotoCount;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnAddPhotos, btnSubmit;
    private LinearLayout photoContainer;
    private TextInputLayout layoutTitle, layoutCategory, layoutCondition,
            layoutPrice, layoutLocation, layoutPhone;
    private TextInputEditText editTitle, editPrice, editDescription, editPhone;
    private MaterialAutoCompleteTextView dropCategory, dropCondition, dropLocation;
    private CheckBox chkNegotiable;

    // Android's photo picker: no storage permission needed.
    private final ActivityResultLauncher<PickVisualMediaRequest> photoPicker =
            registerForActivityResult(
                    new ActivityResultContracts.PickMultipleVisualMedia(MAX_PHOTOS), uris -> {
                        for (Uri uri : uris) {
                            if (selectedPhotos.size() >= MAX_PHOTOS) break;
                            if (!selectedPhotos.contains(uri)) selectedPhotos.add(uri);
                        }
                        renderPhotos();
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_listing);

        // Include the keyboard (ime) so the focused field is never hidden behind it.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createMain), (v, insets) -> {
            Insets bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        bindViews();
        setupDropdowns();
        setupErrorClearing();

        // Restore state after rotation
        if (savedInstanceState != null) {
            isWanted = savedInstanceState.getBoolean(STATE_WANTED, false);
            ArrayList<Uri> saved = BundleCompat.getParcelableArrayList(
                    savedInstanceState, STATE_PHOTOS, Uri.class);
            if (saved != null) selectedPhotos.addAll(saved);
        }

        toggleType.check(isWanted ? R.id.btnTypeWanted : R.id.btnTypeSale);
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) applyListingType(checkedId == R.id.btnTypeWanted);
        });
        applyListingType(isWanted);

        btnAddPhotos.setOnClickListener(v -> photoPicker.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
        renderPhotos();

        // Pre-fill the number used last time
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (text(editPhone).isEmpty()) {
            editPhone.setText(prefs.getString(KEY_LAST_PHONE, ""));
        }

        btnSubmit.setOnClickListener(v -> onSubmitClicked());
        setupBackHandling();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_PHOTOS, selectedPhotos);
        outState.putBoolean(STATE_WANTED, isWanted);
    }

    // ------------------------------------------------------------------ setup

    private void bindViews() {
        txtScreenTitle = findViewById(R.id.txtScreenTitle);
        txtPhotoCount = findViewById(R.id.txtPhotoCount);
        toggleType = findViewById(R.id.toggleType);
        btnAddPhotos = findViewById(R.id.btnAddPhotos);
        btnSubmit = findViewById(R.id.btnSubmitListing);
        photoContainer = findViewById(R.id.photoContainer);

        layoutTitle = findViewById(R.id.layoutTitle);
        layoutCategory = findViewById(R.id.layoutCategory);
        layoutCondition = findViewById(R.id.layoutCondition);
        layoutPrice = findViewById(R.id.layoutPrice);
        layoutLocation = findViewById(R.id.layoutLocation);
        layoutPhone = findViewById(R.id.layoutPhone);

        editTitle = findViewById(R.id.editProductTitle);
        editPrice = findViewById(R.id.editProductPrice);
        editDescription = findViewById(R.id.editDescription);
        editPhone = findViewById(R.id.editSellerPhone);

        dropCategory = findViewById(R.id.dropCategory);
        dropCondition = findViewById(R.id.dropCondition);
        dropLocation = findViewById(R.id.dropLocation);

        chkNegotiable = findViewById(R.id.chkNegotiable);
    }

    private void setupDropdowns() {
        dropCategory.setAdapter(adapterFor(R.array.listing_categories));
        dropCondition.setAdapter(adapterFor(R.array.listing_conditions));
        dropLocation.setAdapter(adapterFor(R.array.listing_locations));
    }

    private ArrayAdapter<String> adapterFor(int arrayRes) {
        return new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(arrayRes));
    }

    private void setupErrorClearing() {
        clearErrorOnChange(layoutTitle, editTitle);
        clearErrorOnChange(layoutCategory, dropCategory);
        clearErrorOnChange(layoutCondition, dropCondition);
        clearErrorOnChange(layoutPrice, editPrice);
        clearErrorOnChange(layoutLocation, dropLocation);
        clearErrorOnChange(layoutPhone, editPhone);
    }

    private void clearErrorOnChange(TextInputLayout layout, TextView field) {
        field.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { layout.setError(null); }
        });
    }

    /** Switches labels and visible fields between "For sale" and "Wanted". */
    private void applyListingType(boolean wanted) {
        isWanted = wanted;
        txtScreenTitle.setText(wanted
                ? R.string.listing_screen_title_wanted : R.string.listing_screen_title_sale);
        layoutTitle.setHint(getString(wanted
                ? R.string.listing_hint_title_wanted : R.string.listing_hint_title_sale));
        layoutPrice.setHint(getString(wanted
                ? R.string.listing_hint_price_wanted : R.string.listing_hint_price_sale));
        btnSubmit.setText(wanted
                ? R.string.listing_submit_wanted : R.string.listing_submit_sale);

        int saleOnly = wanted ? View.GONE : View.VISIBLE;
        layoutCondition.setVisibility(saleOnly);
        chkNegotiable.setVisibility(saleOnly);
    }

    private void setupBackHandling() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!hasUnsavedInput()) {
                    finish();
                    return;
                }
                new AlertDialog.Builder(CreateListingActivity.this)
                        .setTitle(R.string.listing_discard_title)
                        .setMessage(R.string.listing_discard_message)
                        .setPositiveButton(R.string.listing_discard_confirm, (d, w) -> finish())
                        .setNegativeButton(R.string.listing_discard_keep, null)
                        .show();
            }
        });
    }

    private boolean hasUnsavedInput() {
        return !text(editTitle).isEmpty()
                || !text(editPrice).isEmpty()
                || !text(editDescription).isEmpty()
                || !selectedPhotos.isEmpty();
    }

    // ----------------------------------------------------------------- photos

    private void renderPhotos() {
        photoContainer.removeAllViews();
        int size = dp(88);

        for (Uri uri : new ArrayList<>(selectedPhotos)) {
            ImageView thumb = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMarginEnd(dp(8));
            thumb.setLayoutParams(lp);
            thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumb.setContentDescription(getString(R.string.listing_photo_remove_hint));
            Glide.with(this).load(uri).into(thumb);   // Glide downsamples big camera photos safely
            thumb.setOnClickListener(v -> {
                selectedPhotos.remove(uri);
                renderPhotos();
            });
            photoContainer.addView(thumb);
        }

        txtPhotoCount.setText(getString(R.string.listing_photo_count,
                selectedPhotos.size(), MAX_PHOTOS));
        btnAddPhotos.setEnabled(selectedPhotos.size() < MAX_PHOTOS);
    }

    // ------------------------------------------------------------- validation

    private boolean validateForm() {
        firstInvalidView = null;
        boolean ok = true;

        if (text(editTitle).length() < 3) {
            ok = invalid(layoutTitle, editTitle, R.string.listing_error_title);
        }
        if (text(dropCategory).isEmpty()) {
            ok = invalid(layoutCategory, dropCategory, R.string.listing_error_category);
        }
        if (!isWanted && text(dropCondition).isEmpty()) {
            ok = invalid(layoutCondition, dropCondition, R.string.listing_error_condition);
        }
        if (Product.parsePrice(text(editPrice)) <= 0) {
            ok = invalid(layoutPrice, editPrice, R.string.listing_error_price);
        }
        if (text(dropLocation).isEmpty()) {
            ok = invalid(layoutLocation, dropLocation, R.string.listing_error_location);
        }
        if (normalizePhone(text(editPhone)) == null) {
            ok = invalid(layoutPhone, editPhone, R.string.listing_error_phone);
        }
        if (!isWanted && REQUIRE_PHOTO_FOR_SALE && selectedPhotos.isEmpty()) {
            Toast.makeText(this, R.string.listing_error_photo, Toast.LENGTH_SHORT).show();
            if (firstInvalidView == null) firstInvalidView = btnAddPhotos;
            ok = false;
        }

        if (!ok && firstInvalidView != null) {
            firstInvalidView.requestFocus();
            firstInvalidView.requestRectangleOnScreen(
                    new Rect(0, 0, firstInvalidView.getWidth(), firstInvalidView.getHeight()));
        }
        return ok;
    }

    /** Shows an error on the field and remembers the first bad one so we can scroll to it. */
    private boolean invalid(TextInputLayout layout, View focusTarget, int messageRes) {
        layout.setError(getString(messageRes));
        if (firstInvalidView == null) firstInvalidView = focusTarget;
        return false;
    }

    /**
     * Accepts 0712345678, 712345678, 254712345678 and +254712345678 and returns +254712345678.
     * Returns null if it isn't a valid Kenyan mobile number.
     */
    private String normalizePhone(String raw) {
        String d = raw.replaceAll("[^0-9+]", "");
        if (d.startsWith("0")) d = "+254" + d.substring(1);
        else if (d.startsWith("254")) d = "+" + d;
        else if (d.startsWith("7") || d.startsWith("1")) d = "+254" + d;
        return d.matches("^\\+254[17]\\d{8}$") ? d : null;
    }

    // ----------------------------------------------------------------- submit

    private void onSubmitClicked() {
        if (!validateForm()) return;

        List<String> photoStrings = new ArrayList<>();
        for (Uri uri : selectedPhotos) photoStrings.add(uri.toString());

        Product product = new Product(
                isWanted ? Product.TYPE_WANTED : Product.TYPE_SALE,
                text(editTitle),
                Product.parsePrice(text(editPrice)),
                !isWanted && chkNegotiable.isChecked(),
                text(dropCategory),
                isWanted ? "Any" : text(dropCondition),
                text(dropLocation),
                text(editDescription),
                normalizePhone(text(editPhone)),
                photoStrings);

        showConfirmDialog(product);
    }

    private void showConfirmDialog(Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.listing_label_item)).append(": ").append(p.getTitle()).append('\n');
        sb.append(getString(p.isWanted()
                        ? R.string.listing_label_budget : R.string.listing_label_price))
                .append(": ").append(p.getPrice());
        if (p.isNegotiable()) {
            sb.append(" (").append(getString(R.string.listing_negotiable_short)).append(')');
        }
        sb.append('\n');
        sb.append(getString(R.string.listing_label_category)).append(": ").append(p.getCategory()).append('\n');
        if (!p.isWanted()) {
            sb.append(getString(R.string.listing_label_condition)).append(": ").append(p.getCondition()).append('\n');
        }
        sb.append(getString(R.string.listing_label_location)).append(": ").append(p.getLocation()).append('\n');
        sb.append(getString(R.string.listing_label_whatsapp)).append(": ").append(p.getPhone());
        if (!p.getImageUris().isEmpty()) {
            sb.append('\n').append(getString(R.string.listing_label_photos))
                    .append(": ").append(p.getImageUris().size());
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.listing_confirm_title)
                .setMessage(sb.toString())
                .setCancelable(false)
                .setPositiveButton(R.string.listing_confirm_post, (dialog, which) -> startUpload(p))
                .setNegativeButton(R.string.listing_confirm_edit, (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ------------------------------------------------- upload photos, then save

    private void startUpload(Product draft) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        btnSubmit.setEnabled(false);   // prevents double posting
        draft.setOwnerId(user.getUid());

        if (selectedPhotos.isEmpty()) {
            saveListing(draft);
            return;
        }

        btnSubmit.setText(getString(R.string.listing_uploading, 0, selectedPhotos.size()));
        CloudinaryUploader.uploadAll(this, new ArrayList<>(selectedPhotos),
                new CloudinaryUploader.Callback() {
                    @Override
                    public void onProgress(int done, int total) {
                        btnSubmit.setText(getString(R.string.listing_uploading, done, total));
                    }

                    @Override
                    public void onSuccess(List<String> urls) {
                        saveListing(draft.copyWithImages(urls));
                    }

                    @Override
                    public void onError(String message) {
                        failSubmit(getString(R.string.listing_error_upload, message));
                    }
                });
    }

    private void saveListing(Product product) {
        btnSubmit.setText(R.string.listing_saving);
        new ListingRepository().addListing(product, new ListingRepository.Done() {
            @Override
            public void onSuccess() {
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit().putString(KEY_LAST_PHONE, product.getPhone()).apply();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Exception e) {
                failSubmit(getString(R.string.listing_error_save, e.getMessage()));
            }
        });
    }

    private void failSubmit(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        btnSubmit.setEnabled(true);
        applyListingType(isWanted);   // restores the button label
    }

    // ---------------------------------------------------------------- helpers

    private String text(TextView view) {
        CharSequence value = view.getText();
        return value == null ? "" : value.toString().trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}