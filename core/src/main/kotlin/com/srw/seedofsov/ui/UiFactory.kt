package com.srw.seedofsov.ui

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Disposable
import com.srw.seedofsov.assets.UiAssetGenerator
import com.srw.seedofsov.assets.UiStyle
import ktx.async.KtxAsync
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Interface for components that need to react to UI asset availability.
 */
interface UiAssetListener {
    fun onAssetsReady()
}

/**
 * Internal task structure for the generation buffer.
 */
private data class GenerationResult(
    val name: String,
    val pixmap: Pixmap,
    val type: String // "NINEPATCH" or "TEXTURE"
)

/**
 * Factory for creating UI components using procedural assets.
 * Truth on Disk: Implements an atomic background generation buffer.
 * Fixes crashes by ensuring both Regions and Drawables are registered and
 * that the skin is not considered "loaded" until all dependencies are satisfied.
 */
class UiFactory(
    val style: UiStyle,
    val font: BitmapFont,
    val titleFont: BitmapFont
) : Disposable {
    private val generator = UiAssetGenerator(style)
    val skin = Skin()
    private val textures = mutableListOf<Texture>()
    private val listeners = mutableListOf<UiAssetListener>()

    // Thread-safe buffer for generated Pixmaps waiting for GL upload
    private val resultBuffer = ConcurrentLinkedQueue<GenerationResult>()

    private var totalTasks = 0
    private var completedTasks = 0
    var isLoaded = false
        private set

    init {
        font.data.setScale(2.0f)
        titleFont.data.setScale(2.5f)

        // Pre-add core fonts
        skin.add("default", Label.LabelStyle(font, style.palette.highlight))
        skin.add("title", Label.LabelStyle(titleFont, style.palette.primary))

        // Queue procedural generation tasks
        queueGeneration("button-up", "NINEPATCH") { generator.generateButton(32, 32, false) }
        queueGeneration("button-down", "NINEPATCH") { generator.generateButton(32, 32, true) }
        queueGeneration("panel", "NINEPATCH") { generator.generateBackgroundBox(32, 32) }
    }

    /**
     * Offloads the CPU-intensive Pixmap generation to a background thread.
     */
    private fun queueGeneration(name: String, type: String, block: () -> Pixmap) {
        totalTasks++
        KtxAsync.launch {
            val pixmap = block()
            resultBuffer.add(GenerationResult(name, pixmap, type))
        }
    }

    fun addListener(listener: UiAssetListener) {
        if (isLoaded) {
            listener.onAssetsReady()
        } else {
            listeners.add(listener)
        }
    }

    /**
     * Polled every frame by the LoadingScreen.
     * Uploads pending Pixmaps to the GPU (Main Thread only).
     */
    fun update(): Boolean {
        if (isLoaded) return true

        // Process a few results per frame
        repeat(3) {
            val result = resultBuffer.poll()
            if (result != null) {
                val texture = Texture(result.pixmap)
                textures.add(texture)

                // Logcat Fix: Always add as a TextureRegion so getRegion() doesn't fail
                val region = TextureRegion(texture)
                skin.add(result.name, region, TextureRegion::class.java)

                when (result.type) {
                    "NINEPATCH" -> {
                        skin.add(result.name, NinePatchDrawable(NinePatch(region, 10, 10, 10, 10)), Drawable::class.java)
                    }
                    "TEXTURE" -> {
                        skin.add(result.name, texture, Texture::class.java)
                    }
                }

                result.pixmap.dispose()
                completedTasks++
            }
        }

        // Finalize only when ALL background tasks are done and uploaded
        if (totalTasks > 0 && completedTasks >= totalTasks && !isLoaded) {
            finalizeSkin()
        }

        return isLoaded
    }

    private fun finalizeSkin() {
        // Now it is safe to create styles that depend on the generated assets
        skin.add("default", TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("button-up")
            down = skin.getDrawable("button-down")
            this.font = this@UiFactory.font
            fontColor = style.palette.primary
        })

        isLoaded = true
        // Notify all subscribers that assets are safe to use.
        listeners.forEach { it.onAssetsReady() }
        listeners.clear()
    }

    fun getProgress(): Float = if (totalTasks == 0) 1f else completedTasks.toFloat() / totalTasks

    fun createButton(text: String): TextButton {
        if (!isLoaded) throw IllegalStateException("UI Factory Assets not ready ($completedTasks/$totalTasks). Register a UiAssetListener.")
        return TextButton(text, skin)
    }

    fun createLabel(text: String, styleName: String = "default"): Label = Label(text, skin, styleName)
    fun createTitle(text: String): Label = Label(text, skin, "title")

    override fun dispose() {
        skin.dispose()
        textures.forEach { it.dispose() }
        listeners.clear()
    }
}
