package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.srw.seedsofsovereignty.component.*

/**
 * COMMAND MATRIX VIEW: Manages the UI components for module selection and control.
 * Refactored to support Dual-Widget configuration (Tactical vs Navigation).
 */
class CommandMatrixView(
    private val skin: Skin,
    private val filter: Set<ModuleCategory>? = null,
    private val isRightAnchored: Boolean = false
) {

    private var world: World? = null
    val root = Table()
    private val selectionShelf: Table
    private val controlArea: Table
    private val categoryShelf: Table

    private var currentCategory: ModuleCategory? = null
    var joystickSize: Float = 120f

    init {
        selectionShelf = Table(skin)
        controlArea = Table(skin)
        categoryShelf = Table(skin)

        val panelBg = skin.getDrawable("panel_bg")
        selectionShelf.background = panelBg
        controlArea.background = panelBg
        categoryShelf.background = panelBg

        // Align internal content based on screen corner
        val alignSide = if (isRightAnchored) Align.right else Align.left
        selectionShelf.align(alignSide)
        categoryShelf.align(alignSide)

        // Build the layout. Ensure categoryShelf is pinned to the bottom.
        root.apply {
            align(Align.bottom)
            add(selectionShelf).expandX().fillX().height(60f).padBottom(2f).row()
            add(controlArea).expand().fill().row()
            add(categoryShelf).expandX().fillX().height(50f).padTop(2f)
            setTransform(true)
        }

        controlArea.add(TextButton("SELECT MODULE", skin)).expand().fill().pad(10f)
    }

    fun attachWorld(world: World) {
        this.world = world
    }

    fun updateCategories(activeCategories: Set<ModuleCategory>) {
        categoryShelf.clearChildren()

        val filtered = if (filter != null) {
            activeCategories.filter { it in filter }
        } else {
            activeCategories.filter { it != ModuleCategory.NAVIGATION && it != ModuleCategory.ENGINE }
        }

        filtered.forEach { cat ->
            val btn = TextButton(cat.name, skin, cat.name.lowercase())
            // For right-anchored hub, add to the right side of the table
            if (isRightAnchored) {
                categoryShelf.add(btn).right().height(45f).pad(1f)
            } else {
                categoryShelf.add(btn).left().height(45f).pad(1f)
            }

            btn.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    switchToCategory(cat)
                }
            })
        }

        // AUTO-SELECT: If only one category, switch to it
        if (filtered.size == 1 && currentCategory == null) {
            switchToCategory(filtered.first())
        }
    }

    private fun switchToCategory(category: ModuleCategory) {
        currentCategory = category
        applyGlitchEffect(root)
        root.color = getCategoryColor(category).cpy().apply { a = 0.8f }
        populateSelectionShelf(category)
    }

    private fun populateSelectionShelf(category: ModuleCategory) {
        val w = world ?: return
        selectionShelf.clearChildren()

        w.run {
            val player = family { all(PlayerComponent) }.firstOrNull() ?: return
            val targets = mutableListOf<Entity>()

            family { all(ModuleComponent, AttachmentComponent) }.forEach { entity ->
                if (entity[AttachmentComponent].parent == player && entity[ModuleComponent].category == category) {
                    targets.add(entity)
                }
            }

            if (category == ModuleCategory.POWER && player.has(PowerCoreComponent)) targets.add(player)
            if (category == ModuleCategory.SHIELDS && player.has(ArcedDefenseComponent)) targets.add(player)

            targets.forEach { entity ->
                val label = if (entity == player) category.name else "MOD-${entity.id}"
                val btn = TextButton(label, skin)
                if (isRightAnchored) selectionShelf.add(btn).right().height(55f).pad(2f)
                else selectionShelf.add(btn).left().height(55f).pad(2f)

                btn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        selectModule(entity)
                    }
                })
            }

            // AUTO-SELECT: If only one module, display it
            if (targets.size == 1) {
                selectModule(targets.first())
            }
        }
    }

    private fun selectModule(target: Entity) {
        val w = world ?: return
        w.run {
            family { all(SelectedModuleComponent) }.forEach { entity ->
                entity.configure { it -= SelectedModuleComponent }
            }
            target.configure { it += SelectedModuleComponent() }
        }

        applyApertureTransition()
        updateControlArea(target)
    }

    private fun applyApertureTransition() {
        controlArea.clearActions()
        controlArea.setScale(1f, 0f)
        controlArea.setOrigin(Align.center)
        controlArea.setTransform(true)
        controlArea.addAction(Actions.scaleTo(1f, 1f, 0.25f, Interpolation.exp5Out))
    }

    private fun applyGlitchEffect(actor: Actor) {
        actor.clearActions()
        val flareColor = Color(1f, 1f, 1.2f, 1f)
        val originalColor = Color(1f, 1f, 1f, 0.8f)

        actor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.color(flareColor, 0.05f),
                Actions.moveBy(MathUtils.random(-5f, 5f), MathUtils.random(-5f, 5f), 0.03f)
            ),
            Actions.parallel(
                Actions.color(originalColor, 0.05f),
                Actions.moveTo(actor.x, actor.y, 0.03f)
            )
        ))
    }

    fun updateControlArea(entity: Entity) {
        val w = world ?: return
        val s = skin
        controlArea.clearChildren()

        w.run {
            if (entity.has(ModuleChannelsComponent)) {
                val channels = entity[ModuleChannelsComponent]
                val definition = ModuleRegistry.get(channels.type)

                val stack = Stack()
                val layoutTable = Table()
                layoutTable.setFillParent(true)

                definition.controls.forEach { controlDef ->
                    val widget = createWidget(controlDef, channels)
                    val container = Table().apply {
                        val styleName = if (s.has("small", Label.LabelStyle::class.java)) "small" else "default"
                        add(Label(controlDef.label, s, styleName)).row()
                        add(widget).size(if (controlDef.widgetType == ControlWidgetType.JOYSTICK_2D) joystickSize else 70f)
                    }
                    layoutTable.add(container).expand().align(getAlign(controlDef.relativePosition.x, controlDef.relativePosition.y))
                }

                val overlayTable = Table()
                overlayTable.setFillParent(true)
                definition.outputs.forEach { outputDef ->
                    val outputData = channels.outputs[outputDef.channelKey] ?: return@forEach
                    val bar = ProgressBar(0f, 1f, 0.01f, false, s)
                    bar.value = outputData.status

                    val container = Table(s).apply { background = s.getDrawable("panel_bg") }
                    val styleName = if (s.has("small", Label.LabelStyle::class.java)) "small" else "default"
                    container.add(Label(outputDef.label, s, styleName)).left().padLeft(4f).row()
                    container.add(bar).height(8f).fillX().pad(2f)

                    overlayTable.add(container).size(120f, 20f).expand().align(getAlign(outputDef.relativePosition.x, outputDef.relativePosition.y)).pad(5f)
                }

                stack.add(layoutTable)
                stack.add(overlayTable)
                controlArea.add(stack).expand().fill()
            }
        }
    }

    private fun createWidget(definition: ControlWidgetDefinition, channels: ModuleChannelsComponent): Actor {
        val s = skin
        val input = channels.inputs[definition.channelKey] ?: InputChannelData()

        return when (definition.widgetType) {
            ControlWidgetType.JOYSTICK_2D -> {
                Touchpad(10f, s).apply {
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            input.vector.set(knobPercentX, knobPercentY)
                            input.triggered = (knobPercentX != 0f || knobPercentY != 0f)
                        }
                    })
                }
            }
            ControlWidgetType.BUTTON_DISCRETE -> {
                TextButton(definition.label, s).apply {
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            input.triggered = isPressed
                        }
                    })
                }
            }
            ControlWidgetType.TOUCHPAD_2D -> Touchpad(0f, s)
            ControlWidgetType.SLIDER_1D -> {
                Slider(0f, 1f, 0.01f, definition.orientation != 0f, s).apply {
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            input.value = value
                            input.triggered = true
                        }
                    })
                }
            }
        }
    }

    private fun getAlign(x: Float, y: Float): Int {
        var align = 0
        if (y > 0.66f) align = align or Align.top
        else if (y < 0.33f) align = align or Align.bottom
        else align = align or Align.center

        if (x > 0.66f) align = align or Align.right
        else if (x < 0.33f) align = align or Align.left
        else align = align or Align.center
        return align
    }

    private fun getCategoryColor(category: ModuleCategory): Color {
        return when (category) {
            ModuleCategory.PRIMARY -> Color.RED
            ModuleCategory.SECONDARY -> Color.ORANGE
            ModuleCategory.TERTIARY -> Color.YELLOW
            ModuleCategory.QUADRARY -> Color.PURPLE
            ModuleCategory.SHIELDS -> Color.BLUE
            ModuleCategory.POWER -> Color.GREEN
            ModuleCategory.DRONES -> Color.CYAN
            ModuleCategory.FIGHTERS -> Color.WHITE
            ModuleCategory.NAVIGATION -> Color.SKY
            ModuleCategory.ENGINE -> Color.LIME
        }
    }
}
