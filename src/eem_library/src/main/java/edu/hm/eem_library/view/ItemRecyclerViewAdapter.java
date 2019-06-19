package edu.hm.eem_library.view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String}.
 */
public abstract class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.StringViewHolder>{

    final SortableMapLiveData<?, ? extends SortableItem<?>> liveData;
    final ItemListContent content;

    ItemRecyclerViewAdapter(SortableMapLiveData<?, ? extends SortableItem<?>> liveData, ItemListContent content) {
        this.liveData = liveData;
        this.content = content;
    }

    @NonNull
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(@NonNull final StringViewHolder holder, int position) {
        holder.initializeFromLiveData(position);
    }

    @Override
    public int getItemCount() {
        return liveData.getValue().size();
    }

    public class StringViewHolder extends RecyclerView.ViewHolder{
        final View view;
        final TextView nameView;
        final ImageView icon;

        StringViewHolder(View view){
            super(view);
            this.view = view;
            this.nameView = view.findViewById(R.id.itemname);
            this.icon = view.findViewById(R.id.icon);
        }

        void initializeFromLiveData(int position){
            nameView.setText(liveData.getValue().get(position).getSortableKey());
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
