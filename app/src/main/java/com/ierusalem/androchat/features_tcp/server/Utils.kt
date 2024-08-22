package com.ierusalem.androchat.features_tcp.server

/**
 * What the fuck is this
 * https://stackoverflow.com/questions/10006459/regular-expression-for-ip-address-validation
 *
 * Tests if a given string is an IP address
 */
@JvmField
val IP_ADDRESS_REGEX =
    """^(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))$"""
        .toRegex()
