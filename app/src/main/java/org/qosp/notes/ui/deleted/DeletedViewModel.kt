package org.qosp.notes.ui.deleted

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.qosp.notes.components.MediaStorageManager
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.NoteDeletionTime
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.ui.common.AbstractNotesViewModel
import javax.inject.Inject

@HiltViewModel
class DeletedViewModel @Inject constructor(
    private val notesRepository: NoteRepository,
    private val mediaStorageManager: MediaStorageManager,
    preferenceRepository: PreferenceRepository,
    syncManager: SyncManager,
) : AbstractNotesViewModel(preferenceRepository, syncManager) {
    override val notesData: Flow<List<Note>> = sortMethod
        .flatMapLatest {
            notesRepository.getDeleted(it)
        }

    val noteDeletionTimeInDays = noteDeletionTime.map {
        when (it) {
            NoteDeletionTime.WEEK -> 7
            NoteDeletionTime.TWO_WEEKS -> 14
            NoteDeletionTime.MONTH -> 30
            else -> 0
        }
    }

    fun permanentlyDeleteNotesInBin() {
        viewModelScope.launch(Dispatchers.IO) {
            notesRepository.permanentlyDeleteNotesInBin()
            mediaStorageManager.cleanUpStorage()
        }
    }
}