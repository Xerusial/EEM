package edu.hm.eem_library.view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public abstract class SelectableItemRecyclerViewAdapter extends ItemRecyclerViewAdapter {

    private final ItemListFragment.OnListFragmentPressListener listener;

    SelectableItemRecyclerViewAdapter(SelectableSortableMapLiveData<String, ?, ? extends SortableItem<String, ?>> liveData,
                                      ItemListFragment.OnListFragmentPressListener listener) {
        super(liveData);
        this.listener = listener;
    }

    @NonNull
    @Override
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableSortableMapLiveData<String, ?, SortableItem<String, ?>>)liveData).clearSelection();
    }

    abstract class SelectableStringViewHolder extends ItemRecyclerViewAdapter.StringViewHolder {

        SelectableStringViewHolder(View view) {
            super(view);
        }

        private void updateState(int position){
            setSelected(((SelectableSortableMapLiveData<String, ?, SortableItem<String, ?>>)liveData).isSelected(position));
        }

        abstract void setSelected(boolean selected);

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            updateState(position);
            view.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onListFragmentPress(getAdapterPosition());
                }
            });
            view.setOnLongClickListener(v -> {
                ((SelectableSortableMapLiveData<String, ?, SortableItem<String, ?>>)liveData).toggleSelected(getAdapterPosition());
                return true;
            });
        }

    }

}
