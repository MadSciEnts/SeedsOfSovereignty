# HUD ARCHITECTURE: THE DIEGETIC ORCHESTRATOR (v2.0.0)

## 1. THE AR ENGINE (GOLD STANDARD)
- **Zero-Allocation:** No new objects created during render loop.
- **Space-Agnostic:** Elements exist in SCREEN or WORLD space with inter-space tethers.
- **Origin Mandate:** The AR Layer MUST utilize a **Bottom-Left (0,0)** origin. All center-logic must use `(uiWidth/2, uiHeight/2)`.

## 2. INSTRUMENT CLASSIFICATION & STYLING
- **STATUS_LIGHT (Z: 10):**
    - `CORE_YELLOW`: Primary diagnostic status. Color: `Color.YELLOW`.
- **TACTICAL_STICK (Z: 5):**
    - `VANGUARD`: Player weapon/aim. Color: `Color.CYAN`.
    - `SWARM`: Enemy weapon/aim. Color: `Color.LIME`.
- **DATA_READOUT (Z: 2):** Numerical telemetry.

## 3. VITALS & INTEGRITY (THE ARC STANDARD)
- **Visual Model:** Concentric Arcs implemented via `drawer.arc`.
- **Widget Source:** `DiagnosticWidget.kt`.
- **Layer Standards:**
    - **HULL:** White (Max) and Red (Current) arcs.
    - **SHIELD:** Cyan (Current) and Blue (Max/Backing) arcs.
- **Rationale:** Circular arcs represent "Bubble Integrity" and provide high-density data visualization in the Top-Left corner without occupying central screen space.

## 4. RADAR & SCANNING STANDARDS
- **Physical Position:** Top-Right corner. Anchor: `(uiWidth - 160, uiHeight - 160)`.
- **Sweep Logic:** Single precise line originating from center. Sweep speed: `140 deg/s`.
- **Pips:** Hostile: `Color.RED`, Neutral/Friendly: `Color.CYAN`. Radius: 120 units.

## 5. TACTICAL COMMAND SPECIFICATION (NEW)
- **TACTICAL_TOUCHPAD:**
    - **Behavior:** Persistent input (sticky position). Does NOT return to origin on release.
    - **Purpose:** Vector2 positional data for loiter points or attack vectors.
- **JOYSTICK:**
    - **Behavior:** Spring-to-origin. Returns to (0,0) on release.
    - **Purpose:** Vector2 + Magnitude for active steering or aiming.
- **CONTROL TYPES:**
    - **TRIGGER_BUTTON:** Single action signal.
    - **TOGGLE_BUTTON:** Bi-stable switch mechanism.
    - **DATA_INFO:** Dynamic string display from module.
    - **SLIDER:** Bi-directional float (requested vs achieved).
    - **DIAMOND_GRID:** 5-way digital control (U, D, L, R, C).

## 6. MODULE CONTROL CONFIGURATIONS
- **PRIMARY_WPN:** [Joystick]
- **SECONDARY_WPN:** [Trigger Button]
- **TERTIARY_WPN:** [Tactical Touchpad] + [Trigger Button]
- **DRONE_BAY:** [Tactical Touchpad] + [Trigger Button: Launch] + [Toggle: Mode]
- **POWER_CORE:** [Vertical Slider: WPN] + [Vertical Slider: SHD] + [Vertical Slider: ENG] + [Vertical Slider: UTL]
- **SHIELD_GEN:** [Diamond Grid: Fore/Aft/Port/Stern/Equal]
- **FIGHTER_BAY:** [Tactical Touchpad] + [Trigger Button: Launch] + [Slider: Range]

## 7. COMMAND MATRIX LAYOUT STANDARDS
- **Vertical Stack:**
    - **TOP:** Instance Shelf (Specific ship modules).
    - **MIDDLE:** Tactical Control Area (Populated by selected module's schema).
    - **BOTTOM:** Category Shelf (WPN, DRN, NAV, SYS, etc.).
- **Growth Logic:**
    - Controls populate **Right-to-Left** from the bottom-right screen anchor.
    - Category buttons populate along the screen bottom, expanding left.
- **Dimensions:** Tactical Area has a fixed vertical height matching the standard Joystick/Touchpad UI.
