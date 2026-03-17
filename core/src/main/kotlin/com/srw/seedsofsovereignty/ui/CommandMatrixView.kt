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
 * Adheres to Fleks 2.x patterns.
 */
class CommandMatrixView(private val world: World, private val skin: Skin) {

    val root = Table()
    private val selectionShelf = Table(skin)
    private val controlArea = Table(skin)
    val categoryShelf = Table(skin)

    private var currentCategory: ModuleCategory? = null
    var joystickSize: Float = 120f

    init {
        val panelBg = skin.getDrawable("panel_bg")
        selectionShelf.background = panelBg
        controlArea.background = panelBg
        categoryShelf.background = panelBg

        root.apply {
            add(selectionShelf).expandX().fillX().height(80f).row()
            add(controlArea).expand().fill()
            setTransform(true)
        }

        controlArea.add(TextButton("SELECT A MODULE", skin)).expand().fill().pad(10f)
    }

    fun updateCategories(activeCategories: Set<ModuleCategory>) {
        categoryShelf.clearChildren()
        ModuleCategory.entries.forEach { cat ->
            if (cat in activeCategories) {
                val btn = TextButton(cat.name, skin, cat.name.lowercase())
                categoryShelf.add(btn).size(180f, 80f).pad(2f)

                btn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        currentCategory = cat
                        applyGlitchEffect(root)
                        root.color = getCategoryColor(cat).cpy().apply { a = 0.8f }
                        populateSelectionShelf(cat)
                    }
                })
            }
        }
        categoryShelf.pack()
    }

    private fun populateSelectionShelf(category: ModuleCategory) {
        selectionShelf.clearChildren()

        world.run {
            val player = family { all(PlayerComponent) }.firstOrNull() ?: return
            val targets = mutableListOf<Entity>()

            when {
                category == ModuleCategory.POWER && player.has(PowerCoreComponent) -> targets.add(player)
                category == ModuleCategory.SHIELDS && player.has(ArcedDefenseComponent) -> targets.add(player)
                else -> {
                    family { all(ModuleComponent, AttachmentComponent) }.forEach { entity ->
                        if (entity[AttachmentComponent].parent == player && entity[ModuleComponent].category == category) {
                            targets.add(entity)
                        }
                    }
                }
            }

            if (targets.size > 1) {
                val masterBtn = TextButton("MASTER", skin, "toggle")
                selectionShelf.add(masterBtn).height(70f).pad(2f)
                masterBtn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        selectMaster(player, category, targets.firstOrNull())
                    }
                })
            }

            targets.forEach { entity ->
                val label = if (entity == player) category.name else "MOD-${entity.id}"
                val btn = TextButton(label, skin)
                selectionShelf.add(btn).height(70f).pad(2f)

                btn.addListener(object : ChangeListener() {
                    override fun changed(event: ChangeEvent, actor: Actor) {
                        selectModule(entity)
                    }
                })
            }
        }
    }

    private fun selectMaster(player: Entity, category: ModuleCategory, representative: Entity?) {
        world.run {
            var master = family { all(GroupMasterComponent) }.firstOrNull {
                it[GroupMasterComponent].category == category && it.getOrNull(AttachmentComponent)?.parent == player
            }

            if (master == null && representative != null && representative.has(ModuleChannelsComponent)) {
                val repChannels = representative[ModuleChannelsComponent]
                master = world.entity {
                    it += GroupMasterComponent(category)
                    it += AttachmentComponent(parent = player)

                    val newInputs = mutableMapOf<String, InputChannelData>()
                    repChannels.inputs.keys.forEach { key -> newInputs[key] = InputChannelData() }
                    it += ModuleChannelsComponent(type = repChannels.type, inputs = newInputs)
                }
            }

            master?.let { selectModule(it) }
        }
    }

    private fun selectModule(target: Entity) {
        world.run {
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
        val originalX = actor.x
        val originalY = actor.y
        val flareColor = Color(1f, 1f, 1.2f, 1f)
        val originalColor = Color(1f, 1f, 1f, 0.8f)

        actor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.color(flareColor, 0.05f),
                Actions.moveBy(MathUtils.random(-5f, 5f), MathUtils.random(-5f, 5f), 0.03f)
            ),
            Actions.parallel(
                Actions.color(originalColor, 0.05f),
                Actions.moveTo(originalX, originalY, 0.03f)
            )
        ))
    }

    fun updateControlArea(entity: Entity) {
        val s = skin
        controlArea.clearChildren()

        world.run {
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

                if (entity.has(GroupMasterComponent)) {
                    val group = entity[GroupMasterComponent]
                    val header = Label("MASTER CONTROL: ${group.category}", s).apply { color = Color.GOLD }
                    controlArea.addActor(header)
                    header.setPosition(10f, 10f)
                }

            } else {
                val labelText = when {
                    entity.has(PlayerComponent) -> if (currentCategory == ModuleCategory.POWER) "POWER CORE" else "SHIELD MATRIX"
                    else -> "UNKNOWN MODULE"
                }
                controlArea.add(TextButton(labelText, s)).expand().fill().pad(10f)
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
        val vertical = when {
            y > 0.66f -> Align.top
            y < 0.33f -> Align.bottom
            else -> Align.center
        }
        val horizontal = when {
            x > 0.66f -> Align.right
            x < 0.33f -> Align.left
            else -> Align.center
        }
        return vertical or horizontal
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
        }
    }
}
