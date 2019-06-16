package edu.hm.eem_library.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import edu.hm.eem_library.R;
import edu.hm.eem_library.model.ExamDocument;
import edu.hm.eem_library.model.SelectableSortableMapLiveData;
import edu.hm.eem_library.model.ThumbnailedExamDocument;

public class DocumentRecyclerViewAdapter extends InteractableItemRecyclerViewAdapter {
    private final int colorWhiteOpaque;
    private final int colorPrimaryOpaque;

    DocumentRecyclerViewAdapter(SelectableSortableMapLiveData<ExamDocument, ThumbnailedExamDocument> liveData, Context context, ItemListFragment.OnListFragmentPressListener listener, ItemListContent content, boolean isSelectable) {
        super(liveData, context, listener, content, isSelectable);
        this.colorWhiteOpaque = context.getColor(R.color.colorWhiteOpaque);
        this.colorPrimaryOpaque = context.getColor(R.color.colorPrimaryOpaque);
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
        final TextView numberPages;

        DocumentViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.icon);
            numberPages = view.findViewById(R.id.number_of_pages);
        }

        @Override
        void initializeFromLiveData(int position) {
            super.initializeFromLiveData(position);
            ThumbnailedExamDocument ted = (ThumbnailedExamDocument) liveData.getValue().get(position);
            if(isSelectable) {
                if (ted.hasThumbnail) {
                    if (ted.thumbnail != null) {
                        thumbnail.setImageBitmap(ted.thumbnail);
                        numberPages.setVisibility(View.INVISIBLE);
                    } else {
                        numberPages.setText(R.string.thumbnail_not_found);
                        numberPages.setVisibility(View.VISIBLE);
                        numberPages.setTextSize(18);
                    }
                } else {
                    numberPages.setText(String.valueOf(ted.item.getPages()));
                    numberPages.setVisibility(View.VISIBLE);
                    numberPages.setTextSize(36);
                }
            } else {
                switch (ted.reason){
                    case TOO_MANY_PAGES:
                        numberPages.setText(R.string.too_many_pages);
                        break;
                    case HASH_DOES_NOT_MATCH:
                        numberPages.setText(R.string.incorrect_document);
                        break;
                    case TOO_MANY_DOCS:
                        numberPages.setText(R.string.too_many_docs);
                        break;
                }
            }

        }

        @Override
        void setSelected(boolean selected) {
            if(isSelectable) {
                nameView.setBackgroundColor(selected ? colorPrimaryOpaque : colorWhiteOpaque);
            } else {
                numberPages.setVisibility(selected?View.VISIBLE:View.INVISIBLE);
                thumbnail.setImageAlpha(selected?128:255);
                nameView.setAlpha(selected?0.5f:1);
            }
        }
    }
}
