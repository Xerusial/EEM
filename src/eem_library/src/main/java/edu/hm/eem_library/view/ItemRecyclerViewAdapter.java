package edu.hm.eem_library.view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String}.
 */
public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.StringViewHolder>{

    final SortableMapLiveData<?, ? extends SortableItem<?>> liveData;

    ItemRecyclerViewAdapter(SortableMapLiveData<?, ? extends SortableItem<?>> liveData) {
        this.liveData = liveData;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nametab_item, parent, false);
        return new StringViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final StringViewHolder holder, int position) {
        holder.initializeFromLiveData(position);
    }

    @Override
    public int getItemCount() {
        return liveData.getValue().size();
    }

    public class StringViewHolder extends RecyclerView.ViewHolder{
        final View view;
        final TextView nameView;

        StringViewHolder(View view){
            super(view);
            this.view = view;
            this.nameView = view.findViewById(R.id.itemname);
        }

        void initializeFromLiveData(int position){
            nameView.setText(liveData.getValue().get(position).sortableKey);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
