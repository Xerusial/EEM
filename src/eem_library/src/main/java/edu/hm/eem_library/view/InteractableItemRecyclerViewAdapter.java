package edu.hm.eem_library.view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public abstract class InteractableItemRecyclerViewAdapter extends ItemRecyclerViewAdapter {

    final ItemListFragment.OnListFragmentPressListener listener;

    InteractableItemRecyclerViewAdapter(SelectableSortableMapLiveData<?, ? extends SortableItem<?>> liveData,
                                        Context context, ItemListContent content) {
        super(liveData, content);
        this.listener = (ItemListFragment.OnListFragmentPressListener) context;
    }

    @NonNull
    @Override
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableSortableMapLiveData<?, SortableItem<?>>)liveData).clearSelection();
    }

    abstract class SelectableStringViewHolder extends ItemRecyclerViewAdapter.StringViewHolder {

        SelectableStringViewHolder(View view) {
            super(view);
        }

        private void updateState(int position){
            setSelected(((SelectableSortableMapLiveData<?, SortableItem<?>>)liveData).isSelected(position));
        }

        abstract void setSelected(boolean selected);

        void setInteractions(){
            view.setOnClickListener(v -> {
                if (null != listener) {
                    listener.onListFragmentPress(getAdapterPosition());
                }
            });
            view.setOnLongClickListener(v -> {
                ((SelectableSortableMapLiveData<?, SortableItem<?>>)liveData).toggleSelected(getAdapterPosition());
                return true;
            });
        }

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            updateState(position);
            setInteractions();
        }

    }

}
