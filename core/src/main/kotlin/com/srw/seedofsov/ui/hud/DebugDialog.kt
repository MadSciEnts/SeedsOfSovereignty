package com.srw.seedofsov.ui.hud

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.srw.seedofsov.SovereigntyGame
import com.srw.seedofsov.ecs.components.*
import com.srw.seedofsov.model.*
import com.srw.seedofsov.ui.Panel
import com.srw.seedofsov.ui.UiAssetListener
import ktx.ashley.get

/**
 * Advanced Debug Interface for real-time ship and module manipulation.
 * Truth on Disk: Supports Surgical Editing, Save Slots, and Spawner controls.
 */
class DebugDialog(val game: SovereigntyGame) : Panel(), UiAssetListener {

    private val contentArea = Panel()
    private val editorArea = Panel()

    private var activeTab = "PLAYER"
    private var playerEntity: Entity? = null
    private var targetShip: ShipComponent? = null
    private var targetStats: ShipStatsComponent? = null

    private val prefs = Gdx.app.getPreferences("sovereignty_debug_saves")
    private var lastSpawnTier = 1

    init {
        setSize(850f, 650f)
        isVisible = false
        game.uiFactory.addListener(this)
    }

    override fun onAssetsReady() {
        background = game.uiFactory.skin.getDrawable("panel")
        setupLayout()
    }

    private fun setupLayout() {
        clearChildren()

        // 1. Tab Bar
        val tabs = listOf("PLAYER", "SPAWNER", "SAVES")
        var tx = 20f
        tabs.forEach { tabName ->
            val btn = game.uiFactory.createButton(tabName).apply {
                setSize(150f, 50f)
                setPosition(tx, height - 70f)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        activeTab = tabName
                        refreshContent()
                    }
                })
            }
            addActor(btn)
            tx += 160f
        }

        // Close Button
        val closeBtn = game.uiFactory.createButton("X").apply {
            setSize(50f, 50f)
            setPosition(width - 70f, height - 70f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    isVisible = false
                    editorArea.isVisible = false
                }
            })
        }
        addActor(closeBtn)

        contentArea.setSize(width - 40f, height - 120f)
        contentArea.setPosition(20f, 20f)
        addActor(contentArea)

        editorArea.setSize(480f, 580f)
        editorArea.setPosition(width + 20f, 50f)
        editorArea.isVisible = false
        addActor(editorArea)

        refreshContent()
    }

    fun linkPlayer(entity: Entity) {
        playerEntity = entity
        targetShip = entity[ShipComponent.mapper]
        targetStats = entity[ShipStatsComponent.mapper]
        if (isVisible) refreshContent()
    }

    fun toggle() {
        isVisible = !isVisible
        if (!isVisible) editorArea.isVisible = false
        if (isVisible) {
            refreshContent()
            toFront()
        }
    }

    private fun refreshContent() {
        contentArea.clearChildren()
        when (activeTab) {
            "PLAYER" -> drawPlayerTab()
            "SPAWNER" -> drawSpawnerTab()
            "SAVES" -> drawSavesTab()
        }
    }

    private fun drawPlayerTab() {
        val stats = targetStats ?: return
        val ship = targetShip ?: return

        var cy = contentArea.height - 40f
        contentArea.addActor(game.uiFactory.createTitle("PLAYER TIER: ${stats.tier}").apply { setPosition(20f, cy) })

        cy -= 60f
        ship.slots.forEach { slot ->
            val mod = slot.equippedModule
            val modName = mod?.name ?: "EMPTY"
            val label = game.uiFactory.createLabel("${slot.category.displayName}: $modName").apply {
                setPosition(20f, cy)
                color = if (mod != null) Color.CYAN else Color.GRAY
            }
            contentArea.addActor(label)

            if (mod != null) {
                val editBtn = game.uiFactory.createButton("EDIT").apply {
                    setSize(80f, 30f)
                    setPosition(contentArea.width - 190f, cy)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            openSurgicalEditor(slot)
                        }
                    })
                }
                contentArea.addActor(editBtn)

                val rerollBtn = game.uiFactory.createButton("ROLL").apply {
                    setSize(80f, 30f)
                    setPosition(contentArea.width - 100f, cy)
                    addListener(object : ClickListener() {
                        override fun clicked(event: InputEvent?, x: Float, y: Float) {
                            val newMod = game.entityFactory.generateModule(slot.category, stats.tier, stats.subTier)
                            playerEntity?.let { game.entityFactory.injectModule(it, slot, newMod) }
                            refreshContent()
                        }
                    })
                }
                contentArea.addActor(rerollBtn)
            }
            cy -= 40f
        }
    }

    private fun drawSpawnerTab() {
        contentArea.addActor(game.uiFactory.createTitle("SPAWN ENEMY").apply { setPosition(20f, contentArea.height - 40f) })

        var sy = contentArea.height - 100f
        val tierLabel = game.uiFactory.createLabel("TIER: $lastSpawnTier").apply { setPosition(20f, sy) }
        contentArea.addActor(tierLabel)

        val decBtn = game.uiFactory.createButton("-").apply {
            setSize(50f, 40f)
            setPosition(120f, sy)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    lastSpawnTier = (lastSpawnTier - 1).coerceAtLeast(1)
                    tierLabel.setText("TIER: $lastSpawnTier")
                }
            })
        }
        val incBtn = game.uiFactory.createButton("+").apply {
            setSize(50f, 40f)
            setPosition(180f, sy)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    lastSpawnTier = (lastSpawnTier + 1).coerceAtMost(48)
                    tierLabel.setText("TIER: $lastSpawnTier")
                }
            })
        }
        contentArea.addActor(decBtn)
        contentArea.addActor(incBtn)

        val spawnBtn = game.uiFactory.createButton("SPAWN NEAR").apply {
            setSize(200f, 60f)
            setPosition(20f, sy - 80f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    val playerTransform = playerEntity?.get(TransformComponent.mapper)
                    val px = playerTransform?.position?.x ?: 0f
                    val py = playerTransform?.position?.y ?: 0f
                    game.entityFactory.createEnemy(px + 10f, py + 10f, lastSpawnTier)
                }
            })
        }
        contentArea.addActor(spawnBtn)
    }

    private fun drawSavesTab() {
        contentArea.addActor(game.uiFactory.createTitle("CONFIG SLOTS").apply { setPosition(20f, contentArea.height - 40f) })
        var sy = contentArea.height - 100f
        for (i in 1..3) {
            val saveBtn = game.uiFactory.createButton("SAVE SLOT $i").apply {
                setSize(180f, 40f)
                setPosition(20f, sy)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        saveConfiguration(i)
                    }
                })
            }
            val loadBtn = game.uiFactory.createButton("LOAD SLOT $i").apply {
                setSize(180f, 40f)
                setPosition(220f, sy)
                addListener(object : ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        loadConfiguration(i)
                    }
                })
            }
            contentArea.addActor(saveBtn)
            contentArea.addActor(loadBtn)
            sy -= 60f
        }
    }

    private fun openSurgicalEditor(slot: ModuleSlot) {
        editorArea.clearChildren()
        editorArea.background = game.uiFactory.skin.getDrawable("panel")
        editorArea.isVisible = true

        val module = slot.equippedModule ?: return
        editorArea.addActor(game.uiFactory.createTitle("SURGICAL: ${module.name}").apply { setPosition(20f, editorArea.height - 40f) })

        var ey = editorArea.height - 110f
        when (module) {
            is ImpulseEngineComponent -> {
                addSurgicalRow("THRUST", module.thrustPower, ey) { module.thrustPower = it }
                ey -= 110f
                addSurgicalRow("ROTATION", module.maxRotationSpeed, ey) { module.maxRotationSpeed = it }
            }
            is ShieldModuleComponent -> {
                addSurgicalRow("MAX HP", module.maxShieldStrength, ey) { module.maxShieldStrength = it; module.shieldStrength = it }
                ey -= 110f
                addSurgicalRow("REGEN", module.rechargeRate, ey) { module.rechargeRate = it }
            }
            is WeaponModuleComponent -> {
                addSurgicalRow("WPN SPD", module.projectileSpeed, ey) { module.projectileSpeed = it }
                ey -= 110f
                addSurgicalRow("FIRE RATE", module.fireRate, ey) { module.fireRate = it }
            }
        }
    }

    private fun addSurgicalRow(label: String, initial: Float, yPos: Float, onUpdate: (Float) -> Unit) {
        val row = Panel()
        row.setSize(editorArea.width - 40f, 100f)
        row.setPosition(20f, yPos)
        editorArea.addActor(row)

        var currentVal = initial
        var rangeMultiplier = 100f

        val nameLabel = game.uiFactory.createLabel(label).apply { setPosition(0f, 70f); color = Color.GOLD }
        val valLabel = game.uiFactory.createLabel("%.2f".format(currentVal)).apply { setPosition(row.width - 120f, 70f); color = Color.WHITE }
        row.addActor(nameLabel)
        row.addActor(valLabel)

        val rangeLabel = game.uiFactory.createLabel("RANGE: x$rangeMultiplier").apply { setPosition(0f, 5f); color = Color.GRAY }
        row.addActor(rangeLabel)

        val decRange = game.uiFactory.createButton("-").apply {
            setSize(40f, 30f)
            setPosition(0f, 30f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    rangeMultiplier = (rangeMultiplier / 10f).coerceAtLeast(1f)
                    rangeLabel.setText("RANGE: x$rangeMultiplier")
                }
            })
        }
        val incRange = game.uiFactory.createButton("+").apply {
            setSize(40f, 30f)
            setPosition(50f, 30f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    rangeMultiplier = (rangeMultiplier * 10f).coerceAtMost(1000f)
                    rangeLabel.setText("RANGE: x$rangeMultiplier")
                }
            })
        }
        row.addActor(decRange)
        row.addActor(incRange)

        val sliderBar = Panel(game.uiFactory.skin.getDrawable("button-down"))
        sliderBar.setSize(row.width - 150f, 20f)
        sliderBar.setPosition(130f, 35f)
        row.addActor(sliderBar)

        val knob = Panel(game.uiFactory.skin.getDrawable("button-up"))
        knob.setSize(30f, 40f)
        knob.setPosition(130f + ((currentVal / rangeMultiplier).coerceIn(0f, 1f) * (sliderBar.width - 30f)), 25f)
        row.addActor(knob)

        val listener = object : ClickListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                handleUpdate(x)
                return true
            }
            override fun touchDragged(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                handleUpdate(x)
            }
            private fun handleUpdate(touchX: Float) {
                val pct = (touchX / sliderBar.width).coerceIn(0f, 1f)
                currentVal = pct * rangeMultiplier
                knob.setPosition(130f + (pct * (sliderBar.width - 30f)), 25f)
                valLabel.setText("%.2f".format(currentVal))
                onUpdate(currentVal)
            }
        }
        sliderBar.addListener(listener)
    }

    private fun saveConfiguration(slot: Int) {
        val stats = targetStats ?: return
        val ship = targetShip ?: return

        prefs.putInteger("slot_${slot}_tier", stats.tier)
        prefs.putString("slot_${slot}_subtier", stats.subTier.name)

        ship.slots.forEach { s ->
            s.equippedModule?.let { m ->
                val key = "slot_${slot}_mod_${s.category.name}"
                when (m) {
                    is ImpulseEngineComponent -> {
                        prefs.putFloat("${key}_thrust", m.thrustPower)
                        prefs.putFloat("${key}_turn", m.maxRotationSpeed)
                    }
                    is ShieldModuleComponent -> {
                        prefs.putFloat("${key}_hp", m.maxShieldStrength)
                        prefs.putFloat("${key}_regen", m.rechargeRate)
                    }
                    is WeaponModuleComponent -> {
                        prefs.putFloat("${key}_spd", m.projectileSpeed)
                        prefs.putFloat("${key}_rate", m.fireRate)
                    }
                }
            }
        }
        prefs.flush()
        Gdx.app.log("DEBUG", "Saved configuration to slot $slot")
    }

    private fun loadConfiguration(slot: Int) {
        if (!prefs.contains("slot_${slot}_tier")) return

        val stats = targetStats ?: return
        val ship = targetShip ?: return
        val entity = playerEntity ?: return

        stats.tier = prefs.getInteger("slot_${slot}_tier")
        stats.subTier = Subtier.valueOf(prefs.getString("slot_${slot}_subtier"))

        ship.slots.forEach { s ->
            val key = "slot_${slot}_mod_${s.category.name}"
            if (prefs.contains("${key}_thrust") || prefs.contains("${key}_hp") || prefs.contains("${key}_spd")) {
                val newMod = game.entityFactory.generateModule(s.category, stats.tier, stats.subTier)
                when (newMod) {
                    is ImpulseEngineComponent -> {
                        newMod.thrustPower = prefs.getFloat("${key}_thrust")
                        newMod.maxRotationSpeed = prefs.getFloat("${key}_turn")
                    }
                    is ShieldModuleComponent -> {
                        newMod.maxShieldStrength = prefs.getFloat("${key}_hp")
                        newMod.shieldStrength = newMod.maxShieldStrength
                        newMod.rechargeRate = prefs.getFloat("${key}_regen")
                    }
                    is WeaponModuleComponent -> {
                        newMod.projectileSpeed = prefs.getFloat("${key}_spd")
                        newMod.fireRate = prefs.getFloat("${key}_rate")
                    }
                }
                game.entityFactory.injectModule(entity, s, newMod)
            }
        }
        refreshContent()
        Gdx.app.log("DEBUG", "Loaded configuration from slot $slot")
    }
}
