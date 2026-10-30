package com.convx.storage.domain

import java.net.URI
import java.util.UUID

enum class NetworkProtocol { WEBDAV, SMB, NAVIDROME }

data class SourceCredentials(val username: String = "", val password: String = "", val domain: String = "") {
    override fun toString(): String = "SourceCredentials(username=$username, domain=$domain)"
}

sealed interface NetworkServer {
    val id: String
    val name: String
    val credentials: SourceCredentials
    val address: String
    val protocol: NetworkProtocol

    data class WebDav(
        override val id: String,
        override val name: String,
        override val address: String,
        override val credentials: SourceCredentials = SourceCredentials(),
    ) : NetworkServer {
        override val protocol = NetworkProtocol.WEBDAV
    }

    data class Smb(
        override val id: String,
        override val name: String,
        val host: String,
        val port: Int,
        val share: String,
        val folder: String,
        override val credentials: SourceCredentials = SourceCredentials(),
    ) : NetworkServer {
        override val protocol = NetworkProtocol.SMB
        override val address: String get() = URI("smb", null, host, port.takeUnless { it == 445 } ?: -1, "/$share/${folder}".trimEnd('/'), null, null).toASCIIString()
    }

    data class Navidrome(
        override val id: String,
        override val name: String,
        override val address: String,
        override val credentials: SourceCredentials = SourceCredentials(),
    ) : NetworkServer {
        override val protocol = NetworkProtocol.NAVIDROME
    }

    companion object {
        fun create(protocol: NetworkProtocol, name: String, address: String, credentials: SourceCredentials, id: String = UUID.randomUUID().toString()): NetworkServer {
            require(name.trim().isNotBlank()) { "Server name is required" }
            return when (protocol) {
                NetworkProtocol.WEBDAV -> {
                    val input = address.trim().let { if (it.contains("://")) it else "https://$it" }
                    val uri = URI(input)
                    require(uri.scheme.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank()) { "Enter an HTTP or HTTPS server address" }
                    WebDav(id, name.trim(), input.trimEnd('/') + "/", credentials)
                }
                NetworkProtocol.SMB -> {
                    val uri = URI(if (address.startsWith("smb://")) address else "smb://$address")
                    require(!uri.host.isNullOrBlank()) { "Server host name is required" }
                    val path = uri.path.trim('/')
                    val share = path.substringBefore('/')
                    require(share.isNotBlank()) { "SMB share name is required" }
                    val port = uri.port.takeUnless { it == -1 } ?: 445
                    require(port in 1..65535) { "Invalid server port" }
                    Smb(id, name.trim(), uri.host, port, share, path.substringAfter('/', ""), credentials)
                }
                NetworkProtocol.NAVIDROME -> {
                    val input = address.trim().let { if (it.contains("://")) it else "https://$it" }
                    val uri = URI(input)
                    require(uri.scheme.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank()) { "Enter an HTTP or HTTPS server address" }
                    require(credentials.username.isNotBlank()) { "Enter a username" }
                    Navidrome(id, name.trim(), input.trimEnd('/') + "/", credentials)
                }
            }
        }
    }
}
