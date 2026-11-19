# Changelog

All notable changes to this project are documented in this file. Versions are listed newest first.

# 2026.3-1.1.0-beta

## New Content

- Forge generator support for `forge-1.16.5`, `forge-1.17.1`, `forge-1.18.2`, `forge-1.19.2`, `forge-1.19.4`,
  `forge-1.20.1`, `forge-1.20.4`, `forge-1.21.1`, `forge-26.1.2` and `forge-26.3`. These generators use the native
  Forge configuration API and register their specs through `ModLoadingContext`.
- NeoForge generator support for `neoforge-1.20.1`, `neoforge-1.20.4`, `neoforge-1.20.6`, `neoforge-1.21.1`,
  `neoforge-1.21.4`, `neoforge-1.21.8`, `neoforge-26.1.2`, `neoforge-26.2` and `neoforge-26.3`. The 1.20.1 generator
  uses the native Forge compatible API; the other versions use Forge Config API Port.
- Fabric generator support for `fabric-26.2` in addition to `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`,
  `fabric-1.21.11`, `fabric-26.1.2` and `fabric-26.3`.
- The ModAPI definition carries compatibility markers for every native Forge generator and for NeoForge 1.20.1, so a
  workspace can switch between native and port based generators without a missing API error.

## Changes

- The `config` element type is covered by the Forge, NeoForge and Fabric generator flavors instead of the Fabric
  flavor only.
- The configuration editor reports the missing `forgeconfigapiport` external API on Fabric and port based NeoForge
  workspaces, but not on native Forge or NeoForge 1.20.1 workspaces, where the loader supplies the configuration API.
- Forge 26.x initializers import `net.minecraftforge.eventbus.api.listener.SubscribeEvent`; Forge 1.16.5 to 1.21.1
  keep the older `net.minecraftforge.eventbus.api.SubscribeEvent` import.
- Fabric initializers import the generated configuration package explicitly and NeoForge 1.20.4/1.20.6 keep their
  dispense behavior event bus correction.
- Version number, plugin descriptor, README, changelog and the documented generator matrix are aligned to
  `2026.3-1.1.0-beta`.

## Fixes

Fixed 2 bug(s).

- The Fabric configuration initializer referenced the generated configuration class without importing the
  `configuration` package. The explicit wildcard import keeps the generated file compilable even when MCreator's import
  formatter cannot resolve the generated class yet.
- The NeoForge 1.20.4 and 1.20.6 `DispenseBehaviors` initializer is corrected to subscribe on the mod event bus, which
  prevents the client crash reported with the bus-less annotation.

## Known Limitations

- Forge 26.x still exposes `ModLoadingContext`, but Forge marks it deprecated for removal. The native templates use it
  so one initializer shape works from 1.16.5 through 26.3.
- Third party generator plugins for Minecraft versions that MCreator does not bundle are not distributed with this
  plugin and have to be installed separately.

# 1.0.6-beta

## Changes

- Documentation adds a migration section for workspaces that were created with the original plugin, including the
  required generator switch before the external API can be enabled.

## Fixes

Fixed 1 bug(s).

- The configuration procedure blocks no longer declare `forgeconfigapiport` as required API. MCreator converts blockly
  error notes into element validation for procedures, so a procedure that used those blocks became invalid while the
  external API was disabled, which is unavoidable before a workspace is switched from a non Fabric generator to a
  Fabric one. The configuration editor blocks keep the annotation, where it only appears as a compile note and does not
  affect element validation.

# 1.0.5-beta

## Changes

- The configuration blocks and the configuration procedure blocks declare `forgeconfigapiport` as required external
  API, so the Blockly editor reports an `api_required` error note while the API is disabled in the workspace settings.
- Documentation states that the external API has to be enabled per workspace in **Workspace settings → External APIs**
  and lists the matching compile errors in the troubleshooting section.

# 1.0.4-beta

## Fixes

Fixed 1 bug(s).

- Workspace synchronization failed with `Could not find maven.modrinth:forge-config-api-port:...` for
  `fabric-1.21.11`, `fabric-26.1.2` and `fabric-26.3`, because the coordinate was derived from the jar file name
  instead of the published version. The definition selects the Fabric file by its published version number or its
  unique Modrinth version id now.

# 1.0.3-beta

## Fixes

Fixed 1 bug(s).

- Workspace synchronization failed for the unobfuscated Minecraft versions with `Could not find method
  modImplementation()`. The port is declared with `implementation` for `fabric-26.1.2` and `fabric-26.3`, whose non
  remapping Fabric Loom has no mod configuration; the remapping generators keep `modImplementation`.

# 1.0.2-beta

## Fixes

Fixed 1 bug(s).

- The development client crashed with `NoClassDefFoundError: com/electronwill/nightconfig/core/UnmodifiableConfig`
  while Forge Config API Port initialized, because the Night Config artifacts were compile only and missing from the
  development run classpath. They are regular dependencies now, so the artifacts are part of the compile classpath and
  of the development run classpath; the packaged mod still receives the library from the port itself.

# 1.0.1-beta

## Fixes

Fixed 1 bug(s).

- Generated mods failed to compile with `cannot access UnmodifiableConfigWrapper`, because `ForgeConfigSpec` extends
  Night Config types that the port only ships as a jar-in-jar, which never reaches the compile classpath of a
  consumer. The `forgeconfigapiport` ModAPI definition adds the matching Night Config `core` and `toml` artifacts as
  compile only dependencies for every generator except `fabric-1.20.1`, where the port jar contains the library.

# 1.0.0-beta

## New Content

- Fabric port of the Configuration Files mod element (`config`) backed by Forge Config API Port. Element name, Blockly
  editor, block ids, field names, data list, mapping, language keys and template variables match the original plugin
  byte for byte, so existing workspaces can switch without data migration.
- Generated configuration classes build a `net.minecraftforge.common.ForgeConfigSpec` with
  `ForgeConfigSpec.Builder`, declare one `ForgeConfigSpec.ConfigValue` per configured variable and build the spec in a
  static initializer. A generated `<JavaModName>Configs` mod initializer registers every spec through the registry of
  the port that belongs to the selected generator.
- Generator definitions, configuration block templates, procedure templates, mappings and registry templates for
  `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2` and `fabric-26.3`.
- `forgeconfigapiport` ModAPI definition that adds the port dependency, the `forgeconfigapiport` runtime declaration
  with its version range and the automatic `<JavaModName>Configs` entrypoint injection into the generated
  `fabric.mod.json` for workspaces that contain configuration elements.

## Changes

- The configuration element type is covered by the Fabric generator flavor only.
- The plugin uses the id `forge-config-api` and the Java package `com.litqa.forgeconfigapiport`; workspace data,
  element names, Blockly definitions and generated code are not affected by this identity.
- Plugin sources, comments and documentation are written in English and follow the documentation standard of this
  project.

## Known Limitations

- No configuration screen is registered. Forge Config API Port exposes `ConfigScreenFactoryRegistry` for that, which is
  not wired up in this release; values are edited in the generated `.toml` file.
- The external API is enabled per workspace. Once enabled, the ModAPI definition applies even to workspaces without
  configuration elements, so those mods also declare Forge Config API Port as a runtime dependency.
- Configuration file names are written verbatim (`"<file>.toml"`). Characters that are invalid in a file name are
  rejected by the operating system, not by the plugin.
- The Fabric 1.20.1 mod metadata template of MCreator does not emit dynamically required mods. That generator receives
  the Gradle dependency but no `depends` entry for the port in `fabric.mod.json`.
- The port coordinates are pinned to published releases in `apis/forgeconfigapiport.yaml`; updating a generator to a
  newer port version requires editing that definition.
- The first build of a workspace resolves the port from the Modrinth Maven repository and therefore needs network
  access.
