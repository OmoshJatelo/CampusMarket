package com.smart.campusmarket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    // 1. Inflates the item_product.xml layout
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    // 2. Binds the data to the specific views
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.titleText.setText(product.getTitle());
        holder.priceText.setText(product.getPrice());
        holder.locationTimeText.setText(product.getLocation() + " • " + product.getTimePosted());
    }

    // 3. Tells the RecyclerView how many items exist
    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ViewHolder caches the findViewByID calls to keep scrolling smooth
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, priceText, locationTimeText;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textProductTitle);
            priceText = itemView.findViewById(R.id.textProductPrice);
            locationTimeText = itemView.findViewById(R.id.textProductLocationTime);
        }
    }
}