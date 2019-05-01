package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public class SelectableItemRecyclerViewAdapter extends ItemRecyclerViewAdapter {

    private final int colorPrimary;
    private final int colorPrimaryLight;
    private ItemListFragment.OnListFragmentPressListener mListener;

    SelectableItemRecyclerViewAdapter(SortableMapLiveData<?> livedata, ItemListFragment.OnListFragmentPressListener mListener, int colorPrimary, int colorPrimaryLight) {
        super(livedata);
        this.mListener = mListener;
        this.colorPrimary = colorPrimary;
        this.colorPrimaryLight = colorPrimaryLight;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        boolean activated = ((SelectableItemMap<Nameable>) livedata).isSelected(item);
        if(activated){
            ((CardView)holder.itemView).setCardBackgroundColor(colorPrimary);
        } else {
            ((CardView)holder.itemView).setCardBackgroundColor(colorPrimaryLight);
        }
        holder.itemView.setActivated(activated);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentPress(holder.getAdapterPosition());
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ((SelectableItemMap<Nameable>) livedata).toggleSelected(holder.getAdapterPosition());
                notifyDataSetChanged();
                mListener.onListFragmentLongPress();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return livedata.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        ((SelectableItemMap<Nameable>) livedata).clearSelection();
        mListener = null;
    }
}
