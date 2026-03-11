# HUD ARCHITECTURE: THE DIEGETIC ORCHESTRATOR (v1.2.0)

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

## 3. RADAR & SCANNING STANDARDS
- **Physical Position:** Top-Right corner. Anchor: `(uiWidth - 160, uiHeight - 160)`.
- **Sweep Logic:** Single precise line originating from center. Sweep speed: `140 deg/s`.
- **Pips:** Hostile: `Color.RED`, Neutral/Friendly: `Color.CYAN`. Radius: 120 units.

## 4. INTERACTION & FX
- **Tap-to-Target:** PlayScreen routes touches to HUD hitboxes.
- **GLITCH:** Integrity < 20% triggers random localX/Y jitter.
- **ATMOSPHERE:** Subtle 10% opacity scanline overlay with Additive Blending.
