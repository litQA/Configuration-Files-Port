# Configuration Files (Forge Config API Port)

A MCreator plugin that ports the **Configuration Files** mod element by NerdyPuzzle to the Fabric generators, backed by
[Forge Config API Port](https://github.com/Fuzss/forgeconfigapiport). Generated configuration classes use
`net.minecraftforge.common.ForgeConfigSpec` and are registered through the config registry of the port.

| | |
| --- | --- |
| Plugin id | `forge-config-api` |
| Java | 25 |
| Plugin Java package | `com.litqa.forgeconfigapiport` |
| Supported Fabric generators | `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2`, `fabric-26.3` |
| License | CC BY-NC 4.0; original element definitions MIT (NerdyPuzzle) |

## Features

* Adds the `config` mod element type ("Configuration") with the original Blockly editor, block ids, field names, data
  list, mapping and language keys. Workspaces created with the original plugin open, edit and regenerate without data
  migration.
* Generates one `ForgeConfigSpec` based configuration class per element and registers every spec at mod
  initialization, so values are loaded and usable through the regular `ConfigValue#get` API.
* Adds a `forgeconfigapiport` ModAPI definition that wires the port, its runtime dependency, its version range and the
  Night Config artifacts into Fabric workspaces.
* Injects the generated configuration initializer into `fabric.mod.json` only for workspaces that contain
  configuration elements.

## Requirements

| Component | Version |
| --- | --- |
| MCreator | EAP 2026.3.44621 (`202600344621`), plugin descriptor also accepts `2026003` |
| Java | 25 |
| Gradle | 9.8.0 through the bundled wrapper, resolved from the local distribution archive |
| Forge Config API Port | resolved automatically per generator, see the dependency matrix |

The generated mod requires Forge Config API Port at runtime and Fabric API, which the Fabric generators already depend
on.

## Installation

1. Build the plugin (see [Building](#building)) or use the packaged `forge-config-api-port.zip`.
2. In MCreator open **Workspace settings → Plugins → Manage plugins → Load plugin from file** and select the package,
   or copy it into the MCreator `plugins` folder.
3. Restart MCreator, select one of the supported Fabric generators for the workspace and create a
   **Configuration** mod element.

## Usage

The element has three parts:

| Field | Purpose |
| --- | --- |
| Configuration file name | Written verbatim as `<name>.toml` in the `config` directory. Required, must not be empty. |
| Configuration type | `COMMON`, `SERVER` or `CLIENT`, mapped to the matching `ModConfig.Type` of the port. |
| Configuration editor | Blockly document with categories and variables. |

Variables are declared inside a **Category** block with one of these variable blocks:

| Block | Generated Java type | Default value field |
| --- | --- | --- |
| Logic variable | `Boolean` | `true` / `false` drop down |
| Number variable | `Double` | number field |
| Text variable | `String` | text field |
| Item registry name variable | `String` (registry name of the selected item) | item selector |
| Text list variable | `List<? extends String>` | repeating text entries |

The category block opens a config section (`BUILDER.push(...)` / `BUILDER.pop()`), the variable blocks define a
`ForgeConfigSpec.ConfigValue` with the configured default and optional comment. The procedure blocks **Get logic
variable …**, **Get number variable …**, **Get text variable …** and **For loop over text list …** read the values at
runtime.

## Generated code

Every configuration element generates two files.

`<ElementName>Configuration` in the `configuration` package:

```java
package net.mcreator.testp.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

public class TestConfigConfiguration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;
	public static final ForgeConfigSpec.ConfigValue<Double> TEST_CONFIG_NUM;
	static {
		BUILDER.push("set_1");
		TEST_CONFIG_NUM = BUILDER.comment("sample comment").define("testconfignum", (double) 1);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
```

`<JavaModName>Configs` in the `init` package, generated once per workspace and registered as an additional `main`
entrypoint:

```java
package net.mcreator.testp.init;

import net.neoforged.fml.config.ModConfig;

import net.mcreator.testp.configuration.TestConfigConfiguration;

import net.fabricmc.api.ModInitializer;

import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;

public class TestpModConfigs implements ModInitializer {
	@Override
	public void onInitialize() {
		ConfigRegistry.INSTANCE.register("testp", ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "testmod.toml");
	}
}
```

The registry class differs per generator, because Forge Config API Port renamed its API over time:

| Generator | Registry used by the generated initializer | `ModConfig.Type` package |
| --- | --- | --- |
| `fabric-1.20.1` | `fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry` | `net.minecraftforge.fml.config` |
| `fabric-1.21.1` | `fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry` | `net.neoforged.fml.config` |
| `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2`, `fabric-26.3` | `fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry` | `net.neoforged.fml.config` |

`fabric.mod.json` receives `<JavaModName>Configs` as first `main` entrypoint only when the workspace contains at least
one configuration element. Fabric Loader loads the port before the mod because the ModAPI definition also adds
`forgeconfigapiport` to `depends`, so the registry is initialized when the generated initializer runs.

## Dependency matrix

The `apis/forgeconfigapiport.yaml` definition adds these dependencies to the workspace. The Modrinth coordinate is the
release that the generated code is verified against, and the Night Config artifacts are needed because
`ForgeConfigSpec` extends Night Config types while the port ships that library as a jar-in-jar.

| Generator | Loom | Port dependency | Modrinth coordinate | Night Config |
| --- | --- | --- | --- | --- |
| `fabric-1.20.1` | remapping | `modImplementation` | `v8.0.3-1.20.1-Fabric` | bundled in the port jar |
| `fabric-1.21.1` | remapping | `modImplementation` | `v21.1.6-1.21.1-Fabric` | `core`/`toml` 3.8.0 |
| `fabric-1.21.8` | remapping | `modImplementation` | `v21.8.2-1.21.8-Fabric` | `core`/`toml` 3.8.3 |
| `fabric-1.21.11` | remapping | `modImplementation` | `uXrWPsCu` (21.11.1) | `core`/`toml` 3.8.4 |
| `fabric-26.1.2` | non remapping | `implementation` | `jUe0ucoE` (26.1.5) | `core`/`toml` 3.8.3 |
| `fabric-26.3` | non remapping | `implementation` | `JpKvrr9J` (26.3.1) | `core`/`toml` 3.8.4 |

Minecraft 26.1 and later are unobfuscated and their generator uses the non remapping Fabric Loom without any mod
configuration, so the port is a plain `implementation` dependency there. The Night Config artifacts are never bundled
into the mod jar; an installed mod receives them from the port itself.

## Name compatibility

The following names are part of the compatibility contract and are identical to the original plugin:

* Mod element type `config`, readable name keys `modelement.config`, `modelement.config.name`,
  `modelement.config.description`.
* GUI language keys `elementgui.config.*`, help contexts `config/name` and `config/type`, theme icons
  `themes/*/images/mod_types/config.png`.
* Blockly editor type `config`, start block `config_start`, toolbox category `configdata`, block ids
  `config_category`, `config_variable`, `config_logic_variable`, `config_number_variable`, `config_text_variable`,
  `config_registryname_variable`, `config_textlist_variable`, procedure blocks `text_variable`, `logic_variable`,
  `number_variable`, `stringlist_iterator`, `stringlist_variable_foreach` and the mutator `text_list_mutator`.
* Element fields `pools`, `file`, `configType`, `config`, `defining` and the legacy `Pool` / `Pool.Entry` fields.
* Block and template variables `data.defining`, `field$name`, `field$value`, `field$comment`, `field$config`,
  `field$variable`, `field_list$entry`, `input$value`, `statement$write`, `statement$foreach`, `variables`, `code` and
  `configs`.
* Data list `config`, mapping `config`, generated class names `<Name>Configuration` and the
  `@modid.configuration.*` localization keys.

## Building

```text
cd plugin
gradlew build
```

The build needs a Java 25 JDK and uses the Gradle 9.8.0 distribution from `Reference resources` through the wrapper.
The plugin package is written to `plugin/build/libs/forge-config-api-port.zip`.

Set `GRADLE_USER_HOME` to a workspace local directory when the build must not touch the user profile, for example
`GRADLE_USER_HOME=<workspace>/caches/gradle`.

Gradle 9.8 validates that the project directory is writable. In restricted shells where Java reports a writable
directory as read only, run the build from a regular terminal session.

## Verification

The following checks were performed on this project:

* `javac --release 25 -Xlint:all` against the MCreator EAP class files: 0 errors, 0 warnings.
* Resource validation of all 113 JSON, YAML and FreeMarker files of the plugin package: 0 failures.
* `gradlew build`: `BUILD SUCCESSFUL`, package with 203 entries, `plugin.json` at the archive root, no compile time
  jars and no sources included.
* The generated configuration pattern was compiled against the Forge Config API Port jars for 1.20.1, 1.21.1, 1.21.8,
  26.1.5 and 26.3.1: 0 errors, including the generic `ConfigValue<List<? extends String>>` list value.
* The `fabric.mod.json` entrypoint injection was exercised against the built package: entrypoint added, existing
  entrypoints preserved, repeated generation idempotent.
* All plugin classes were loaded against the MCreator EAP class files to verify the referenced MCreator API.

Workspace level build and in game verification of the generated configuration is performed per generator on the
target installation; the 1.21.8 workspace compiled and reached client launch during the beta cycle.

## Troubleshooting

| Symptom | Cause | Resolution |
| --- | --- | --- |
| `cannot access UnmodifiableConfigWrapper` while compiling | Night Config is not on the compile classpath | Plugin `1.0.2-beta` or newer, then regenerate `mcreator.gradle` |
| `NoClassDefFoundError: com/electronwill/nightconfig/...` on client start | Night Config is missing from the development run classpath | Plugin `1.0.2-beta` or newer |
| `Could not find method modImplementation()` on a 26.x workspace | Old definition used the remapping Loom configuration | Plugin `1.0.3-beta` or newer |
| `Could not find maven.modrinth:forge-config-api-port:...` | Coordinate does not exist in the Modrinth Maven repository | Plugin `1.0.4-beta` or newer |
| An updated plugin does not change `mcreator.gradle` | MCreator keeps the generated script until the workspace is saved or opened again | Close and reopen the workspace, or delete `mcreator.gradle` and let MCreator regenerate it |
| The expected `.toml` file is not created | `SERVER` configs are loaded on server start, `CLIENT` configs only on a client | Check the configuration type and start the matching side |

## Known limitations

* No configuration screen is registered. The original plugin opened the NeoForge `ConfigurationScreen`; Forge Config
  API Port exposes `ConfigScreenFactoryRegistry` for that, which is not wired up yet. Values are edited in the
  generated `.toml` file while the game is not running.
* The ModAPI definition applies to every workspace that uses a supported Fabric generator, even when it contains no
  configuration element, so such mods also declare Forge Config API Port as a runtime dependency.
* Configuration file names are written verbatim (`"<file>.toml"`). Characters that are invalid in a file name are
  rejected by the operating system, not by the plugin.
* The Fabric 1.20.1 mod metadata template does not emit dynamically required mods. That generator receives the Gradle
  dependency but no `depends` entry for the port in `fabric.mod.json`.
* The port coordinates are pinned to published releases. Updating a generator to a newer port release requires editing
  `apis/forgeconfigapiport.yaml`.
* The first build of a workspace resolves the port from the Modrinth Maven repository and therefore needs network
  access.

## Repository layout

| Path | Content |
| --- | --- |
| `src/main/java` | Plugin sources: launcher, mod element type, element and editor |
| `src/main/resources` | Plugin descriptor, Blockly definitions, procedures, language files, help, themes, ModAPI definition and the templates of all supported Fabric generators |
| `libs` | Compile time only MCreator API and Log4j API jars |
| `gradle`, `gradlew`, `gradlew.bat` | Gradle wrapper pinned to the local 9.8.0 distribution |
| `build/libs` | Built plugin package |

`libs/mcreator-api.jar` contains the MCreator EAP class files and is used exclusively to compile this plugin. It is not
packaged into `forge-config-api-port.zip`; MCreator provides these classes at runtime.

## DSH Empowers Accelerated Plugin Development

<img width="1000" alt="dsh" src="https://github.com/user-attachments/assets/1b0b22c9-5213-48eb-9e51-6de70e852dec" />

This project includes code generated with **DSH (AI)** assistance.

## License and attribution

The plugin is licensed under CC BY-NC 4.0, see `LICENSE`. The element definition, Blockly definitions, user interface
and language files are derived from the Configuration Files plugin by NerdyPuzzle, which is distributed under the MIT
license; the original notice is reproduced in `THIRD-PARTY-NOTICES.md`. Forge Config API Port is a separate mod by Fuzs
under MPL-2.0 and is not redistributed with this plugin.

# MCreator - Minecraft Mod Maker

[![MCreator](https://mcreator.net/image/brand/mcreator300s.png)](https://mcreator.net/)

MCreator is an open-source software used to make Minecraft Java Edition mods, Minecraft Bedrock Edition Add-Ons, resource packs, and data packs using an intuitive easy-to-learn interface or with an integrated code editor. It is used worldwide by Minecraft players, aspiring mod developers, for education, online classes, and STEM workshops. 

NOT AN OFFICIAL MINECRAFT PRODUCT. NOT APPROVED BY OR ASSOCIATED WITH MOJANG OR MICROSOFT.
