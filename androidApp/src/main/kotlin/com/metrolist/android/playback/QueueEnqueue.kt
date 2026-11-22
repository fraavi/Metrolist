package com.metrolist.android.playback

import com.metrolist.shared.model.SongItem
import com.metrolist.shared.playback.MusicQueue
import com.metrolist.shared.playback.QUEUE_SOURCE_ADDED_MANUALLY
import com.metrolist.shared.playback.isRecommendationGeneratedQueueSource

internal fun enqueueSongs(
    queue: MusicQueue,
    songs: List<SongItem>,
    playNext: Boolean,
    preventDuplicates: Boolean,
) {
    val additions = if (preventDuplicates && !playNext) songs.asReversed().distinctBy { it.id }.asReversed() else songs
    val prepared =
        additions.map { song ->
            if (playNext && song.queueSourceTitle.isRecommendationGeneratedQueueSource()) {
                song.copy(queueSourceTitle = QUEUE_SOURCE_ADDED_MANUALLY)
            } else if (!playNext) {
                // Forza l'etichetta MANUALE su tutto ciò che aggiungi in coda
                song.copy(queueSourceTitle = QUEUE_SOURCE_ADDED_MANUALLY)
            } else {
                song
            }
        }
    if (playNext) {
        queue.playNext(prepared, preventDuplicates)
    } else {
        if (preventDuplicates) queue.removeSongs(prepared.mapTo(mutableSetOf()) { it.id }, keepCurrent = true)
        
        // Cerca il primo brano dopo quello attuale che NON è stato aggiunto manualmente
        // (quindi si ferma subito prima del prossimo brano dell'artista/album/playlist)
        val insertIndex =
            queue.items.value.indices.firstOrNull {
                it > queue.currentIndex.value &&
                    queue.items.value[it].queueSourceTitle != QUEUE_SOURCE_ADDED_MANUALLY
            }
            
        queue.addToQueue(prepared, preventDuplicates, insertIndex)
    }
}
