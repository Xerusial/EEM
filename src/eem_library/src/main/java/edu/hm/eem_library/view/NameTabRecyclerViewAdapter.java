package edu.hm.eem_library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;

public class NameTabRecyclerViewAdapter extends InteractableItemRecyclerViewAdapter {
    final int colorPrimary;
    final int colorPrimaryLight;

    NameTabRecyclerViewAdapter(SelectableSortableItemLiveData<?, SelectableSortableItem<?>> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content, boolean isSelectable) {
        super(liveData, listener, content, isSelectable);
        this.colorPrimary = context.getColor(R.color.colorPrimary);
        this.colorPrimaryLight = context.getColor(R.color.colorPrimaryLight);
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.examtab_item, parent, false);
        return new NameTabViewHolder(v);
    }

    class NameTabViewHolder extends SelectableStringViewHolder {
        final CardView item;

        NameTabViewHolder(View view) {
            super(view);
            item = view.findViewById(R.id.card_item);
            switch (content) {
                case EXAM:
                    icon.setImageResource(R.drawable.ic_exam);
                    break;
                case HOST:
                    icon.setImageResource(R.drawable.ic_teacher);
                    break;
                case DEVICE:
                    icon.setImageResource(R.drawable.ic_student);
            }
        }

        @Override
        void setSelected(boolean selected) {
            item.setCardBackgroundColor(selected ? colorPrimary : colorPrimaryLight);
            selectedCb.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
