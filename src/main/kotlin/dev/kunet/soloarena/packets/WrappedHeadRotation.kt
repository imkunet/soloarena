package dev.kunet.soloarena.packets

import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.PacketWrapper

class WrappedHeadRotation(
    private val entityId: Int,
    private val yaw: Byte,
) : PacketWrapper<WrappedHeadRotation>(Play.Server.ENTITY_HEAD_LOOK) {
    override fun write() {
        writeVarInt(entityId)
        writeByte(yaw.toInt())
    }
}
