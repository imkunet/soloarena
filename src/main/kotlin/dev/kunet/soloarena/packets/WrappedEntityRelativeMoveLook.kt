package dev.kunet.soloarena.packets

import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import dev.kunet.soloarena.util.IntLocation

class WrappedEntityRelativeMoveLook(
    private val entityId: Int,
    private val previousIntLocation: IntLocation,
    private val intLocation: IntLocation,
    private val onGround: Boolean,
) : PacketWrapper<WrappedEntityRelativeMoveLook>(PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
    override fun write() {
        writeVarInt(entityId)

        writeByte(intLocation.x - previousIntLocation.x)
        writeByte(intLocation.y - previousIntLocation.y)
        writeByte(intLocation.z - previousIntLocation.z)
        writeByte(intLocation.yaw.toInt())
        writeByte(intLocation.pitch.toInt())

        writeBoolean(onGround)
    }
}
