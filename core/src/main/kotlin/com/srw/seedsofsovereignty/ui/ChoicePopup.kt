package com.srw.seedsofsovereignty.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import com.srw.seedsofsovereignty.component.ChoiceComponent
import com.srw.seedsofsovereignty.component.WeaponModuleComponent
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * A highly efficient, minimalist comparison popup for strategic choices.
 * Optimized for scifi HUD aesthetics and vertical space.
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
    private val pendingDetails = WeaponDetailsWidget(skin, drawer, "EVOLUTION OFFER")

    init {
        background = skin.getDrawable("panel")
        pad(20f)

        // 1. Sleek Contextual Header
        val headerText = if (choiceType == ChoiceComponent.ChoiceType.EVOLUTION_UPGRADE) "ARCHITECTURAL EVOLUTION" else "EXOTIC TECH DISCOVERED"
        val headerColor = if (choiceType == ChoiceComponent.ChoiceType.EVOLUTION_UPGRADE) Color.GOLD else Color.CYAN

        val headerTable = Table(skin)
        headerTable.add(Label(headerText, skin, "title").apply { color = headerColor }).row()
        headerTable.add(Label(tierInfo, skin).apply { color = Color.GRAY })
        add(headerTable).colspan(2).padBottom(25f).row()

        // 2. Direct Comparison (Using refined Widgets for parity)
        currentDetails.update(currentModule)
        pendingDetails.update(pendingModule)

        val comparison = Table(skin)
        comparison.add(currentDetails).width(340f).padRight(15f)
        comparison.add(pendingDetails).width(340f).padLeft(15f)

        // Ensure comparison fits vertically on mobile
        val scroll = ScrollPane(comparison, skin)
        scroll.setScrollingDisabled(true, false)
        add(scroll).expand().fill().colspan(2).padBottom(30f).row()

        // 3. Integrated Action Footer
        val footer = Table(skin)
        val keepBtn = UIButtonFactory.createButton(skin, "KEEP EXISTING", UIButtonFactory.ButtonType.GENERIC) { onKeep() }
        val swapBtn = UIButtonFactory.createButton(skin, "EQUIP UPGRADE", UIButtonFactory.ButtonType.MARKET) { onSwap() }

        footer.add(keepBtn).size(320f, 85f).padRight(30f)
        footer.add(swapBtn).size(320f, 85f)

        add(footer).colspan(2).center()

        pack()
    }
}
