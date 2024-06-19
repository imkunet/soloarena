package dev.kunet.soloarena.arena.mech

// shadow step
// while it's not entirely known how the ray is cast:
// - it's known to travel through "transparent" blocks (slabs, stairs)
// - it has a claimed range of 20 blocks
// - we actually know the box of the player being "stepped" to
// - though we don't know which box it'll prioritize if there are two players
// box dimensions:
// - top/bottom of box: round(y) +- 2
// - floor(abs(x/z)) +- 1
// - z +/- = 1 if abs(z)-floor(abs(z)) > 0.25
// - x +/- = 1 if abs(x)-floor(abs(x)) > 0.25
// https://www.desmos.com/calculator/ph7bykom1t
// has a cooldown of 30 seconds
// grants speed 3 for 2 seconds post-shadow
// teleports the user 1 block behind the eyes on the same y level ray cast to non-transparent blocks
