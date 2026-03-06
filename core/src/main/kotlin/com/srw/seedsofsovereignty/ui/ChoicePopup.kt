package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.srw.seedsofsovereignty.component.ChoiceComponent
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * A professional modal popup for making strategic module choices.
 * Optimized for clarity, purpose, and vertical space.
 */
class ChoicePopup(
    private val skin: Skin,
    private val drawer: ShapeDrawer,
    private val choiceType: ChoiceComponent.ChoiceType,
    private val tierInfo: String,
    currentModule: WeaponModuleComponent,
    pendingModule: WeaponModuleComponent,
    private val onKeep: () -> Unit,
    private val onSwap: () -> Unit
) : Table(skin) {

    private val currentDetails = WeaponDetailsWidget(skin, drawer, "CURRENTLY EQUIPPED")
    private val pendingDetails = WeaponDetailsWidget(skin, drawer, "NEW MODULE")

    init {
        background = skin.getDrawable("panel")
        pad(25f)

        // 1. Dynamic Header Logic
        val (headerText, subHeaderText, headerColor) = when (choiceType) {
            ChoiceComponent.ChoiceType.EVOLUTION_UPGRADE -> Triple(
                "ARCHITECTURAL EVOLUTION",
                "Tier advancement protocol initiated. Choose your primary hardware baseline.",
                Color.GOLD
            )
            ChoiceComponent.ChoiceType.LOOT_FOUND -> Triple(
                "EXOTIC TECH DISCOVERED",
                "Salvaged hardware signature identified. Analyze compatibility with current hull.",
                Color.CYAN
            )
        }

        val headerLabel = Label(headerText, skin, "title").apply { color = headerColor }
        val tierLabel = Label(tierInfo, skin).apply { color = Color.GRAY }
        val subLabel = Label(subHeaderText, skin).apply {
            wrap = true
            setAlignment(com.badlogic.gdx.utils.Align.center)
        }

        add(headerLabel).colspan(2).row()
        add(tierLabel).colspan(2).padBottom(5f).row()
        add(subLabel).width(600f).colspan(2).padBottom(30f).row()

        // 2. Side-by-Side Comparison
        currentDetails.update(currentModule)
        pendingDetails.update(pendingModule)

        val comparisonTable = Table(skin)
        comparisonTable.add(currentDetails).pad(5f).fillY()
        comparisonTable.add(pendingDetails).pad(5f).fillY()

        val scroll = ScrollPane(comparisonTable, skin)
        scroll.setScrollingDisabled(true, false)
        add(scroll).expand().fill().colspan(2).padBottom(25f).row()

        // 3. Action Buttons
        val buttonTable = Table(skin)
        val keepBtn = UIButtonFactory.createButton(skin, "KEEP EXISTING", UIButtonFactory.ButtonType.GENERIC) {
            onKeep()
        }
        val swapBtn = UIButtonFactory.createButton(skin, "EQUIP UPGRADE", UIButtonFactory.ButtonType.MARKET) {
            onSwap()
        }

        buttonTable.add(keepBtn).size(340f, 85f).padRight(30f)
        buttonTable.add(swapBtn).size(340f, 85f)

        add(buttonTable).colspan(2).center()

        pack()
    }
}
