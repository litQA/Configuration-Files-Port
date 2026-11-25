# Changelog

## 1.1.1.2-beta

### Fixes

* Fixed the Forge 1.12.2 configuration initializer. It is now called from the generated base mod `preInit` method
  instead of subscribing to the non-EventBus `FMLPreInitializationEvent`. The generated configuration class also
  initializes on the first client or server tick when the base mod file is locked.

### Known Limitations

* Locked Forge 1.12.2 base mod files prevent the `preInit` call and defer configuration loading to the first tick.

## 1.1.1.1-beta

### New Content

* Added native configuration generation for Forge `forge-1.20.1`, `forge-1.20.4`, `forge-1.21.1` and `forge-26.3`.
* Added native configuration generation for NeoForge `neoforge-1.21.1`, `neoforge-1.21.4`, `neoforge-1.21.8`,
  `neoforge-26.1.2`, `neoforge-26.2` and `neoforge-26.3`.
* Added the legacy Forge `forge-1.12.2` generator with `net.minecraftforge.common.config.Configuration` templates,
  `Property` fields and one `.cfg` file per configuration element.
* Added a managed Fabric dependency block in the generated `build.gradle` for Forge Config API Port and the matching
  Night Config artifacts.
* Added migration cleanup for the legacy `forgeconfigapiport` workspace API id.

### Changes

* Removed the unsupported Forge `1.16.5`, `1.17.1`, `1.18.2`, `1.19.2`, `1.19.4` and `26.1.2` generator resources.
* Removed the unsupported NeoForge `1.20.1`, `1.20.4` and `1.20.6` generator resources.
* Changed the supported version set to Forge 1.12.2/1.20.1/1.20.4/1.21.1/26.3, NeoForge 1.21.1/1.21.4/1.21.8/26.1.2/
  26.2/26.3 and Fabric 1.20.1/1.21.1/1.21.8/1.21.11/26.1.2/26.2/26.3.
* Changed all Forge and NeoForge generator templates to loader-native configuration APIs. Forge Config API Port is now
  used only by Fabric generators.
* Changed NeoForge 26.3 registration to `ModConfig.Type.LOCAL` for common configurations and `ModConfig.Type.SYNCED`
  for server configurations. NeoForge 1.21.1 to 26.2 keep `COMMON` and `SERVER`.
* Changed the ModAPI definition to eighteen empty compatibility markers. It no longer injects Gradle dependencies.
* Changed the plugin package version to `1.1.1.1-beta` and the package name to `forge-config-api-2026.3.zip`.
* Changed the Gradle wrapper to the public Gradle 9.8.0 distribution URL with distribution URL validation enabled.
* Removed the configuration editor API-required compile note. Fabric dependencies are managed automatically and Forge
  and NeoForge do not use the port.

### Fixes

* Fixed the workspace event listener generator flavor lookup to use
  `GeneratorConfiguration#getGeneratorFlavor()`.
* Fixed the Forge 1.12.2 configuration templates to pass defaults and comments through the four-argument
  `Configuration#get` overloads.
* Fixed Fabric template verification so text lists use the `List.of` form emitted by the Fabric templates.

### Known Limitations

* Forge 1.12.2 uses the legacy `.cfg` format and ignores the `COMMON`, `SERVER` and `CLIENT` configuration types.
* No configuration screen is registered. Values are edited in the generated file while the game is not running.
* Fabric 1.20.1 mod metadata does not emit a dynamic `depends` entry for Forge Config API Port.
* Fabric workspaces need network access on the first build to resolve Forge Config API Port from Modrinth Maven.
* MCreator 2026.3 does not bundle every generator in the support matrix. Third-party generator plugins are required for
  the missing generators.
* Generated workspace builds and in-game behavior have to be checked per installed generator plugin on the target
  MCreator installation.
