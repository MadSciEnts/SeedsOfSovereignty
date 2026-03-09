# MISSION: Phase 8.2 - Pop-up & Table Unification (Surgical Implementer)

## STEP 2: Evolution Report Refactor
- **File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/EvolutionReportPopup.kt`

### ACTIONS
1.  **Set Background**: Ensure `background = skin.getDrawable("panel")` is present in the `init` block (it is already there, but confirm).
2.  **Refactor `confirmBtn`**:
    - Replace `UIButtonFactory.createButton` with a `TextButton` using the `"market-swap"` style:
      ```kotlin
      val confirmBtn = TextButton("COMMENCE OPERATIONS", skin, "market-swap").apply {
          addListener(object : ClickListener() {
              override fun clicked(event: InputEvent?, x: Float, y: Float) {
                  onConfirm()
              }
          })
      }
      ```
3.  **Cleanup**:
    - Remove `import com.srw.seedsofsovereignty.ui.UIButtonFactory`.
    - Add required imports: `com.badlogic.gdx.scenes.scene2d.InputEvent` and `com.badlogic.gdx.scenes.scene2d.utils.ClickListener`.

## VERIFICATION
- `EvolutionReportPopup` uses `"panel"` background.
- The `confirmBtn` is a `TextButton` using the `"market-swap"` style.
- `UIButtonFactory` is no longer used in this file.
