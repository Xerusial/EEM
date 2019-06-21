package edu.hm.eem_library.model;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;

/** Base class for all {@link androidx.lifecycle.ViewModel} in this App. These are needed to share
 * Data between {@link android.app.Activity} and {@link androidx.fragment.app.Fragment}, especially
 * from my various activities to my {@link edu.hm.eem_library.view.ItemListFragment}. Each ViewModel
 * holds an instance of a {@link androidx.lifecycle.LiveData}, which is responsible for the content
 * of the {@link androidx.recyclerview.widget.RecyclerView} in the
 * {@link edu.hm.eem_library.view.ItemListFragment}. The inheritance chain of the viewmodels with
 * their respective livedatas is shown in the following.
 *
 *                                 .---------------.
 *                                 | ItemViewModel |
 *                                 |---------------|<-----------------------.
 *                                 | <T>           |                        |
 *                                 '---------------' .--------------------------------------------.
 *                                         ^         |            ClientItemViewModel             |
 *                                         |         |--------------------------------------------|
 *                                         |         | SelectableSortableItemLiveData<ClientItem> |
 *                                         |         '--------------------------------------------'
 *                            .-------------------------.
 *                            | FileBackedItemViewModel |
 *                            |-------------------------|<-------------------.
 *                            | <T>                     |                    |
 *                            '-------------------------'          .-------------------.
 *                                         ^                       | ExamItemViewModel |
 *                                         |                       |-------------------|
 *                                         |                       | ExamItemLiveData  |
 *                                         |                       '-------------------'
 *                           .---------------------------.
 *                           | ExamDocumentItemViewModel |
 *                     .---->|---------------------------|<-----.
 *                     |     | ExamDocumentItemLiveData  |      |
 *                     |     '---------------------------'      |
 *                     |                                        |
 *                     |                                        |
 * .---------------------------------------..---------------------------------------.
 * |   TeacherExamDocumentItemViewModel    ||   StudentExamDocumentItemViewModel    |
 * |---------------------------------------||---------------------------------------|
 * | ExamDocumentItemLiveData<TeacherExam> || ExamDocumentItemLiveData<StudentExam> |
 * '---------------------------------------''---------------------------------------'
 *
 * @param <T> The type of livedata used in this viewmodel
 */
public abstract class ItemViewModel<T extends MutableLiveData> extends AndroidViewModel {
    protected T livedata;

    ItemViewModel(@NonNull Application application) {
        super(application);
    }

    public T getLivedata(){
        return livedata;
    }
}
