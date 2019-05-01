package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import edu.hm.eem_library.model.SortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String}.
 */
public class ItemRecyclerViewAdapter<S extends SortableMapLiveData<String, ?>, T extends StringViewHolder<?>> extends RecyclerView.Adapter<T>{

    private final ViewHolderBuilder<T> builder;
    final S liveData;

    ItemRecyclerViewAdapter(S liveData, ViewHolderBuilder<T> builder) {
        this.liveData = liveData;
        this.builder = builder;
    }

    @NonNull
    @Override
    public T onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return builder.build(parent, this);
    }

    @Override
    public void onBindViewHolder(@NonNull final T holder, int position) {
        holder.initializeFromLiveData(position);
    }

    @Override
    public int getItemCount() {
        return liveData.getValue().length;
    }
}
