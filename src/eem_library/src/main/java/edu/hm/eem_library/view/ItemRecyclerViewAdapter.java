package edu.hm.eem_library.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.model.SortableItemLiveData;

/**
 * {@link RecyclerView.Adapter} for viewholders displaying a {@link String}
 *<p>
 *       .-------------------------.
 *       | ItemRecyclerViewAdapter |
 *       |-------------------------|
 *       | StringViewHolder        |
 *       '-------------------------'
 *                    ^
 *                    |
 * .-------------------------------------.   .---------------------------------.
 * | InteractableItemRecyclerViewAdapter |   | ExamDocumentRecyclerViewAdapter |
 * |-------------------------------------|<--|---------------------------------|
 * | SelectableStringViewHolder          |   | DocumentViewHolder              |
 * '-------------------------------------'   '---------------------------------'
 *                    ^
 *                    |
 *   .--------------------------------.
 *   |   NameTabRecyclerViewAdapter   |
 *   |--------------------------------|
 *   | NameTabViewHolder              |
 *   '--------------------------------'
 *                    ^
 *                    |
 * .------------------------------------.
 * |   ClientItemRecyclerViewAdapter    |
 * |------------------------------------|
 * | StudentDeviceViewHolder            |
 * '------------------------------------'
 *<p>
 * This basic adapter is extended by its child adapters to add more UI elements to the viewholder tabs.
 * The basic layout stays the same.
 */
public abstract class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.StringViewHolder> {

    final SortableItemLiveData<?, ? extends SortableItem<?>> liveData;
    final ItemListContent content;

    /**
     * Constructor
     *
     * @param liveData to initialize the viewholders
     * @param content  content type for Recyclerview to differentiate adapters
     */
    ItemRecyclerViewAdapter(SortableItemLiveData<?, ? extends SortableItem<?>> liveData, ItemListContent content) {
        this.liveData = liveData;
        this.content = content;
    }

    /**
     * Create viewholders to fill the screen
     *
     * @param parent   Android basics
     * @param viewType Android basics
     * @return the viewholder
     */
    @NonNull
    public abstract StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * Recycle viewholder from bottom to top or vice versa depending on scroll direction
     *
     * @param holder   target holder
     * @param position new position
     */
    @Override
    public final void onBindViewHolder(@NonNull final StringViewHolder holder, int position) {
        holder.initializeFromLiveData(position);
    }

    /**
     * Number of items in Recyclerview
     *
     * @return number
     */
    @Override
    public int getItemCount() {
        return Objects.requireNonNull(liveData.getValue()).size();
    }

    /**
     * The viewholder for this adapter
     */
    public class StringViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView nameView;
        final ImageView icon;

        /**
         * Constructor
         *
         * @param view inflated view of this viewholder
         */
        StringViewHolder(View view) {
            super(view);
            this.view = view;
            this.nameView = view.findViewById(R.id.itemname);
            this.icon = view.findViewById(R.id.icon);
        }

        /**
         * Initialize this viewholder from livedata
         *
         * @param position view is at this position in list
         */
        void initializeFromLiveData(int position) {
            nameView.setText(Objects.requireNonNull(liveData.getValue()).get(position).getSortableKey());
        }

        /**
         * Java basics
         *
         * @return String representing the object
         */
        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
