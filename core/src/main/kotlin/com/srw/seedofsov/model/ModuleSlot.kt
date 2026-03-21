package com.srw.seedofsov.model

/**
 * Intermediary between the Command Matrix and a Module.
 * Command Matrix writes to channels, and Modules read from them.
 */
class ModuleSlot(val category: ModuleCategory) {
    private val channels = mutableMapOf<String, DataChannel>()
    var equippedModule: ShipModule? = null

    fun getChannel(name: String): DataChannel {
        return channels.getOrPut(name) { DataChannel(name) }
    }

    fun clearChannels() {
        channels.values.forEach { it.reset() }
    }
}
