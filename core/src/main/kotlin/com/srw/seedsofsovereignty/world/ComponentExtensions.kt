package com.srw.seedsofsovereignty.world

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * COMPONENT EXTENSIONS: The AI-Friendly API layer.
 * These extensions simplify component access within the Fleks 2.x World.
 *
 * MANDATE: Adheres to FLEKS_2X_MANDATE.md by using stable extension patterns.
 */

fun Entity.pos(world: World): PositionComponent = world.run { this@pos[PositionComponent] }
fun Entity.vel(world: World): VelocityComponent = world.run { this@vel[VelocityComponent] }
fun Entity.texture(world: World): TextureRegionComponent = world.run { this@texture[TextureRegionComponent] }
fun Entity.composite(world: World): CompositeVisualComponent = world.run { this@composite[CompositeVisualComponent] }
fun Entity.player(world: World): PlayerComponent = world.run { this@player[PlayerComponent] }
fun Entity.module(world: World): ModuleComponent = world.run { this@module[ModuleComponent] }
fun Entity.attach(world: World): AttachmentComponent = world.run { this@attach[AttachmentComponent] }
fun Entity.defense(world: World): ArcedDefenseComponent = world.run { this@defense[ArcedDefenseComponent] }
fun Entity.power(world: World): PowerCoreComponent = world.run { this@power[PowerCoreComponent] }
fun Entity.radar(world: World): RadarComponent = world.run { this@radar[RadarComponent] }
fun Entity.evo(world: World): EvolutionComponent = world.run { this@evo[EvolutionComponent] }
fun Entity.channels(world: World): ModuleChannelsComponent = world.run { this@channels[ModuleChannelsComponent] }
fun Entity.launcher(world: World): WeaponLauncherComponent = world.run { this@launcher[WeaponLauncherComponent] }
fun Entity.status(world: World): StatusEffectComponent = world.run { this@status[StatusEffectComponent] }

// Utility Helpers
fun Entity.isPlayer(world: World): Boolean = world.run { this@isPlayer.has(PlayerComponent) }
fun Entity.isModule(world: World): Boolean = world.run { this@isModule.has(ModuleComponent) }
fun Entity.isAttached(world: World): Boolean = world.run { this@isAttached.has(AttachmentComponent) }
fun Entity.hasStatus(world: World): Boolean = world.run { this@hasStatus.has(StatusEffectComponent) }
