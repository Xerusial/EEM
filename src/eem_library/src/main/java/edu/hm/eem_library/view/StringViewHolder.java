package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableMapLiveData;

public class StringViewHolder<T extends ItemRecyclerViewAdapter<? extends SortableMapLiveData<String,?>,? extends StringViewHolder<?>>> extends RecyclerView.ViewHolder{
    protected final static int layout = R.layout.fragment_item;
    final View view;
    private final TextView nameView;
    final T container;

    protected StringViewHolder(View view, T container){
        super(view);
        this.view = view;
        this.nameView = view.findViewById(R.id.selectableItemName);
        this.container = container;
    }

    void initializeFromLiveData(int position){
        nameView.setText(container.liveData.getValue()[position].sortableKey);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + " '" + nameView.getText() + "'";
    }

    public class Builder implements ViewHolderBuilder<StringViewHolder<?>>{
        @Override
        public StringViewHolder<?> build(@NonNull ViewGroup parent, ItemRecyclerViewAdapter<? extends SortableMapLiveData<String, ?>, StringViewHolder<?>> container) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);
            return new StringViewHolder<>(v, (T)container);
        }
    }
}
