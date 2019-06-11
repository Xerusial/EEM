package edu.hm.eem_library.view;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public class DocumentRecyclerViewAdapter extends SelectableItemRecyclerViewAdapter {
    DocumentRecyclerViewAdapter(SelectableSortableMapLiveData<String, ExamDocument, ThumbnailedExamDocument> liveData, ItemListFragment.OnListFragmentPressListener listener) {
        super(liveData, listener);
    }

    @NonNull
    @Override
    public StringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.document_item, parent, false);
        return new DocumentViewHolder(v);
    }

    class DocumentViewHolder extends SelectableStringViewHolder {
        final ImageView thumbnail;

        DocumentViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
        }

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            ThumbnailedExamDocument ted = (ThumbnailedExamDocument) liveData.getValue().get(position);
            thumbnail.setImageBitmap(ted.thumbnail);
        }

        @Override
        void setSelected(boolean selected) {

        }
    }
}
