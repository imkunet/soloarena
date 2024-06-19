package dev.kunet.soloarena.util

import kotlin.math.*

data class MutableLocation(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var yaw: Float = 0f,
    var pitch: Float = 0f
) {
    fun toPELocation() = com.github.retrooper.packetevents.protocol.world.Location(x, y, z, yaw, pitch)

    operator fun plus(other: MutableLocation): MutableLocation {
        return MutableLocation(x + other.x, y + other.y, z + other.z, yaw, pitch)
    }

    operator fun times(other: MutableLocation): MutableLocation {
        return MutableLocation(x * other.x, y * other.y, z * other.z, yaw, pitch)
    }

    fun distance(other: MutableLocation): Double {
        val x = x - other.x
        val y = y - other.y
        val z = z - other.z
        return sqrt(x * x + y * y + z * z)
    }
}

class IntLocation(
    var x: Int,
    var y: Int,
    var z: Int,
    var yaw: Byte,
    var pitch: Byte,
) {
    constructor(l: MutableLocation) : this(
        (l.x * 32).roundToInt(),
        (l.y * 32).roundToInt(),
        (l.z * 32).roundToInt(),
        (l.yaw * 256.0 / 360.0).roundToInt().toByte(),
        (l.pitch * 256.0 / 360.0).roundToInt().toByte(),
    )

    fun setTo(l: MutableLocation) {
        x = (l.x * 32).roundToInt()
        y = (l.y * 32).roundToInt()
        z = (l.z * 32).roundToInt()
        yaw = (l.yaw * 256.0 / 360.0).roundToInt().toByte()
        pitch = (l.pitch * 256.0 / 360.0).roundToInt().toByte()
    }

    fun copyFrom(l: IntLocation) {
        x = l.x; y = l.y; z = l.z; yaw = l.yaw; pitch = l.pitch
    }

    fun clone(): IntLocation = IntLocation(x, y, z, yaw, pitch)

    fun sameCoordinates(l: IntLocation) = (l.x == x && l.y == y && l.z == z)
    fun sameDirection(l: IntLocation) = (l.yaw == yaw && l.pitch == pitch)
    fun anyCoordinateDiffersAtLeast(to: IntLocation, delta: Int) =
        abs(x - to.x) >= delta || abs(y - to.y) >= delta || abs(z - to.z) >= delta
}
