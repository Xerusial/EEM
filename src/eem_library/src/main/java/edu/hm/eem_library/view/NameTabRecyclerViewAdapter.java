package edu.hm.eem_library.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;

public class NameTabRecyclerViewAdapter extends SelectableItemRecyclerViewAdapter {
    private final int colorPrimary;
    private final int colorPrimaryLight;

    public NameTabRecyclerViewAdapter(SelectableSortableMapLiveData<?, SortableItem<?>> liveData, ItemListFragment.OnListFragmentPressListener listener, int colorPrimary, int colorPrimaryLight) {
        super(liveData, listener);
        this.colorPrimary = colorPrimary;
        this.colorPrimaryLight = colorPrimaryLight;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nametab_item, parent, false);
        return new NameTabViewHolder(v);
    }

    class NameTabViewHolder extends SelectableStringViewHolder{

        NameTabViewHolder(View view) {
            super(view);
        }

        @Override
        void setSelected(boolean selected) {
            {
                ((CardView)itemView).setCardBackgroundColor(selected ? colorPrimary : colorPrimaryLight);
            }
        }
    }
}
