package com.music.innertube

import com.music.innertube.models.response.PlayerResponse

object NewPipeExtractor {
    fun init() {}

    fun getSignatureTimestamp(videoId: String): Result<Int> {
        return Result.failure(UnsupportedOperationException("NewPipe extractor decoupled"))
    }

    fun getStreamUrl(
        format: PlayerResponse.StreamingData.Format,
        videoId: String
    ): String? {
        return format.url
    }

    fun newPipePlayer(videoId: String): List<Pair<Int, String>> {
        return emptyList()
    }
}
