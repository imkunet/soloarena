package dev.kunet.soloarena.npc

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer
import dev.kunet.soloarena.packets.WrappedEntityRelativeMove
import dev.kunet.soloarena.packets.WrappedEntityRelativeMoveLook
import dev.kunet.soloarena.packets.WrappedEntityTeleport
import dev.kunet.soloarena.packets.WrappedHeadRotation
import dev.kunet.soloarena.util.send
import dev.kunet.soloarena.util.IntLocation
import dev.kunet.soloarena.util.MutableLocation
import org.bukkit.entity.Player
import java.util.*

// I don't recommend skidding this code since it was
// made for literally one thing and was meant to just
// look "good enough" without any additional functionality

var ENTITY_ID = 100_000

class NPC(
    val name: String,
    val location: MutableLocation,
    val skin: TextureProperty? = null,
    val entityId: Int = ++ENTITY_ID,
    val uuid: UUID = UUID.randomUUID(),
) {
    private val profile = if (skin != null) UserProfile(uuid, name, listOf(skin)) else UserProfile(uuid, name)

    private var lastSneaking = false
    var sneaking = false
    private var lastSprinting = false
    var sprinting = false
    var onGround = true

    val observerList = mutableListOf<Player>()

    private var intLocation = IntLocation(location)
    private var lastIntLocation = intLocation.clone()

    fun addObserver(observer: Player) {
        WrapperPlayServerPlayerInfo(
            WrapperPlayServerPlayerInfo.Action.ADD_PLAYER, WrapperPlayServerPlayerInfo.PlayerData(
                null, profile, GameMode.ADVENTURE, 0
            )
        ).send(observer)

        WrapperPlayServerSpawnPlayer(
            entityId,
            uuid,
            location.toPELocation(),
            EntityData(10, EntityDataTypes.BYTE, 127.toByte())
        ).send(observer)

        WrappedEntityTeleport(entityId, intLocation, onGround).send(observer)
        WrappedHeadRotation(entityId, intLocation.yaw).send(observer)

        WrapperPlayServerEntityAnimation(
            entityId,
            WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
        ).send(observer)

        if (sneaking || sprinting) {
            var state = 0
            if (sneaking) state = state or 0x02
            if (sprinting) state = state or 0x08

            WrapperPlayServerEntityMetadata(entityId, listOf(EntityData(0, EntityDataTypes.BYTE, state.toByte())))
                .send(observer)
        }

        observerList.add(observer)
    }

    fun removeObserver(observer: Player) {
        WrapperPlayServerDestroyEntities(entityId).send(observer)
        observerList.remove(observer)
    }

    fun animate() {
        WrapperPlayServerEntityAnimation(
            entityId,
            WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM
        ).send(observerList)
    }

    fun hurt() {
        WrapperPlayServerEntityAnimation(
            entityId,
            WrapperPlayServerEntityAnimation.EntityAnimationType.HURT
        ).send(observerList)
    }

    fun tick() {
        observerList.removeIf { !it.isOnline }

        if (lastSneaking != sneaking || lastSprinting != sprinting) {
            var state = 0
            if (sneaking) state = state or 0x02
            if (sprinting) state = state or 0x08

            WrapperPlayServerEntityMetadata(entityId, listOf(EntityData(0, EntityDataTypes.BYTE, state.toByte())))
                .send(observerList)
            lastSneaking = sneaking
            lastSprinting = sprinting
        }

        runMovement()
    }

    private fun runMovement() {
        intLocation.setTo(location)

        val positionChanged = !intLocation.sameCoordinates(lastIntLocation)
        val directionChanged = !intLocation.sameDirection(lastIntLocation)

        val shouldUpdate = intLocation.anyCoordinateDiffersAtLeast(lastIntLocation, 4 * 32)

        if (shouldUpdate) {
            WrappedEntityTeleport(entityId, intLocation, onGround).send(observerList)
            WrappedHeadRotation(entityId, intLocation.yaw).send(observerList)
            lastIntLocation.copyFrom(intLocation)
            return
        }

        if (positionChanged) {
            if (directionChanged) {
                WrappedEntityRelativeMoveLook(entityId, lastIntLocation, intLocation, onGround).send(observerList)
                WrappedHeadRotation(entityId, intLocation.yaw).send(observerList)
                lastIntLocation.copyFrom(intLocation)
                return
            }

            WrappedEntityRelativeMove(entityId, lastIntLocation, intLocation, onGround).send(observerList)
            lastIntLocation.copyFrom(intLocation)
            return
        }

        if (directionChanged) {
            WrappedEntityRelativeMoveLook(entityId, lastIntLocation, intLocation, onGround).send(observerList)
            WrappedHeadRotation(entityId, intLocation.yaw).send(observerList)
            lastIntLocation.copyFrom(intLocation)
        }
    }

    fun restoreSnapshot(snapshot: NPCSnapshot) {
        location.x = snapshot.intX / 32.0
        location.y = snapshot.intY / 32.0
        location.z = snapshot.intZ / 32.0
        location.yaw = snapshot.byteYaw.toInt() * 360.0f / 256.0f
        location.pitch = snapshot.bytePitch.toInt() * 360.0f / 256.0f

        sneaking = snapshot.sneaking
        sprinting = snapshot.sprinting
        onGround = snapshot.onGround

        // TODO: item in hand
    }
}
