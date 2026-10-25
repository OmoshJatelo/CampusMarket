package com.smart.campusmarket;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** Pulsing grey placeholder cards shown while the feed loads. */
public class SkeletonAdapter extends RecyclerView.Adapter<SkeletonAdapter.VH> {

    private final int count;

    public SkeletonAdapter(int count) {
        this.count = count;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_skeleton, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) { /* nothing to bind */ }

    @Override
    public int getItemCount() {
        return count;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VH holder) {
        ObjectAnimator pulse = ObjectAnimator.ofFloat(holder.itemView, View.ALPHA, 0.45f, 1f);
        pulse.setDuration(800);
        pulse.setRepeatCount(ObjectAnimator.INFINITE);
        pulse.setRepeatMode(ObjectAnimator.REVERSE);
        holder.pulse = pulse;
        pulse.start();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VH holder) {
        if (holder.pulse != null) holder.pulse.cancel();
        holder.itemView.setAlpha(1f);
    }

    static class VH extends RecyclerView.ViewHolder {
        ObjectAnimator pulse;

        VH(@NonNull View v) {
            super(v);
        }
    }
}