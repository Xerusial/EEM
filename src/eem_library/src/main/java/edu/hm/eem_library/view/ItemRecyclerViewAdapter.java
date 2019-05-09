package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String}.
 */
public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.StringViewHolder>{

    final SortableMapLiveData<String, ?> liveData;

    ItemRecyclerViewAdapter(SortableMapLiveData<String, ?> liveData) {
        this.liveData = liveData;
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new StringViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final StringViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return liveData.getValue().length;
    }

    public class StringViewHolder extends RecyclerView.ViewHolder{
        final View view;
        private final TextView nameView;

        StringViewHolder(View view){
            super(view);
            this.view = view;
            this.nameView = view.findViewById(R.id.selectableItemName);
        }

        void initializeFromLiveData(int position){
            nameView.setText(liveData.getValue()[position].sortableKey);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
