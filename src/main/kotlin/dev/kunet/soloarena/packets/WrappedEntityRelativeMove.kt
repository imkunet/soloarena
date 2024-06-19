package dev.kunet.soloarena.packets

import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import dev.kunet.soloarena.util.IntLocation

class WrappedEntityRelativeMove(
    private val entityId: Int,
    private val previousIntLocation: IntLocation,
    private val intLocation: IntLocation,
    private val onGround: Boolean,
) : PacketWrapper<WrappedEntityRelativeMove>(PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
    override fun write() {
        writeVarInt(entityId)

        writeByte(intLocation.x - previousIntLocation.x)
        writeByte(intLocation.y - previousIntLocation.y)
        writeByte(intLocation.z - previousIntLocation.z)

        writeBoolean(onGround)
    }
}
