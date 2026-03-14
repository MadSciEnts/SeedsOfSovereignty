package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.github.quillraven.fleks.*
import com.srw.seedsofsovereignty.component.*
import com.srw.seedsofsovereignty.manager.SectorManager
import com.srw.seedsofsovereignty.ui.factory.CommandMatrixFactory

/**
 * High-fidelity HUD Factory (v2.7.0).
 * ROLE: Primary Spawner for Diegetic AR HUD Entities.
 *
 * UPDATES:
 * 1. [CMD_MATRIX] - Initializing Command Matrix hierarchy in createPermanentHUDEntities.
 * 2. [PARENTING] - Added defaultParent support to createInstrumentEntities (Phase 25.4).
 * 3. [INITIAL_STATE] - Added refresh calls for starting loadout (Phase 25.4).
 */
object HUDFactory {
    fun createSkin(styleId: String = "INDUSTRIAL"): Skin {
        val skin = Skin()
        val whitePixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply { setColor(Color.WHITE); fill() }
        val whiteTex = Texture(whitePixmap); whitePixmap.dispose()
        skin.add("white", whiteTex)

        AssetFactory.addProceduralAssets(skin)

        val mainFont = BitmapFont().apply { data.setScale(1.5f) }
        val titleFont = BitmapFont().apply { data.setScale(2.2f) }
        val mediumFont = BitmapFont().apply { data.setScale(1.4f) }
        val smallFont = BitmapFont().apply { data.setScale(1.1f) }

        skin.add("default", mainFont); skin.add("title-font", titleFont)
        skin.add("medium-font", mediumFont); skin.add("small-font", smallFont)

        skin.add("default", Label.LabelStyle(mainFont, Color.WHITE))
        skin.add("title", Label.LabelStyle(titleFont, Color.CYAN))
        skin.add("medium", Label.LabelStyle(mediumFont, Color.LIGHT_GRAY))
        skin.add("small", Label.LabelStyle(smallFont, Color.LIGHT_GRAY))

        applyStyleToSkin(styleId, skin)
        return skin
    }

    private fun applyStyleToSkin(styleId: String, skin: Skin) {
        val collection = HUDTextureGenerator.generate(styleId, skin)
        val font = skin.getFont("default")
        skin.add("panel", collection.panel, Drawable::class.java)

        val scifiStyle = TextButton.TextButtonStyle().apply {
            up = collection.buttonUp; down = collection.buttonDown; over = collection.buttonUp
            this.font = font; fontColor = if (styleId == "IMPERIAL") Color.GOLD else Color.WHITE
        }
        skin.add("scifi", scifiStyle)
        skin.add("scifi", Button.ButtonStyle(scifiStyle.up, scifiStyle.down, scifiStyle.checked))

        val toggleStyle = TextButton.TextButtonStyle().apply {
            up = collection.toggleOff; checked = collection.toggleOn; over = collection.toggleOff
            this.font = font; fontColor = Color.BLACK
        }
        skin.add("mode-toggle", toggleStyle)

        // Register Mandatory Touchpad Styles
        val joystickStyle = Touchpad.TouchpadStyle().apply {
            background = collection.joystickBg; knob = collection.joystickKnob
        }
        skin.add("default", joystickStyle)
        skin.add("aim", joystickStyle)
        skin.add("drone-mode", joystickStyle)
    }

    fun swapSkin(styleId: String, skin: Skin) = applyStyleToSkin(styleId, skin)

    fun refreshUI(stage: Stage, skin: Skin) {
        stage.actors.forEach { actor ->
            if (actor is TextButton) actor.style = skin.get("scifi", TextButton.TextButtonStyle::class.java)
        }
    }

    fun createPermanentHUDEntities(world: World, player: Entity) {
        val rootAnchor = CommandMatrixFactory.createRootAnchor(world)
        CommandMatrixFactory.createCategoryShelf(world, rootAnchor)

        // Phase 25.4: Populate initial state
        CommandMatrixFactory.refreshModuleShelf(world, player)
        CommandMatrixFactory.refreshInstruments(world, player)
    }

    fun createInstrumentEntities(world: World, player: Entity, schema: TacticalSchema, defaultParent: Entity? = null) {
        val channelToEntity = mutableMapOf<String, Entity>()
        with(world) {
            schema.inputs.forEach { channel ->
                val pIdLocal = channel.relativeTo?.let { channelToEntity[it] } ?: defaultParent

                val hudEntity = entity {
                    it += HUDInstrumentTag(); it += HUDTagComponent(ownerId = "TACTICAL")
                    it += HUDElementComponent(
                        space = HUDSpace.SCREEN,
                        anchor = HUDAnchor.BOTTOM_RIGHT,
                        parentId = pIdLocal,
                        offsetX = channel.relX,
                        offsetY = channel.relY,
                        width = channel.width,
                        height = channel.height,
                        layer = HUDLayer.SYSTEM,
                        zIndex = 5
                    )
                    it += HUDRendererComponent(
                        renderType = if (channel.classification == ControlClassification.DATA_READOUT) HUDRenderType.TEXT else HUDRenderType.BLUEPRINT,
                        styleId = channel.styleId ?: "INDUSTRIAL",
                        localizationKey = channel.label
                    )
                    if (channel.classification != ControlClassification.DATA_READOUT) {
                        it += HUDInteractionComponent(isClickable = true, actionKey = channel.actionKey)
                    }
                }
                channelToEntity[channel.actionKey] = hudEntity
            }
        }
    }

    fun clearInstrumentEntities(world: World) {
        with(world) { family { all(HUDTagComponent) }.forEach { if (it[HUDTagComponent].ownerId == "TACTICAL") it.remove() } }
    }
}
