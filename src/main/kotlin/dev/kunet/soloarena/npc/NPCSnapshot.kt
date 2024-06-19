package dev.kunet.soloarena.npc

import dev.kunet.soloarena.util.IntLocation
import dev.kunet.soloarena.util.toMutableLocation
import org.bukkit.entity.Player
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

fun Player.npcSnapshot(): NPCSnapshot {
    val l = IntLocation(location.toMutableLocation())
    val item = itemInHand

    return NPCSnapshot(
        l.x,
        l.y,
        l.z,
        l.yaw,
        l.pitch,

        isSneaking,
        isSprinting,
        // literally the only valid use of onGround like ever
        // except in like anti-cheats I guess :(
        isOnGround,

        item.type.id,
        item.data.data,
    )
}

data class NPCSnapshot(
    var intX: Int = 0,
    var intY: Int = 0,
    var intZ: Int = 0,
    var byteYaw: Byte = 0b0,
    var bytePitch: Byte = 0b0,

    var sneaking: Boolean = false,
    var sprinting: Boolean = false,
    var onGround: Boolean = true,

    var itemInHand: Int = 0,
    var itemInHandData: Byte = 0,
) {
    fun write(output: ObjectOutputStream) {
        output.writeInt(intX)
        output.writeInt(intY)
        output.writeInt(intZ)
        output.writeByte(byteYaw.toInt())
        output.writeByte(bytePitch.toInt())

        output.writeBoolean(sneaking)
        output.writeBoolean(sprinting)
        output.writeBoolean(onGround)

        output.writeInt(itemInHand)
        output.writeByte(itemInHandData.toInt())
    }

    fun read(input: ObjectInputStream) {
        intX = input.readInt()
        intY = input.readInt()
        intZ = input.readInt()
        byteYaw = input.readByte()
        bytePitch = input.readByte()

        sneaking = input.readBoolean()
        sprinting = input.readBoolean()
        onGround = input.readBoolean()

        itemInHand = input.readInt()
        itemInHandData = input.readByte()
    }
}
