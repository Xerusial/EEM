package edu.hm.eem_host.view;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.eem_host.R;
import edu.hm.eem_host.model.Nameable;
import edu.hm.eem_host.model.SelectableItemHost;
import edu.hm.eem_host.model.SelectableItemList;
import edu.hm.eem_host.model.SelectableItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SelectableItem} and makes a call to the
 * specified {@link SelectableItemListFragment.OnListFragmentPressListener}.
 */
public class SelectableItemRecyclerViewAdapter extends RecyclerView.Adapter<SelectableItemRecyclerViewAdapter.ViewHolder>{

    private final SelectableItemListFragment.OnListFragmentPressListener mListener;
    private SelectableItemHost model;
    private final int colorPrimary;
    private final int colorPrimaryLight;

    SelectableItemRecyclerViewAdapter(SelectableItemListFragment.OnListFragmentPressListener mListener, SelectableItemHost model, int primary, int primaryLight) {
        this.mListener = mListener;
        this.model = model;
        this.colorPrimary = primary;
        this.colorPrimaryLight = primaryLight;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_selectableitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        SelectableItem item = model.getLivedata().getValue().get(position);
        if(item.selected){
            ((CardView)holder.itemView).setCardBackgroundColor(colorPrimary);
        } else {
            ((CardView)holder.itemView).setCardBackgroundColor(colorPrimaryLight);
        }
        holder.itemView.setSelected(item.selected);
        holder.mNameView.setText(item.dataItem.getName());
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
                SelectableItem item = model.getLivedata().getValue().get(holder.getAdapterPosition());
                //Toggle selected
                item.selected ^= true;
                notifyDataSetChanged();
                mListener.onListFragmentLongPress();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.getLivedata().getValue().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final TextView mNameView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.selectableItemName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
