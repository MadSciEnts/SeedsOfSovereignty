package com.srw.seedsofsovereignty.ui.factory

import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.ui.HUDFactory

/**
 * Command Matrix Factory (v3.2.0)
 * ROLE: Constructing the sovereign interface hierarchy.
 *
 * UPDATES:
 * 1. [DYNAMIC_REFRESH] - Implement refreshModuleShelf and refreshInstruments.
 * 2. [WEAPON_SATELLITES] - Populating specific AR instruments for the WEAPON category.
 * 3. [STACKING_HELPER] - Added calculateHorizontalOffset for centered instrument layout.
 * 4. [DRONE_SATELLITES] - Implementing the Drone Triad instruments (Phase 26.3).
 */
object CommandMatrixFactory {
    private const val OWNER_ID = "COMMAND_MATRIX"
    private const val MOD_OWNER_ID = "CMD_MATRIX_MOD"

    fun createRootAnchor(world: World): Entity {
        return with(world) {
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent(ownerId = OWNER_ID)
                it += HUDElementComponent(
                    space = HUDSpace.SCREEN,
                    anchor = HUDAnchor.BOTTOM_RIGHT,
                    offsetX = -160f,
                    offsetY = 200f,
                    width = 320f,
                    height = 400f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 10
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "INDUSTRIAL",
                    isVisible = false // Anchor serves as coordinate root
                )
            }
        }
    }

    private fun getRootAnchor(world: World): Entity? {
        return with(world) {
            family { all(HUDTagComponent) }.firstOrNull { it[HUDTagComponent].ownerId == OWNER_ID }
        }
    }

    /**
     * Calculates centered horizontal offset for instruments.
     * Spacing: 80f units. Centered in 320f matrix.
     */
    private fun calculateHorizontalOffset(index: Int, total: Int): Float {
        val spacing = 80f
        val startX = -((total - 1) * spacing) / 2f
        return startX + (index * spacing)
    }

    /**
     * Spawns the primary category buttons parented to the matrix root.
     */
    fun createCategoryShelf(world: World, rootAnchor: Entity) {
        val categories = listOf("WEAPON", "DRONE", "NAV", "SYSTEM")

        with(world) {
            categories.forEachIndexed { index, name ->
                entity {
                    it += HUDInstrumentTag()
                    it += HUDTagComponent(OWNER_ID)
                    it += HUDElementComponent(
                        parentId = rootAnchor,
                        offsetX = calculateHorizontalOffset(index, categories.size),
                        offsetY = -160f,
                        width = 64f,
                        height = 64f,
                        layer = HUDLayer.SYSTEM,
                        zIndex = 11
                    )
                    it += HUDRendererComponent(
                        renderType = HUDRenderType.BLUEPRINT,
                        styleId = "INDUSTRIAL",
                        localizationKey = name,
                        isVisible = true
                    )
                    it += HUDInteractionComponent(
                        isClickable = true,
                        actionKey = "CMD_MATRIX_CAT_$name"
                    )
                }
            }
        }
    }

    /**
     * Clears and spawns module buttons based on the selected category.
     */
    fun refreshModuleShelf(world: World, player: Entity) {
        val root = getRootAnchor(world) ?: return

        with(world) {
            val hub = player[TacticalHubComponent]

            // Clear existing modules
            family { all(HUDTagComponent) }.forEach { if (it[HUDTagComponent].ownerId == MOD_OWNER_ID) it.remove() }

            val slots = player[ModuleSlotComponent].slots
            val filteredSlots = slots.filter { slot: ModuleSlot ->
                when (hub.selectedCategory) {
                    "WEAPON" -> slot.type == SlotType.WEAPON || slot.type == SlotType.UNIVERSAL
                    "DRONE" -> slot.type == SlotType.DRONE || slot.type == SlotType.UNIVERSAL
                    else -> false
                }
            }

            filteredSlots.forEachIndexed { index, slot ->
                entity {
                    it += HUDInstrumentTag()
                    it += HUDTagComponent(MOD_OWNER_ID)
                    it += HUDElementComponent(
                        parentId = root,
                        offsetX = calculateHorizontalOffset(index, filteredSlots.size),
                        offsetY = -80f,
                        width = 64f,
                        height = 64f,
                        layer = HUDLayer.SYSTEM,
                        zIndex = 11
                    )
                    it += HUDRendererComponent(
                        renderType = HUDRenderType.BLUEPRINT,
                        styleId = "INDUSTRIAL",
                        localizationKey = slot.equippedName,
                        isVisible = true
                    )
                    it += HUDInteractionComponent(
                        isClickable = true,
                        actionKey = "CMD_MATRIX_MOD_$index"
                    )
                }
            }
        }
    }

    /**
     * Clears and spawns AR instruments based on the selected module.
     */
    fun refreshInstruments(world: World, player: Entity) {
        val root = getRootAnchor(world) ?: return

        with(world) {
            val hub = player[TacticalHubComponent]
            val slots = player[ModuleSlotComponent].slots

            // Filter logic must match refreshModuleShelf for index consistency
            val filteredSlots = slots.filter { slot: ModuleSlot ->
                when (hub.selectedCategory) {
                    "WEAPON" -> slot.type == SlotType.WEAPON || slot.type == SlotType.UNIVERSAL
                    "DRONE" -> slot.type == SlotType.DRONE || slot.type == SlotType.UNIVERSAL
                    else -> false
                }
            }

            // Clear existing instruments
            family { all(HUDTagComponent) }.forEach { if (it[HUDTagComponent].ownerId == "TACTICAL") it.remove() }

            // Category-Specific Satellites
            when (hub.selectedCategory) {
                "WEAPON" -> spawnWeaponCategoryInstruments(world, player, root)
                "DRONE" -> spawnDroneCategoryInstruments(world, player, root)
            }

            if (hub.selectedModuleIndex < 0 || hub.selectedModuleIndex >= filteredSlots.size) return

            val activeSlot = filteredSlots[hub.selectedModuleIndex]
            val slotEntity = activeSlot.slotEntity

            val schema = when {
                slotEntity != null && slotEntity.has(WeaponModuleComponent) -> slotEntity[WeaponModuleComponent].tacticalSchema
                slotEntity != null && slotEntity.has(DroneModuleComponent) -> slotEntity[DroneModuleComponent].tacticalSchema
                else -> null
            }

            schema?.let {
                HUDFactory.createInstrumentEntities(world, player, it, root)
            }
        }
    }

    private fun spawnWeaponCategoryInstruments(world: World, player: Entity, root: Entity) {
        with(world) {
            val instrumentCount = 2

            // Primary Fire Control
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = calculateHorizontalOffset(0, instrumentCount),
                    offsetY = 40f,
                    width = 80f,
                    height = 80f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 12
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "VANGUARD",
                    localizationKey = "PRIMARY",
                    isVisible = true
                )
                it += HUDInteractionComponent(isClickable = true, actionKey = "WPN_PRIMARY")
            }

            // Special Action Control
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = calculateHorizontalOffset(1, instrumentCount),
                    offsetY = 40f,
                    width = 80f,
                    height = 80f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 12
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "CORE_YELLOW",
                    localizationKey = "SPECIAL",
                    isVisible = true
                )
                it += HUDInteractionComponent(isClickable = true, actionKey = "WPN_SPECIAL")
            }
        }
    }

    private fun spawnDroneCategoryInstruments(world: World, player: Entity, root: Entity) {
        with(world) {
            // 1. Touchpad: Loiter Control (Center)
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = 0f,
                    offsetY = 40f,
                    width = 160f,
                    height = 160f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 12
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "VANGUARD",
                    isVisible = true
                )
                it += HUDInteractionComponent(isClickable = true, actionKey = "DRONE_TARGET_PAD")
            }

            // 2. Deploy Button (Left)
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = -120f,
                    offsetY = 40f,
                    width = 64f,
                    height = 64f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 13
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "CORE_YELLOW",
                    localizationKey = "DEPLOY",
                    isVisible = true
                )
                it += HUDInteractionComponent(isClickable = true, actionKey = "DRONE_DEPLOY")
            }

            // 3. Mode Toggle: Localization (Right)
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = 120f,
                    offsetY = 40f,
                    width = 64f,
                    height = 64f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 13
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.BLUEPRINT,
                    styleId = "INDUSTRIAL",
                    localizationKey = "MODE",
                    isVisible = true
                )
                it += HUDInteractionComponent(isClickable = true, actionKey = "DRONE_LOCALIZATION")
            }

            // 4. Readout: Telemetry (Above Deploy)
            entity {
                it += HUDInstrumentTag()
                it += HUDTagComponent("TACTICAL")
                it += HUDElementComponent(
                    parentId = root,
                    offsetX = -120f,
                    offsetY = 100f,
                    layer = HUDLayer.SYSTEM,
                    zIndex = 13
                )
                it += HUDRendererComponent(
                    renderType = HUDRenderType.TEXT,
                    styleId = "INDUSTRIAL",
                    localizationKey = "ACTIVE: ",
                    isVisible = true
                )
            }
        }
    }
}
