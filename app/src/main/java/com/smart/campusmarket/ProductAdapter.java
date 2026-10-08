package com.smart.campusmarket;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final int PLACEHOLDER = android.R.drawable.ic_menu_gallery;

    /** Every post (the same list MainActivity created). */
    private final List<Product> allProducts;
    /** What is currently shown after applying the search text. */
    private final List<Product> visibleProducts = new ArrayList<>();
    private String query = "";

    public ProductAdapter(List<Product> productList) {
        this.allProducts = productList;
        this.visibleProducts.addAll(productList);
    }

    // -------------------------------------------------------- public actions

    /** Adds a new post at the top (use this instead of editing the list directly). */
    public void addProduct(Product product) {
        allProducts.add(0, product);
        if (matches(product, query)) {
            visibleProducts.add(0, product);
            notifyItemInserted(0);
        }
    }

    /** Filters by title, category, location or description. Empty text shows everything. */
    public void filter(String text) {
        query = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
        visibleProducts.clear();
        for (Product p : allProducts) {
            if (matches(p, query)) visibleProducts.add(p);
        }
        notifyDataSetChanged();
    }

    private boolean matches(Product p, String q) {
        if (q.isEmpty()) return true;
        return contains(p.getTitle(), q)
                || contains(p.getCategory(), q)
                || contains(p.getLocation(), q)
                || contains(p.getDescription(), q);
    }

    private boolean contains(String field, String q) {
        return field != null && field.toLowerCase(Locale.ROOT).contains(q);
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
        // \u2022 is the bullet character (your original showed "?" because of an encoding issue)
        holder.locationTimeText.setText(
                product.getLocation() + " \u2022 " + product.getTimePosted());
        holder.wantedBadge.setVisibility(product.isWanted() ? View.VISIBLE : View.GONE);

        Glide.with(holder.thumb)
                .load(product.getFirstImageUri())   // null for posts without photos
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