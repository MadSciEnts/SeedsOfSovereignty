package com.srw.seedofsov.assets.generators

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.UiStyle

/**
 * Procedurally generates visuals for weapon projectiles and components.
 */
class ProjectileAssetGenerator(private val style: UiStyle) {

    /**
     * Generates a glowing kinetic bolt for primary weapons.
     */
    fun generateKineticBolt(): Pixmap {
        val pixmap = Pixmap(16, 64, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fillRectangle(6, 10, 4, 44)
        val glowColor = style.palette.primary.cpy()
        glowColor.a = 0.5f
        pixmap.setColor(glowColor)
        pixmap.drawRectangle(4, 5, 8, 54)
        return pixmap
    }

    /**
     * Generates a technical missile hull for secondary weapons.
     */
    fun generateMissile(): Pixmap {
        val pixmap = Pixmap(32, 64, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.GRAY)
        pixmap.fillRectangle(8, 10, 16, 40)
        pixmap.setColor(Color.RED)
        pixmap.fillTriangle(16, 0, 8, 10, 24, 10)
        pixmap.setColor(style.palette.highlight)
        pixmap.fillRectangle(12, 50, 8, 10)
        return pixmap
    }

    /**
     * Generates a technical weapon barrel texture.
     */
    fun generateBarrel(): Pixmap {
        val pixmap = Pixmap(16, 64, Pixmap.Format.RGBA8888)
        // Dark metallic base
        pixmap.setColor(0.2f, 0.2f, 0.2f, 1f)
        pixmap.fillRectangle(4, 0, 8, 64)
        // Technical trim (Primary color)
        pixmap.setColor(style.palette.primary)
        pixmap.drawRectangle(4, 0, 8, 64)
        return pixmap
    }
}
