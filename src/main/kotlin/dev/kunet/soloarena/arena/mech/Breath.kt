package dev.kunet.soloarena.arena.mech

import dev.kunet.soloarena.arena.Brawler
import dev.kunet.soloarena.packets.WrappedParticle
import dev.kunet.soloarena.util.DEG_TO_RAD
import dev.kunet.soloarena.util.MutableLocation
import dev.kunet.soloarena.util.send
import org.bukkit.World
import kotlin.math.cos
import kotlin.math.sin

// freezing breath
// slowing (movement speed to 0.0375) for 2s
// hit means a particle is <2.5 blocks away
// hit cone is measured upside down and upright
// deals 200 dmg, requiring 90 energy
// the breathed player(s) experience a "spike"
// the spike is basically if their y velocity < 0 it multiplies it by 2
// the cone will in each upright position spawn:
//  - 3x 0f speed snow shovel particles and
//  - 1x water drip particle
// the cone is as follows

private const val pointCount = 80
private const val depthSeparation = 0.065
private const val amplitudeScale = 2.0 / (pointCount - 1)

private val vectors = Array(pointCount) { i ->
    val amplitude = i * amplitudeScale
    val x = cos(DEG_TO_RAD * 15.0 * i) * amplitude
    val y = sin(DEG_TO_RAD * 15.0 * i) * amplitude
    val z = (i + 1) * depthSeparation
    MutableLocation(x, y, z)
}

fun animateCone(world: World, footLocation: MutableLocation, isSneaking: Boolean) {
    var eye = footLocation + MutableLocation(y = 1.62)
    if (isSneaking) eye += MutableLocation(y = -0.08)

    val si = sin(DEG_TO_RAD * eye.yaw)
    val co = cos(DEG_TO_RAD * eye.yaw)

    for (p in vectors) {
        val location = eye + MutableLocation(co * p.x - si * p.z, p.y, si * p.x + co * p.z)
        WrappedParticle(18, location).send(world.players)
        repeat(3) { WrappedParticle(32, location, particleData = 0f).send(world.players) }
    }
}

fun getConeHitPlayers(filteredBrawlers: List<Brawler>, footLocation: MutableLocation, isSneaking: Boolean): Set<Brawler> {
    var eye = footLocation + MutableLocation(y = 1.62)
    if (isSneaking) eye += MutableLocation(y = -0.08)

    val si = sin(DEG_TO_RAD * eye.yaw)
    val co = cos(DEG_TO_RAD * eye.yaw)

    val hitSet = mutableSetOf<Brawler>()

    for (p in vectors) {
        var location = eye + MutableLocation(co * p.x - si * p.z, p.y, si * p.x + co * p.z)
        for (brawler in filteredBrawlers) if (brawler.location.distance(location) < 2.5) hitSet.add(brawler)
        location = eye + MutableLocation(co * p.x - si * p.z, -p.y, si * p.x + co * p.z)
        for (brawler in filteredBrawlers) if (brawler.location.distance(location) < 2.5) hitSet.add(brawler)
    }

    return hitSet
}
