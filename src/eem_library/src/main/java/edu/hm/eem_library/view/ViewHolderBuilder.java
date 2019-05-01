package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import edu.hm.eem_library.model.SortableMapLiveData;

public interface ViewHolderBuilder<T extends StringViewHolder<?>> {
    T build(@NonNull ViewGroup parent, ItemRecyclerViewAdapter<? extends SortableMapLiveData<String, ?>,T> container);
}
