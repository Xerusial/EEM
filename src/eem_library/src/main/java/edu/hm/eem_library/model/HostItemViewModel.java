package edu.hm.eem_library.model;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.Objects;

import edu.hm.eem_library.net.NsdService;

/**
 * Subclass of {@link ItemViewModel}. Check out {@link ItemViewModel} for full hierarchy
 * The livedata in this one holds nsdservices (information about found bonjour services).
 * This backs the {@link androidx.recyclerview.widget.RecyclerView} of the scan activity
 */
public class HostItemViewModel extends ItemViewModel<SelectableSortableItemLiveData<NsdService, SelectableSortableItem<NsdService>>> {
    public HostItemViewModel(@NonNull Application application) {
        super(application);
        this.livedata = new SelectableSortableItemLiveData<>(true);
    }

    /**
     * Get a certain nsdservice for the list
     *
     * @param index target index
     * @return nsdservice at location index
     */
    public NsdService get(int index) {
        return Objects.requireNonNull(livedata.getValue()).get(index).item;
    }
}
