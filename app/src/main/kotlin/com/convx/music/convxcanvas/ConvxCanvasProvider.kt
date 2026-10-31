package com.convx.music.convxcanvas

import com.convx.music.canvas.CanvasArtwork

/**
 * Convx video canvas stub (video canvas scraping removed).
 */
object ConvxCanvasProvider {
    suspend fun getBySongArtist(
        song: String,
        artist: String,
        album: String? = null,
    ): CanvasArtwork? = null

    suspend fun getByAlbumArtist(album: String, artist: String): CanvasArtwork? = null
}
