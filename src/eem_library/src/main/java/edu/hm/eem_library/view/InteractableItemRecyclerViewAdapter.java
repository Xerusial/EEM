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

    /**
     * Constructor
     *
     * @param liveData     liveData liveData holding the data for this adapter
     * @param listener     listener for on item click callbacks
     * @param content      content type for Recyclerview to differentiate adapters
     * @param isSelectable content Indicator for the recyclerview fragment holding this adapter
     */
    InteractableItemRecyclerViewAdapter(SelectableSortableItemLiveData<?, ? extends SortableItem<?>> liveData,
                                        ItemListFragment.OnListFragmentPressListener listener, ItemListContent content, boolean isSelectable) {
        super(liveData, content);
        this.listener = listener;
        this.isSelectable = isSelectable;
    }

    /**
     * Create a new viewholder
     *
     * @param parent   Android basics
     * @param viewType Android basics
     * @return a new viewholder
     */
    @NonNull
    @Override
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * Clean up
     *
     * @param recyclerView host recyclerview
     */
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) liveData).clearSelection();
    }

    /**
     * The viewholder for this adapter
     */
    abstract class SelectableStringViewHolder extends ItemRecyclerViewAdapter.StringViewHolder {
        final CheckBox selectedCb;

        /**
         * Constructor
         *
         * @param view inflated view of this viewholder
         */
        SelectableStringViewHolder(View view) {
            super(view);
            selectedCb = view.findViewById(R.id.selected);
        }

        /**
         * Updates selectedness
         *
         * @param position view is at this position in list
         */
        private void updateState(int position) {
            boolean selected = Objects.requireNonNull(((SelectableSortableItemLiveData<?, SelectableSortableItem<?>>) liveData).getValue()).get(position).selected;
            setSelected(selected);
        }

        /**
         * Template for callback to be executed if a tab is selected
         *
         * @param selected yes or no?
         */
        abstract void setSelected(boolean selected);

        /**
         * Call callbacks it an action on the item has occurred
         */
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

        /**
         * Inititalize this viewholder from given livedata
         *
         * @param position view is at this position in list
         */
        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            updateState(position);
            setInteractions();
        }

    }

}
