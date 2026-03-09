package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.srw.seedsofsovereignty.ui.TextureCollection

/**
 * Interface for modular UI styling generators.
 * v0.5.0: Phase 7.3 - Tactical Style Interface.
 */
interface TacticalStyleGenerator {
    /**
     * Generates a TextureCollection for the given skin.
     */
    fun generate(skin: Skin): TextureCollection
}
