package edu.hm.eem_library.view;

import android.content.Context;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.SortableItem;
import edu.hm.eem_library.net.ClientDevice;

public class StudentDeviceRecyclerviewAdapter extends InteractableItemRecyclerViewAdapter {


    StudentDeviceRecyclerviewAdapter(SelectableSortableMapLiveData<ClientDevice, SortableItem<ClientDevice>> liveData, Context context, ItemListContent content) {
        super(liveData, context, content);
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.studentdevice_item, parent, false);
        return new StudentDeviceViewHolder(v);
    }

    public class StudentDeviceViewHolder extends SelectableStringViewHolder{
        final ConstraintLayout layout;
        final ConstraintSet constraintSet = new ConstraintSet();
        StudentDeviceViewHolder(View view) {
            super(view);
            icon.setImageResource(R.drawable.ic_student);
            layout = view.findViewById(R.id.itemlayout);
        }

        @Override
        void setSelected(boolean selected) {
            constraintSet.clone(layout);
            if(selected)
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.lighthouse, ConstraintSet.RIGHT);
            else
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.itemlayout, ConstraintSet.LEFT);
            constraintSet.applyTo(layout);
        }

        @Override
        void setInteractions() {
            view.setOnClickListener(v -> {
                listener.onListFragmentPress(getAdapterPosition());
                ((SelectableSortableMapLiveData<?, SortableItem<?>>)liveData).toggleSelected(getAdapterPosition());
            });
        }
    }
}
