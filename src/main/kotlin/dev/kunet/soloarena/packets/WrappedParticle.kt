package dev.kunet.soloarena.packets

import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.particle.data.LegacyParticleData
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import dev.kunet.soloarena.util.MutableLocation

class WrappedParticle(
    val id: Int,
    val position: MutableLocation,
    val offset: MutableLocation? = null,
    val particleCount: Int = 0,
    val particleData: Float = 1f,
) : PacketWrapper<WrappedParticle>(PacketType.Play.Server.PARTICLE) {
    override fun write() {
        writeInt(id)
        writeBoolean(false)
        writeFloat(position.x.toFloat())
        writeFloat(position.y.toFloat())
        writeFloat(position.z.toFloat())
        writeFloat(offset?.x?.toFloat() ?: 0f)
        writeFloat(offset?.y?.toFloat() ?: 0f)
        writeFloat(offset?.z?.toFloat() ?: 0f)
        writeFloat(particleData)
        writeInt(particleCount)
        LegacyParticleData.write(this, id, LegacyParticleData.zero())
    }
}
