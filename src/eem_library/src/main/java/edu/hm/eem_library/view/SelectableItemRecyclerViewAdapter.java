package edu.hm.eem_library.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import edu.hm.eem_library.model.SelectableSortableMapLiveData;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link ItemListFragment.OnListFragmentPressListener}.
 */
public class SelectableItemRecyclerViewAdapter<T extends SelectableStringViewHolder> extends ItemRecyclerViewAdapter<SelectableSortableMapLiveData<String, ?>, T> {

    final int colorPrimary;
    final int colorPrimaryLight;
    ItemListFragment.OnListFragmentPressListener listener;

    SelectableItemRecyclerViewAdapter(SelectableSortableMapLiveData<String, ?> livedata,
                                      ViewHolderBuilder<T> builder,
                                      ItemListFragment.OnListFragmentPressListener listener,
                                      int colorPrimary,
                                      int colorPrimaryLight) {
        super(livedata, builder);
        this.listener = listener;
        this.colorPrimary = colorPrimary;
        this.colorPrimaryLight = colorPrimaryLight;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        liveData.clearSelection();
        listener = null;
    }
}
