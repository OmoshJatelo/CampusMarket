package com.smart.campusmarket;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

/** Full-screen photo viewer: swipe between photos, pinch or double-tap to zoom. */
public class ImageViewerActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGES = "extra_images";
    public static final String EXTRA_INDEX = "extra_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<String> images = getIntent().getStringArrayListExtra(EXTRA_IMAGES);
        if (images == null || images.isEmpty()) {
            finish();
            return;
        }
        int startIndex = Math.max(0, Math.min(getIntent().getIntExtra(EXTRA_INDEX, 0), images.size() - 1));

        // Black screen, so keep the status and navigation icons light
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(Color.TRANSPARENT));
        setContentView(R.layout.activity_image_viewer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.viewerMain), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        TextView counter = findViewById(R.id.viewerCounter);
        ViewPager2 pager = findViewById(R.id.viewerPager);
        pager.setAdapter(new Adapter(images));
        pager.setCurrentItem(startIndex, false);
        counter.setText(getString(R.string.viewer_counter, startIndex + 1, images.size()));
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                counter.setText(getString(R.string.viewer_counter, position + 1, images.size()));
            }
        });

        findViewById(R.id.btnViewerClose).setOnClickListener(v -> finish());
    }

    // ---------------------------------------------------------------- adapter

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {

        private final List<String> urls;

        Adapter(List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ZoomImageView view = new ZoomImageView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            h.image.setContentDescription(h.image.getContext().getString(
                    R.string.viewer_photo_desc, position + 1, urls.size()));
            // Uploaded photos are at most 1280 px, so loading the original size is safe and sharp when zoomed
            Glide.with(h.image).load(urls.get(position)).override(Target.SIZE_ORIGINAL).into(h.image);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ZoomImageView image;

            VH(@NonNull ZoomImageView v) {
                super(v);
                image = v;
            }
        }
    }
}