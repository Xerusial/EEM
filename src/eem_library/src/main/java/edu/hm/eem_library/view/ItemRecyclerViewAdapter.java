package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String}.
 */
public class ItemRecyclerViewAdapter<T> extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder>{

    private ArrayList<Map.Entry<String, T> itemlist;

    ItemRecyclerViewAdapter(SortableMapLiveData<T> livedata) {
        this.itemlist = new ArrayList<>(livedata.getValue().entrySet());
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mNameView.setText(entryIterator.);
    }

    @Override
    public int getItemCount() {
        return livedata.getValue().length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNameView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.selectableItemName);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
