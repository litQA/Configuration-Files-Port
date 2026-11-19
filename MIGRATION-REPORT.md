# Migration Report

## Scope

This report covers the migration of the Configuration Files mod element to MCreator 2026.3 and the changes needed to
support the Forge, NeoForge and Fabric generators of that release.

## Workspace Migration

1. Remove or disable the original `configuration_files.zip` plugin and any older `forge-config-api` package. Both the
   original plugin and this plugin register the `config` element type and the `config` Blockly editor; MCreator cannot
   load both at the same time.
2. Install `forge-config-api-port.zip` version `2026.3-1.1.0-beta` or newer and restart MCreator.
3. Switch the workspace to the target generator before editing the external API selection. The element is only offered
   on generator versions that contain the matching `config.definition.yaml`.
4. On Fabric and on NeoForge generators other than 1.20.1, enable **Workspace settings → External APIs → Forge Config
   API Port**. Native Forge and NeoForge 1.20.1 workspaces do not need the external API because the loader supplies the
   configuration API.
5. Run **Regenerate code** and rebuild the workspace. Template changes are only applied while MCreator generates code;
   an existing build directory keeps the old generated classes otherwise.

Existing `Pools` based workspaces are read through `Config.listsToXML()` and saved in the current Blockly format on the
next edit. Element names, block ids, field names, data list entries, mappings and generated class names are unchanged.

## Architecture Differences

| Platform group | Configuration API | Spec registration |
| --- | --- | --- |
| Forge 1.16.5–26.3, NeoForge 1.20.1 | native `net.minecraftforge.common.ForgeConfigSpec` | `ModLoadingContext.get().registerConfig(...)` |
| NeoForge 1.20.4–1.21.4 | Forge Config API Port forge v4 registry | `ForgeConfigRegistry.INSTANCE.register(...)` |
| NeoForge 1.21.8, 26.1.2, 26.2, 26.3 | Forge Config API Port v5 registry | `ForgeConfigRegistry.INSTANCE.register(...)` |
| Fabric 1.20.1 | Forge Config API Port v2 registry | `ForgeConfigRegistry.INSTANCE.register(...)` |
| Fabric 1.21.1 | Forge Config API Port forge v4 registry | `ForgeConfigRegistry.INSTANCE.register(...)` |
| Fabric 1.21.8, 1.21.11, 26.1.2, 26.2, 26.3 | Forge Config API Port v5 registry | `ConfigRegistry.INSTANCE.register(...)` |

The Forge 26.x event bus moved `SubscribeEvent` to `net.minecraftforge.eventbus.api.listener`. Forge 1.16.5–1.21.1
keeps the older `net.minecraftforge.eventbus.api.SubscribeEvent` import. NeoForge 26.x keeps
`net.neoforged.bus.api.SubscribeEvent`.

## Known Limitations

- **No configuration screen.** The original plugin opened the NeoForge `ConfigurationScreen`. Forge Config API Port
  exposes `ConfigScreenFactoryRegistry`, but this plugin does not register a screen yet; values are edited in the
  generated `.toml` file while the game is not running.
- **Port API is enabled per workspace.** Once `forgeconfigapiport` is enabled, the ModAPI definition applies to the
  whole workspace, including mods without configuration elements. The generated mod metadata may declare the port as a
  runtime dependency in that case.
- **Native Forge compatibility marker.** Forge 1.16.5–26.3 and NeoForge 1.20.1 use an empty ModAPI implementation. If
  the marker remains enabled after a generator switch, Forge metadata may contain an optional `forgeconfigapiport`
  entry. It does not make the port a required Forge dependency and does not change the build.
- **Forge 26.x registration API.** Forge 26.x still exposes `ModLoadingContext`, but it is marked deprecated for
  removal. The native Forge initializer uses `ModLoadingContext.get().registerConfig(...)` so the same template works
  from 1.16.5 through 26.3. A future Forge release that removes the method requires switching to the constructor
  context form.
- **Third party generators.** MCreator 2026.3 does not bundle every generator listed in the support matrix. The Forge
  generators, NeoForge 1.20.1/1.20.4/1.20.6/1.21.4/26.1.2/26.2 and Fabric 26.2 are supplied by separate generator
  plugins by their respective authors; they are not distributed with this plugin.
- **Fabric 1.20.1 metadata.** The Fabric 1.20.1 mod metadata template does not emit dynamically required mods. The
  workspace receives the Gradle dependency but no `depends` entry for the port in `fabric.mod.json`.
- **Network on first build.** The port based workspaces resolve Forge Config API Port from the Modrinth Maven repository
  on the first build, so that build needs network access.
- **Pinned coordinates.** The port coordinates in `apis/forgeconfigapiport.yaml` are pinned to published releases.
  Updating a generator to a newer port release requires editing the definition.
- **No in-game verification for every generator.** The plugin sources, resources and rendered templates are verified
  locally; the generated workspace build and in-game launch have to be checked per installed generator plugin on the
  target MCreator installation.
