# Changelog

All notable changes to this project are documented in this file. Versions are listed newest first.

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
