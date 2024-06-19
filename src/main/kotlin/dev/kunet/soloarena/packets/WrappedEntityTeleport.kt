package dev.kunet.soloarena.packets

import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import dev.kunet.soloarena.util.IntLocation

class WrappedEntityTeleport(
    private val entityId: Int,
    private val intLocation: IntLocation,
    private val onGround: Boolean,
) : PacketWrapper<WrappedEntityTeleport>(Play.Server.ENTITY_TELEPORT) {
    override fun write() {
        writeVarInt(entityId)

        writeInt(intLocation.x)
        writeInt(intLocation.y)
        writeInt(intLocation.z)

        writeByte(intLocation.yaw.toInt())
        writeByte(intLocation.pitch.toInt())

        writeBoolean(onGround)
    }
}