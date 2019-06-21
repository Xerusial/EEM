package edu.hm.eem_library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.SelectableSortableItem;
import edu.hm.eem_library.model.SelectableSortableItemLiveData;
import edu.hm.eem_library.net.ClientItem;

public class ClientItemRecyclerviewAdapter extends NameTabRecyclerViewAdapter {


    ClientItemRecyclerviewAdapter(SelectableSortableItemLiveData<?, SelectableSortableItem<?>> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content) {
        super(liveData, context, listener, content, false);
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.studentdevice_item, parent, false);
        return new StudentDeviceViewHolder(v);
    }

    public class StudentDeviceViewHolder extends NameTabRecyclerViewAdapter.NameTabViewHolder {
        final ConstraintLayout layout;
        final ConstraintSet constraintSet = new ConstraintSet();
        StudentDeviceViewHolder(View view) {
            super(view);
            icon.setImageResource(R.drawable.ic_student);
            layout = view.findViewById(R.id.itemlayout);
        }

        private void lighthouse(){
            int position = getAdapterPosition();
            ClientItem device = (ClientItem) liveData.getValue().get(position).item;
            constraintSet.clone(layout);
            if(device.lighthoused ^= true)
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.lighthouse, ConstraintSet.RIGHT);
            else
                constraintSet.connect(R.id.card_item, ConstraintSet.LEFT, R.id.itemlayout, ConstraintSet.LEFT);
            constraintSet.applyTo(layout);
            listener.onListFragmentPress(position);
        }


        @Override
        void setSelected(boolean selected) {
            item.setCardBackgroundColor(selected ? colorPrimary : colorPrimaryLight);
            selectedCb.setChecked(selected);
        }

        @Override
        void setInteractions() {
            view.setOnClickListener(v -> lighthouse());
        }
    }
}
