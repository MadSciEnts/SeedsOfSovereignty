# SEED OF SOVEREIGNTY: COMPONENT API CHEATSHEET

Use this file to find exact component properties without searching multiple files.

## 1. Physics & Identity
- **PositionComponent**: `pos: Vector2`
- **VelocityComponent**: `v: Vector2`, `acceleration: Float`, `friction: Float`
- **PlayerComponent**: (Tag)
- **FactionComponent**: `type: FactionType` (PLAYER, ENEMY, NEUTRAL)

## 2. Defense & Health
- **ArcedDefenseComponent**: 
    - `shieldFore`, `shieldAft`, `shieldPort`, `shieldStbd: Float`
    - `hullFore`, `hullAft`, `hullPort`, `hullStbd: Float`
- **PowerCoreComponent**: 
    - `integrity: Float` (0.0 to 1.0)
    - `weaponPower`, `shieldPower`, `enginePower`, `utilityPower: Float`

## 3. Hierarchy & Modules
- **AttachmentComponent**: `parent: Entity`, `offset: Vector2`
- **ModuleComponent**: `category: ModuleCategory`, `cooldown: Float`
- **ModuleChannelsComponent**: `type: ModuleType`, `inputs: Map<String, InputChannelData>`, `outputs: Map<String, OutputChannelData>`
- **SelectedModuleComponent**: (Tag)

## 4. Visuals
- **TextureRegionComponent**: `region: TextureRegion`, `scale: Float`, `rotation: Float`, `color: Color`
- **CompositeVisualComponent**: `layers: MutableList<VisualLayer>`
- **TrailComponent**: `color: Color`, `width: Float`, `pointLifetime: Float`

## 5. Combat
- **WeaponLauncherComponent**: `fireRate: Float`, `barrelOffsets: List<Vector2>`, `barrelIndex: Int`
- **ProjectilePayloadComponent**: `blueprint: ProjectileBlueprint`
- **DamageComponent**: `amount: Float`, `type: DamageType`, `shieldMultiplier: Float`, `impactFX: Map<ImpactTarget, ImpactFXType>`
- **ProjectileComponent**: `lifeTime: Float`
- **ImpactFXComponent**: `type: ImpactFXType`, `lifeTime: Float`, `color: Color`
