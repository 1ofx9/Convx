/**
 * Convx Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.convx.music.utils

import android.content.Context

/**
 * Legacy folder index stub. Rebuilt Convx streams exclusively from network servers (Navidrome).
 * Local MediaStore folder indexing is fully disabled.
 */
object LocalFolderIndex {

    data class Folder(
        val path: String,
        val name: String,
        val songIds: List<String> = emptyList(),
    )

    val pathColumn: String get() = ""

    fun folderPathOf(raw: String): String = ""

    suspend fun load(context: Context): List<Folder> = emptyList()

    suspend fun songIdsFor(context: Context, path: String): List<String> = emptyList()
}
