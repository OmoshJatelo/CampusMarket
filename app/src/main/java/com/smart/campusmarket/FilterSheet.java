package com.smart.campusmarket;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

/** The "Filters and sorting" bottom sheet. */
public final class FilterSheet {

    public interface OnApply {
        void apply(FeedFilter filter);
    }

    private FilterSheet() { }

    public static void show(Activity activity, FeedFilter current, OnApply onApply) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.sheet_filters, null, false);
        dialog.setContentView(view);

        ChipGroup sortGroup = view.findViewById(R.id.groupSort);
        ChipGroup typeGroup = view.findViewById(R.id.groupType);
        ChipGroup conditionGroup = view.findViewById(R.id.groupCondition);
        TextInputEditText editMin = view.findViewById(R.id.editMinPrice);
        TextInputEditText editMax = view.findViewById(R.id.editMaxPrice);

        // One chip per condition, taken from the same list the posting form uses
        for (String condition : activity.getResources().getStringArray(R.array.listing_conditions)) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_filter, conditionGroup, false);
            chip.setText(condition);
            chip.setChecked(current.conditions.contains(condition));
            conditionGroup.addView(chip);
        }

        // Show what is currently selected
        sortGroup.check(current.sort == FeedFilter.Sort.PRICE_LOW ? R.id.chipSortLow
                : current.sort == FeedFilter.Sort.PRICE_HIGH ? R.id.chipSortHigh
                  : R.id.chipSortNewest);
        typeGroup.check(current.type == FeedFilter.Type.SALE ? R.id.chipTypeSale
                : current.type == FeedFilter.Type.WANTED ? R.id.chipTypeWanted
                  : R.id.chipTypeAll);
        if (current.minPrice > 0) editMin.setText(String.valueOf(current.minPrice));
        if (current.maxPrice > 0) editMax.setText(String.valueOf(current.maxPrice));

        view.findViewById(R.id.btnFilterReset).setOnClickListener(v -> {
            onApply.apply(new FeedFilter());
            dialog.dismiss();
        });

        view.findViewById(R.id.btnFilterApply).setOnClickListener(v -> {
            FeedFilter f = new FeedFilter();

            int sortId = sortGroup.getCheckedChipId();
            f.sort = sortId == R.id.chipSortLow ? FeedFilter.Sort.PRICE_LOW
                    : sortId == R.id.chipSortHigh ? FeedFilter.Sort.PRICE_HIGH
                      : FeedFilter.Sort.NEWEST;

            int typeId = typeGroup.getCheckedChipId();
            f.type = typeId == R.id.chipTypeSale ? FeedFilter.Type.SALE
                    : typeId == R.id.chipTypeWanted ? FeedFilter.Type.WANTED
                      : FeedFilter.Type.ALL;

            f.minPrice = Product.parsePrice(text(editMin));
            f.maxPrice = Product.parsePrice(text(editMax));
            if (f.minPrice > 0 && f.maxPrice > 0 && f.minPrice > f.maxPrice) {   // swapped by mistake
                long tmp = f.minPrice;
                f.minPrice = f.maxPrice;
                f.maxPrice = tmp;
            }

            for (int i = 0; i < conditionGroup.getChildCount(); i++) {
                View child = conditionGroup.getChildAt(i);
                if (child instanceof Chip && ((Chip) child).isChecked()) {
                    f.conditions.add(((Chip) child).getText().toString());
                }
            }

            onApply.apply(f);
            dialog.dismiss();
        });

        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.show();
    }

    private static String text(TextView view) {
        CharSequence value = view.getText();
        return value == null ? "" : value.toString().trim();
    }
}