package com.metrolist.android.playback

import com.metrolist.shared.model.SongItem
import com.metrolist.shared.playback.MusicQueue
import com.metrolist.shared.playback.QUEUE_SOURCE_ADDED_MANUALLY
import com.metrolist.shared.playback.QUEUE_SOURCE_USER_ENQUEUED
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
            } else if (!playNext && !song.queueSourceTitle.isRecommendationGeneratedQueueSource()) {
                song.copy(queueSourceTitle = QUEUE_SOURCE_USER_ENQUEUED)
            } else {
                song
            }
        }
    if (playNext) {
        queue.playNext(prepared, preventDuplicates)
    } else {
        if (preventDuplicates) queue.removeSongs(prepared.mapTo(mutableSetOf()) { it.id }, keepCurrent = true)
        val insertIndex = if (queue.currentIndex.value < 0) {
            queue.items.value.size
        } else {
            var insertPos = queue.currentIndex.value + 1
            while (insertPos < queue.items.value.size &&
                queue.items.value[insertPos].queueSourceTitle == QUEUE_SOURCE_USER_ENQUEUED) {
                insertPos++
            }
            insertPos
        }
        queue.addToQueue(prepared, preventDuplicates, insertIndex)
    }
}