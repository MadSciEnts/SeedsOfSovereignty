# SURGICAL IMPLEMENTATION PLAN (v0.8.0)

## MISSION: Phase 13.1 - HUD Anchor & Signal Binding
**Status:** [ 2 / 2 : COMPLETE ]

## TASK 1: UPDATE HUDFactory REFRESH LOGIC
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/HUDFactory.kt`
**Status:** [ COMPLETE ]

### 1.1 Implementation
Add a case for `DiagnosticWidget` in the `refreshActor` function to ensure it is handled during UI style swaps, even if it doesn't require specific property updates currently.

```kotlin
    private fun refreshActor(actor: Actor, skin: Skin) {
        when (actor) {
            // ... (Around line 125)
            is DiagnosticWidget -> {
                // Procedural widget - no style-specific children to refresh yet.
            }
            // ...
        }
    }
```

## TASK 2: REFACTOR VITALS CLUSTER WIDGET
**File:** `core/src/main/kotlin/com/srw/seedsofsovereignty/ui/VitalsClusterWidget.kt`
**Status:** [ COMPLETE ]

### 2.1 Implementation
Update the `VitalsClusterWidget` to use the `SignalObserver` pattern for its bars, moving away from direct ECS polling in the `update` method where possible, or at least synchronizing with the new signal architecture.

```kotlin
class VitalsClusterWidget(
    private val drawer: ShapeDrawer,
    private val skin: Skin,
    private val world: World
) : Table(skin), com.srw.seedsofsovereignty.component.SignalObserver {

    // ...

    override fun onSignalReceived(signal: com.srw.seedsofsovereignty.component.StatusSignal) {
        // Handle signals like "SYSTEMS_INTEGRITY" to update specific bars
    }
}
```

*Note: I will perform a surgical edit to add the interface and a basic implementation of `onSignalReceived`.*

---
**CRITICAL:** DO NOT remove or alter any existing architectural comments or annotations. Perform surgical edits around them.
