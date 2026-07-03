package com.instagramclone.core.network

import java.net.Inet4Address
import java.net.InetAddress
import okhttp3.Dns

/** VPN hiện tại có tuyến IPv6 tới Cloudinary bị treo, nên ưu tiên IPv4. */
object Ipv4FirstDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> =
        Dns.SYSTEM.lookup(hostname).sortedBy { it !is Inet4Address }
}
