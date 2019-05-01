package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.hm.eem_library.model.SortableMapLiveData;

public class SelectableStringViewHolder extends StringViewHolder<SelectableItemRecyclerViewAdapter<SelectableStringViewHolder>> {

    protected SelectableStringViewHolder(View view, SelectableItemRecyclerViewAdapter<SelectableStringViewHolder> container) {
        super(view, container);
    }

    private void updateState(int position){
        boolean activated = container.liveData.isSelected(position);
        if(activated){
            ((CardView)itemView).setCardBackgroundColor(container.colorPrimary);
        } else {
            ((CardView)itemView).setCardBackgroundColor(container.colorPrimaryLight);
        }
        itemView.setActivated(activated);
    }

    @Override
    void initializeFromLiveData(int position) {
        super.initializeFromLiveData(position);
        final ItemListFragment.OnListFragmentPressListener listener = container.listener;
        updateState(position);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onListFragmentPress(getAdapterPosition());
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = getAdapterPosition();
                container.liveData.toggleSelected(position);
                updateState(position);
                listener.onListFragmentLongPress();
                return true;
            }
        });
    }

    public class Builder implements ViewHolderBuilder<SelectableStringViewHolder>{
        @Override
        public SelectableStringViewHolder build(@NonNull ViewGroup parent, ItemRecyclerViewAdapter<? extends SortableMapLiveData<String, ?>, SelectableStringViewHolder> container) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);
            return new SelectableStringViewHolder(v, (SelectableItemRecyclerViewAdapter<SelectableStringViewHolder>)container);
        }
    }
}
