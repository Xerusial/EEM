package edu.hm.eem_library.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.model.SortableItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public abstract class InteractableItemRecyclerViewAdapter extends ItemRecyclerViewAdapter {

    final ItemListFragment.OnListFragmentPressListener listener;
    final boolean isSelectable;

    InteractableItemRecyclerViewAdapter(SelectableSortableItemLiveData<?, ? extends SortableItem<?>> liveData,
                                        ItemListFragment.OnListFragmentPressListener listener, ItemListContent content, boolean isSelectable) {
        super(liveData, content);
        this.listener = listener;
        this.isSelectable = isSelectable;
    }

    @NonNull
    @Override
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) liveData).clearSelection();
    }

    abstract class SelectableStringViewHolder extends ItemRecyclerViewAdapter.StringViewHolder {
        final CheckBox selectedCb;

        SelectableStringViewHolder(View view) {
            super(view);
            selectedCb = view.findViewById(R.id.selected);
        }

        private void updateState(int position) {
            boolean selected = Objects.requireNonNull(((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) liveData).getValue()).get(position).selected;
            setSelected(selected);
        }

        abstract void setSelected(boolean selected);

        void setInteractions() {
            view.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onListFragmentPress(getAdapterPosition());
                }
            });
            if (isSelectable) {
                view.setOnLongClickListener(v -> {
                    ((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) liveData).toggleSelected(getAdapterPosition());
                    return true;
                });
            }
        }

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            updateState(position);
            setInteractions();
        }

    }

}
