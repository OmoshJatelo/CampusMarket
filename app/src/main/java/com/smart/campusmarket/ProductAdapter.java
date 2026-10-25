package com.smart.campusmarket;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Shows posts as cards. Applies the search text, the filters and the sort order, and animates
 * posts in and out when the list changes.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final int PLACEHOLDER = R.drawable.ic_photo_placeholder;

    /** Every post (before search and filters). */
    private final List<Product> allProducts = new ArrayList<>();
    /** What is currently shown. */
    private List<Product> visibleProducts = new ArrayList<>();
    private String query = "";
    private FeedFilter filter = new FeedFilter();

    public ProductAdapter(List<Product> productList) {
        allProducts.addAll(productList);
        rebuild();
    }

    // -------------------------------------------------------- public actions

    /** Replaces everything with a fresh list (from Firestore) and re-applies search and filters. */
    public void setProducts(List<Product> products) {
        List<Product> copy = new ArrayList<>(products);
        allProducts.clear();
        allProducts.addAll(copy);
        rebuild();
    }

    /** Adds a new post at the top. */
    public void addProduct(Product product) {
        allProducts.add(0, product);
        rebuild();
    }

    /** Filters by title, category, location or description. Empty text shows everything. */
    public void filter(String text) {
        query = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        rebuild();
    }

    /** Applies the filter sheet's choices (price range, condition, type, sort order). */
    public void setFilter(FeedFilter newFilter) {
        filter = newFilter.copy();
        rebuild();
    }

    // ------------------------------------------------------------ list logic

    private void rebuild() {
        List<Product> next = new ArrayList<>();
        for (Product p : allProducts) {
            if (matchesQuery(p, query) && filter.matches(p)) next.add(p);
        }
        sort(next);

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new Diff(visibleProducts, next));
        visibleProducts = next;
        diff.dispatchUpdatesTo(this);   // animates inserts, removals and moves
    }

    private void sort(List<Product> list) {
        switch (filter.sort) {
            case PRICE_LOW:
                Collections.sort(list, (a, b) -> Long.compare(a.getPriceValue(), b.getPriceValue()));
                break;
            case PRICE_HIGH:
                Collections.sort(list, (a, b) -> Long.compare(b.getPriceValue(), a.getPriceValue()));
                break;
            default:
                Collections.sort(list, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
        }
    }

    private boolean matchesQuery(Product p, String q) {
        if (q.isEmpty()) return true;
        return contains(p.getTitle(), q)
                || contains(p.getCategory(), q)
                || contains(p.getLocation(), q)
                || contains(p.getDescription(), q);
    }

    private boolean contains(String field, String q) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(q);
    }

    private static class Diff extends DiffUtil.Callback {
        private final List<Product> oldList;
        private final List<Product> newList;

        Diff(List<Product> oldList, List<Product> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }

        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int o, int n) {
            return Objects.equals(oldList.get(o).getId(), newList.get(n).getId());
        }

        @Override
        public boolean areContentsTheSame(int o, int n) {
            Product a = oldList.get(o);
            Product b = newList.get(n);
            return Objects.equals(a.getTitle(), b.getTitle())
                    && a.getPriceValue() == b.getPriceValue()
                    && Objects.equals(a.getStatus(), b.getStatus())
                    && Objects.equals(a.getBundleId(), b.getBundleId())
                    && Objects.equals(a.getFirstImageUri(), b.getFirstImageUri())
                    && Objects.equals(a.getLocation(), b.getLocation())
                    && Objects.equals(a.getTimeAgo(), b.getTimeAgo());
        }
    }

    // ---------------------------------------------------------- RecyclerView

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = visibleProducts.get(position);
        Context context = holder.itemView.getContext();

        holder.titleText.setText(product.getTitle());
        holder.priceText.setText(product.isWanted()
                ? context.getString(R.string.product_budget_format, product.getPrice())
                : product.getPrice());
        holder.locationTimeText.setText(
                product.getLocation() + " \u2022 " + product.getTimePosted());

        if (product.isWanted()) {
            holder.wantedBadge.setText(R.string.product_wanted_badge);
            holder.wantedBadge.setVisibility(View.VISIBLE);
        } else if (product.getBundleId() != null) {
            holder.wantedBadge.setText(R.string.product_bundle_badge);
            holder.wantedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.wantedBadge.setVisibility(View.GONE);
        }

        Glide.with(holder.thumb)
                .load(CloudinaryUploader.thumbnail(product.getFirstImageUri()))   // null = no photo
                .placeholder(PLACEHOLDER)
                .fallback(PLACEHOLDER)
                .error(PLACEHOLDER)
                .centerCrop()
                .into(holder.thumb);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("extra_product", product);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return visibleProducts.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumb;
        final TextView titleText, priceText, locationTimeText, wantedBadge;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.thumbProduct);
            titleText = itemView.findViewById(R.id.textProductTitle);
            priceText = itemView.findViewById(R.id.textProductPrice);
            locationTimeText = itemView.findViewById(R.id.textProductLocationTime);
            wantedBadge = itemView.findViewById(R.id.textWantedBadge);
        }
    }
}