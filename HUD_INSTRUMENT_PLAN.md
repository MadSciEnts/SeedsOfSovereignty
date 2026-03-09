# HUD INSTRUMENT ARCHITECTURE (v0.4.0)

## 1. Goal
To implement a robust, bidirectional HUD system where hardware modules define their controls using a classified "Chained Position" model with integrated feedback signals.

## 2. Control Classifications
Every control belongs to a known classification that defines its base behavior, footprint, and feedback capabilities.

| Classification | Behavior | Feedback Support |
| :--- | :--- | :--- |
| **ACTION_BUTTON** | Momentary | Label (Quantity), Color (Status) |
| **TACTICAL_STICK** | Analog/Persistent | Distance (Range), Color (Lock) |
| **MODIFIER_SLIDER**| Linear Range | Dual-Bar (Requested vs Actual) |
| **STATUS_LIGHT** | Indicator Only | Color, Blink-Rate |

## 3. Chained Geometry & Return Signals
- **The Chain:** Modules define a list of `InputChannels`. Position `relX, relY` is relative to the **previous item** in the list.
- **The Signal:** Each channel defines an optional `signalKey`. The HUD will "pull" state from the `TacticalHubComponent` matching this key to update labels/colors.

## 4. Visual DNA
The `InputChannel` DNA includes:
- `classification`: (Enum)
- `style`: (Texture Collection ID)
- `geometry`: (relX, relY, width, height)
- `meta`: (Default Label, Default Color)

## 5. Implementation Roadmap
1. **Phase 6.1:** Refactor `TacticalSchema.kt` to the Bidirectional Chained model.
2. **Phase 6.2:** Create `HUDTextureGenerator` for procedural style sets.
3. **Phase 6.3:** Implement `TacticalHubComponent` (The Bidirectional Hub).
4. **Phase 6.4:** Update `TacticalClusterWidget` to plot geometry and bridge signals.
