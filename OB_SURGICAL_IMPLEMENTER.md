# MISSION: Phase 7.3 - Step 3: Router Refactor

## TASK
Refactor `HUDTextureGenerator.kt` to act as a factory router, delegating work to the new style classes.

## ACTIONS

### 1. Update HUDTextureGenerator.kt
- **Path:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDTextureGenerator.kt`
- **Content:**
    - Keep the `TextureCollection` data class.
    - Delete all color constants (`DARK_GREY`, `HAZARD_ORANGE`, etc.).
    - Delete the `applyNoise` function.
    - Delete `generateIndustrial` and `generateImperial` methods.
    - Refactor the `generate` method to use the new style classes:
      ```kotlin
      fun generate(styleId: String, skin: Skin): TextureCollection {
          val generator: TacticalStyleGenerator = when (styleId.uppercase()) {
              "INDUSTRIAL" -> IndustrialStyle()
              "IMPERIAL" -> ImperialStyle()
              else -> IndustrialStyle()
          }
          return generator.generate(skin)
      }
      ```
    - Ensure all necessary imports are maintained or added.

## VERIFICATION
- `HUDTextureGenerator.kt` is simplified.
- No duplicate logic remains in `HUDTextureGenerator.kt`.
- The `generate` method correctly delegates to `IndustrialStyle` and `ImperialStyle`.
