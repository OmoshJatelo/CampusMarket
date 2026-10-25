package com.smart.campusmarket;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        Product product = IntentCompat.getSerializableExtra(
                getIntent(), "extra_product", Product.class);
        if (product == null) {
            finish();
            return;
        }

        bindDetails(product);
        bindGallery(product);
        bindActions(product);
        bindExtras(product);
    }

    // ---------------------------------------------------------------- details

    private void bindDetails(Product p) {
        ((TextView) findViewById(R.id.detailTitle)).setText(p.getTitle());

        String price = p.isWanted()
                ? getString(R.string.product_budget_format, p.getPrice())
                : (p.isNegotiable()
                   ? getString(R.string.detail_price_negotiable, p.getPrice())
                   : p.getPrice());
        ((TextView) findViewById(R.id.detailPrice)).setText(price);

        findViewById(R.id.detailWantedBadge)
                .setVisibility(p.isWanted() ? View.VISIBLE : View.GONE);

        // Older posts have no category/condition/description, so hide empty rows.
        setOptionalText(R.id.detailCategory, p.getCategory(), R.string.detail_category);
        if (!p.isWanted()) {
            setOptionalText(R.id.detailCondition, p.getCondition(), R.string.detail_condition);
        } else {
            findViewById(R.id.detailCondition).setVisibility(View.GONE);
        }
        setOptionalText(R.id.detailLocation, p.getLocation(), R.string.detail_location);

        ((TextView) findViewById(R.id.detailTime))
                .setText(getString(R.string.detail_posted, p.getTimePosted()));

        TextView description = findViewById(R.id.detailDescription);
        if (p.getDescription() != null && !p.getDescription().isEmpty()) {
            description.setText(p.getDescription());
            description.setVisibility(View.VISIBLE);
        }
    }

    private void setOptionalText(int viewId, String value, int formatRes) {
        TextView view = findViewById(viewId);
        if (value == null || value.isEmpty()) {
            view.setVisibility(View.GONE);
        } else {
            view.setText(getString(formatRes, value));
            view.setVisibility(View.VISIBLE);
        }
    }

    // ---------------------------------------------------------------- gallery

    private void bindGallery(Product p) {
        View box = findViewById(R.id.detailGalleryBox);
        List<String> images = p.getImageUris();
        if (images.isEmpty()) {
            box.setVisibility(View.GONE);
            return;
        }

        ViewPager2 pager = findViewById(R.id.detailPager);
        LinearLayout dots = findViewById(R.id.detailDots);

        pager.setAdapter(new GalleryAdapter(images, position -> openViewer(images, position)));
        Dots.setup(dots, images.size());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Dots.select(dots, position);
            }
        });
    }

    private void openViewer(List<String> images, int position) {
        Intent intent = new Intent(this, ImageViewerActivity.class);
        intent.putStringArrayListExtra(ImageViewerActivity.EXTRA_IMAGES, new ArrayList<>(images));
        intent.putExtra(ImageViewerActivity.EXTRA_INDEX, position);
        startActivity(intent);
    }

    private interface OnPhotoClick {
        void onClick(int position);
    }

    private static class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {

        private final List<String> urls;
        private final OnPhotoClick onClick;

        GalleryAdapter(List<String> urls, OnPhotoClick onClick) {
            this.urls = urls;
            this.onClick = onClick;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView view = new ImageView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            h.image.setContentDescription(h.image.getContext().getString(
                    R.string.detail_photo_desc, position + 1, urls.size()));
            Glide.with(h.image)
                    .load(urls.get(position))
                    .placeholder(R.drawable.ic_photo_placeholder)
                    .error(R.drawable.ic_photo_placeholder)
                    .into(h.image);
            h.image.setOnClickListener(v -> onClick.onClick(h.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ImageView image;

            VH(@NonNull ImageView v) {
                super(v);
                image = v;
            }
        }
    }

    // ---------------------------------------------------------------- contact

    private void bindActions(Product p) {
        Button contact = findViewById(R.id.btnContactSeller);
        contact.setText(p.isWanted()
                ? R.string.detail_contact_wanted : R.string.detail_contact_sale);
        contact.setOnClickListener(v ->
                open(new Intent(Intent.ACTION_VIEW, Uri.parse(p.getWhatsAppUrl()))));

        Button call = findViewById(R.id.btnCallSeller);
        call.setOnClickListener(v ->
                open(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p.getPhone()))));
    }

    private void open(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.detail_error_open, Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------ share, report, block

    private void bindExtras(Product p) {
        findViewById(R.id.btnShare).setOnClickListener(v -> ShareHelper.sharePost(this, p));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        View report = findViewById(R.id.btnReport);
        View block = findViewById(R.id.btnBlock);
        boolean canModerate = user != null && p.getOwnerId() != null
                && !user.getUid().equals(p.getOwnerId());
        if (!canModerate) {   // your own posts (or old sample posts) cannot be reported or blocked
            report.setVisibility(View.GONE);
            block.setVisibility(View.GONE);
            return;
        }
        String uid = user.getUid();
        report.setOnClickListener(v -> showReportDialog(p));
        block.setOnClickListener(v -> confirmBlock(uid, p));
    }

    private void showReportDialog(Product p) {
        String[] reasons = getResources().getStringArray(R.array.report_reasons);
        new AlertDialog.Builder(this)
                .setTitle(R.string.report_dialog_title)
                .setItems(reasons, (dialog, which) ->
                        new ModerationRepository().report(p.getId(), p.getOwnerId(), p.getTitle(),
                                reasons[which], new ListingRepository.Done() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(ProductDetailActivity.this,
                                                R.string.report_sent, Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ProductDetailActivity.this,
                                                R.string.report_error, Toast.LENGTH_LONG).show();
                                    }
                                }))
                .show();
    }

    private void confirmBlock(String uid, Product p) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.block_title)
                .setMessage(R.string.block_message)
                .setPositiveButton(R.string.block_confirm, (dialog, which) ->
                        new ModerationRepository().block(uid, p.getOwnerId(), p.getTitle(),
                                new ListingRepository.Done() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(ProductDetailActivity.this,
                                                R.string.block_done, Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Toast.makeText(ProductDetailActivity.this,
                                                getString(R.string.block_error, e.getMessage()),
                                                Toast.LENGTH_LONG).show();
                                    }
                                }))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}