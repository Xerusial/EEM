package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public class SelectableItemRecyclerViewAdapter extends ItemRecyclerViewAdapter {

    private final int colorPrimary;
    private final int colorPrimaryLight;
    private final ItemListFragment.OnListFragmentPressListener listener;

    SelectableItemRecyclerViewAdapter(SelectableSortableMapLiveData<String, ?> liveData,
                                      ItemListFragment.OnListFragmentPressListener listener,
                                      int colorPrimary,
                                      int colorPrimaryLight) {
        super(liveData);
        this.listener = listener;
        this.colorPrimary = colorPrimary;
        this.colorPrimaryLight = colorPrimaryLight;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new SelectableStringViewHolder(v);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableSortableMapLiveData<String, ?>)liveData).clearSelection();
    }

    public class SelectableStringViewHolder extends ItemRecyclerViewAdapter.StringViewHolder {

        SelectableStringViewHolder(View view) {
            super(view);
        }

        private void updateState(int position){
            boolean activated = ((SelectableSortableMapLiveData<String, ?>)liveData).isSelected(position);
            if(activated){
                ((CardView)itemView).setCardBackgroundColor(colorPrimary);
            } else {
                ((CardView)itemView).setCardBackgroundColor(colorPrimaryLight);
            }
            itemView.setActivated(activated);
        }

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
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
                    ((SelectableSortableMapLiveData<String, ?>)liveData).toggleSelected(position);
                    updateState(position);
                    listener.onListFragmentLongPress();
                    return true;
                }
            });
        }

    }

}
