# Migration Report

## Scope

This branch replaces the previous `plugin/` development copy. It targets MCreator 2026.3 EAP and the following
generators:

| Loader | Generators |
| --- | --- |
| Forge | `forge-1.12.2`, `forge-1.20.1`, `forge-1.20.4`, `forge-1.21.1`, `forge-26.3` |
| NeoForge | `neoforge-1.21.1`, `neoforge-1.21.4`, `neoforge-1.21.8`, `neoforge-26.1.2`, `neoforge-26.2`, `neoforge-26.3` |
| Fabric | `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2`, `fabric-26.2`, `fabric-26.3` |

The source is self-contained under `plugin-branch-2026.3/`. The previous `plugin/` directory is deprecated and is not
part of this migration.

## Native configuration APIs

Forge and NeoForge no longer use Forge Config API Port as a prerequisite.

* Forge 1.20.1/1.20.4/1.21.1/26.3 use `net.minecraftforge.common.ForgeConfigSpec` and register through
  `ModLoadingContext#registerConfig`.
* NeoForge 1.21.1/1.21.4/1.21.8/26.1.2/26.2 use `net.neoforged.neoforge.common.ModConfigSpec` and register through
  `ModContainer#registerConfig` with `COMMON`, `SERVER` and `CLIENT`.
* NeoForge 26.3 uses the same `ModConfigSpec` API with `LOCAL`, `SYNCED` and `CLIENT`.
* Forge 1.12.2 uses the legacy `net.minecraftforge.common.config.Configuration` API, `Property` fields and one
  `config/<file>.cfg` file per element, loaded from the generated base mod `preInit` lifecycle method.

The `apis/forgeconfigapiport.yaml` file now contains only empty compatibility markers. It does not add Gradle
dependencies or expose the Fabric implementation through the workspace external API list.

## Fabric dependency injection

Fabric workspaces still use Forge Config API Port. The plugin owns a marked block in the generated `build.gradle`:

* The block adds the Modrinth Maven repository, `mavenCentral()` and the matching `modImplementation` or
  `implementation` dependency for the active Fabric generator.
* Fabric 1.21.1 and newer also receive the matching Night Config `core` and `toml` artifacts.
* The block is rewritten at the start of every Gradle build and is removed when the workspace is not a Fabric workspace
  or has no configuration elements.
* The legacy `forgeconfigapiport` entry is removed from `WorkspaceSettings#getMCreatorDependenciesRaw()` when a
  workspace is loaded or built.

No external API has to be enabled manually. Existing workspaces that enabled the old API are cleaned automatically.

## Removed generator resources

The following generator resource directories were removed because they are outside the new support scope:

* Forge: `forge-1.16.5`, `forge-1.17.1`, `forge-1.18.2`, `forge-1.19.2`, `forge-1.19.4`, `forge-26.1.2`.
* NeoForge: `neoforge-1.20.1`, `neoforge-1.20.4`, `neoforge-1.20.6`.

`forge-1.12.2` was added with its own legacy configuration templates.

## Compatibility

* The `config` mod element type, Blockly editor, block ids, field names, data list and language keys are unchanged.
  Existing workspaces do not require a data migration.
* The generated class names and package layout are unchanged.
* The plugin package version is `1.1.1.2-beta` and the archive name is `forge-config-api-2026.3.zip`.
* The Gradle wrapper uses the public Gradle 9.8.0 distribution URL with distribution URL validation enabled.
* The plugin descriptor still accepts MCreator `202600344621` and `2026003`.

## Verification

* Plugin Java sources compile with Java 25 and `-Xlint:all` without errors or warnings.
* The plugin resource validator reports no failures.
* The API definition contains eighteen compatibility markers and no real Gradle dependency blocks.
* All eighteen generator template sets render and compile against local API stubs, including the Forge 1.12.2 legacy
  API and the NeoForge 26.3 `LOCAL`/`SYNCED` registration types.
* The Gradle package build and archive inspection are part of the release check.

## Known limitations

* Forge 1.12.2 has no `COMMON`/`SERVER`/`CLIENT` split. The configuration type is ignored and each element uses its own
  `.cfg` file.
* No configuration screen is registered.
* Fabric 1.20.1 mod metadata does not emit a dynamic `depends` entry for Forge Config API Port.
* Fabric workspaces need network access on the first build for the Modrinth Maven dependency.
* Third-party generator plugins are required for generators that MCreator 2026.3 does not bundle.
* Generated workspace builds and in-game behavior must be checked per installed generator plugin.
