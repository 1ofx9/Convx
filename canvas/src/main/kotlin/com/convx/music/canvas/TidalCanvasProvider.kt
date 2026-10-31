package com.convx.music.canvas

/**
 * Tidal video canvas stub (video canvas scraping removed).
 */
object TidalCanvasProvider {
    suspend fun getBySongArtist(song: String, artist: String): CanvasArtwork? = null
    suspend fun getByAlbumArtist(album: String, artist: String): CanvasArtwork? = null
}
