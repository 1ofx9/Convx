/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import android.content.Context
import com.convx.music.db.MusicDatabase

/**
 * Legacy scanner stub. Rebuilt Convx streams exclusively from network servers (Navidrome).
 * Local MediaStore audio scanning is fully disabled.
 */
object LocalAudioScanner {

    data class ScanResult(
        val totalFound: Int = 0,
        val newSongs: Int = 0,
        val skippedExisting: Int = 0,
    )

    suspend fun scanAndInsert(
        context: Context,
        database: MusicDatabase,
    ): ScanResult = ScanResult(0, 0, 0)
}
