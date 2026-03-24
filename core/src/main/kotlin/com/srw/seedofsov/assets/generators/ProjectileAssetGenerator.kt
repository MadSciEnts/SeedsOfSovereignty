package com.srw.seedofsov.assets.generators

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.srw.seedofsov.assets.UiStyle

/**
 * Procedurally generates visuals for weapon projectiles, components, and impact effects.
 */
class ProjectileAssetGenerator(private val style: UiStyle) {

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

    fun generateBarrel(): Pixmap {
        val pixmap = Pixmap(16, 64, Pixmap.Format.RGBA8888)
        pixmap.setColor(0.2f, 0.2f, 0.2f, 1f)
        pixmap.fillRectangle(4, 0, 8, 64)
        pixmap.setColor(style.palette.primary)
        pixmap.drawRectangle(4, 0, 8, 64)
        return pixmap
    }

    /**
     * Generates a high-fidelity "Phase Ripple" for shield impacts.
     * Truth on Disk: Uses concentric rings with varied alpha for electromagnetic feel.
     */
    fun generateShieldImpact(): Pixmap {
        val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(32, 32, 12)
        pixmap.setColor(Color.CYAN)
        pixmap.drawCircle(32, 32, 28)
        pixmap.drawCircle(32, 32, 27)
        pixmap.setColor(0.2f, 0.6f, 1.0f, 0.4f)
        pixmap.drawCircle(32, 32, 20)
        pixmap.drawCircle(32, 32, 19)
        pixmap.setColor(0.1f, 0.4f, 0.8f, 0.2f)
        pixmap.fillCircle(32, 32, 31)
        return pixmap
    }

    /**
     * Generates a solid metallic shard for visceral hull damage.
     * Truth on Disk: Asymmetric solid geometry replaces outlines.
     */
    fun generateHullDebris(): Pixmap {
        val pixmap = Pixmap(32, 32, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.DARK_GRAY)
        pixmap.fillTriangle(2, 12, 30, 2, 14, 30)
        pixmap.setColor(Color.LIGHT_GRAY)
        pixmap.drawLine(2, 12, 30, 2)
        pixmap.drawLine(30, 2, 14, 30)
        pixmap.drawLine(14, 30, 2, 12)
        return pixmap
    }

    /**
     * Technical spark particle for energy shattering.
     */
    fun generateSpark(): Pixmap {
        val pixmap = Pixmap(8, 8, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        return pixmap
    }

    fun generateExplosion(): Pixmap {
        val pixmap = Pixmap(64, 64, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.ORANGE)
        pixmap.fillCircle(32, 32, 30)
        pixmap.setColor(Color.YELLOW)
        pixmap.fillCircle(32, 32, 20)
        pixmap.setColor(Color.WHITE)
        pixmap.fillCircle(32, 32, 10)
        return pixmap
    }
}
