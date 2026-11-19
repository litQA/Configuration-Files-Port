<img width="1000"  alt="forge_config_api_port banner" src="https://github.com/user-attachments/assets/64cfb54d-4d36-49ee-9e9d-ab2f49224669" />

# Configuration Files (Forge Config API Port)

A MCreator plugin that ports the **Configuration Files** mod element by NerdyPuzzle to the Forge, NeoForge and Fabric
generators of MCreator 2026.3. The generated configuration classes use `net.minecraftforge.common.ForgeConfigSpec`:
Forge and NeoForge 1.20.1 use the native configuration API of the loader, every other NeoForge generator and every
Fabric generator uses [Forge Config API Port](https://github.com/Fuzss/forgeconfigapiport).

| | |
| --- | --- |
| Plugin id | `forge-config-api` |
| Plugin version | `2026.3-1.1.0-beta` |
| Java | 25 |
| Plugin Java package | `com.litqa.forgeconfigapiport` |
| Supported Forge generators | `forge-1.16.5`, `forge-1.17.1`, `forge-1.18.2`, `forge-1.19.2`, `forge-1.19.4`, `forge-1.20.1`, `forge-1.20.4`, `forge-1.21.1`, `forge-26.1.2`, `forge-26.3` |
| Supported NeoForge generators | `neoforge-1.20.1`, `neoforge-1.20.4`, `neoforge-1.20.6`, `neoforge-1.21.1`, `neoforge-1.21.4`, `neoforge-1.21.8`, `neoforge-26.1.2`, `neoforge-26.2`, `neoforge-26.3` |
| Supported Fabric generators | `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2`, `fabric-26.2`, `fabric-26.3` |
| License | CC BY-NC 4.0; original element definitions MIT (NerdyPuzzle) |

## Features

* Adds the `config` mod element type ("Configuration") with the original Blockly editor, block ids, field names, data
  list, mapping and language keys. Workspaces created with the original plugin open, edit and regenerate without data
  migration.
* Generates one `ForgeConfigSpec` based configuration class per element and registers every spec at mod
  initialization, so values are loaded and usable through the regular `ConfigValue#get` API.
* Adds a `forgeconfigapiport` ModAPI definition that wires the port, its runtime dependency, its version range and the
  Night Config artifacts into the supported workspaces. Forge and NeoForge 1.20.1 use an empty compatibility marker
  because their configuration API is part of the loader.
* Injects the generated configuration initializer into `fabric.mod.json` only for Fabric workspaces that contain
  configuration elements. NeoForge and Forge discover the generated initializer through their event bus.

## Requirements

| Component | Version |
| --- | --- |
| MCreator | EAP 2026.3.44621 (`202600344621`), plugin descriptor also accepts `2026003` |
| Java | 25 |
| Gradle | 9.8.0 through the bundled wrapper, resolved from the local distribution archive |
| Forge Config API Port | resolved automatically per generator, see the dependency matrix; not needed on native Forge and NeoForge 1.20.1 |

The generated mod requires Forge Config API Port at runtime on the port based generators. Fabric workspaces also need
Fabric API, which the Fabric generators already depend on.

## Installation

1. Build the plugin (see [Building](#building)) or use the packaged `forge-config-api-port.zip`.
2. In MCreator open **Workspace settings → Plugins → Manage plugins → Load plugin from file** and select the package,
   or copy it into the MCreator `plugins` folder.
3. Restart MCreator, remove the original `configuration_files.zip` plugin if it is installed (both plugins register the
   same element and Blockly editor names and must not coexist), select a supported generator and create a
   **Configuration** mod element.
4. For Fabric and NeoForge generators other than 1.20.1, enable **Workspace settings → External APIs → Forge Config API
   Port**.

## Usage

The element has three parts:

| Field | Purpose |
| --- | --- |
| Configuration file name | Written verbatim as `<name>.toml` in the `config` directory. Required, must not be empty. |
| Configuration type | `COMMON`, `SERVER` or `CLIENT`, mapped to the matching `ModConfig.Type` of the loader or port. |
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

import java.util.List;

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

`<JavaModName>Configs` in the `init` package, generated once per workspace. Native Forge and NeoForge 1.20.1 register
through `ModLoadingContext`:

```java
package net.mcreator.testp.init;

import net.mcreator.testp.configuration.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = "testp", bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestpModConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "test.toml");
		});
	}
}
```

The port based NeoForge generators register through the port and split common/client specs into dist aware nested
event subscribers:

```java
package net.mcreator.testp.init;

import net.mcreator.testp.configuration.*;
import fuzs.forgeconfigapiport.neoforge.api.v5.ForgeConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

public class TestpModConfigs {
	@EventBusSubscriber
	public static class CommonRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				ForgeConfigRegistry.INSTANCE.register("testp", ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "test.toml");
			});
		}
	}
}
```

Fabric generators generate a `ModInitializer` and the plugin injects `<JavaModName>Configs` into the `main`
entrypoints of `fabric.mod.json`:

```java
package net.mcreator.testp.init;

import net.mcreator.testp.configuration.*;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public class TestpModConfigs implements ModInitializer {
	@Override
	public void onInitialize() {
		ConfigRegistry.INSTANCE.register("testp", ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "test.toml");
	}
}
```

The registry class differs per generator, because Forge Config API Port renamed its API over time:

| Generator | Registry used by the generated initializer | `ModConfig.Type` package |
| --- | --- | --- |
| `forge-1.16.5` … `forge-26.3`, `neoforge-1.20.1` | native `ModLoadingContext` | `net.minecraftforge.fml.config` |
| `neoforge-1.20.4`, `neoforge-1.20.6`, `neoforge-1.21.1`, `neoforge-1.21.4` | `fuzs.forgeconfigapiport.neoforge.api.forge.v4.ForgeConfigRegistry` | `net.neoforged.fml.config` |
| `neoforge-1.21.8`, `neoforge-26.1.2`, `neoforge-26.2`, `neoforge-26.3` | `fuzs.forgeconfigapiport.neoforge.api.v5.ForgeConfigRegistry` | `net.neoforged.fml.config` |
| `fabric-1.20.1` | `fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry` | `net.minecraftforge.fml.config` |
| `fabric-1.21.1` | `fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry` | `net.neoforged.fml.config` |
| `fabric-1.21.8`, `fabric-1.21.11`, `fabric-26.1.2`, `fabric-26.2`, `fabric-26.3` | `fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry` | `net.neoforged.fml.config` |

Forge 26.x uses `net.minecraftforge.eventbus.api.listener.SubscribeEvent`. Forge 1.16.5–1.21.1 uses
`net.minecraftforge.eventbus.api.SubscribeEvent`. NeoForge 26.x keeps `net.neoforged.bus.api.SubscribeEvent`.

`fabric.mod.json` receives `<JavaModName>Configs` as first `main` entrypoint only when the workspace contains at least
one configuration element. Fabric Loader loads the port before the mod because the ModAPI definition also adds
`forgeconfigapiport` to `depends`, so the registry is initialized when the generated initializer runs.

## Dependency matrix

The `apis/forgeconfigapiport.yaml` definition adds these dependencies to the workspace. The Modrinth coordinate is the
release that the generated code is verified against, and the Night Config artifacts are needed because
`ForgeConfigSpec` extends Night Config types while the port ships that library as a jar-in-jar.

| Generator | Port dependency | Modrinth coordinate | Night Config |
| --- | --- | --- | --- |
| `forge-1.16.5` … `forge-26.3` | none, native Forge | — | supplied by Forge |
| `neoforge-1.20.1` | none, native Forge compatible | — | supplied by NeoForge |
| `neoforge-1.20.4` | `implementation` | `v20.4.3-1.20.4-NeoForge` | supplied by NeoForge |
| `neoforge-1.20.6` | `implementation` | `v20.6.1-1.20.6-NeoForge` | supplied by NeoForge |
| `neoforge-1.21.1` | `implementation` | `v21.1.6-1.21.1-NeoForge` | supplied by NeoForge |
| `neoforge-1.21.4` | `implementation` | `v21.4.3-1.21.4-NeoForge` | supplied by NeoForge |
| `neoforge-1.21.8` | `implementation` | `v21.8.2-1.21.8-NeoForge` | supplied by NeoForge |
| `neoforge-26.1.2` | `implementation` | `gtX4TxQk` (26.1.5) | supplied by NeoForge |
| `neoforge-26.2` | `implementation` | `DZCpByDO` (26.2.1) | supplied by NeoForge |
| `neoforge-26.3` | `implementation` | `XmQjDLuW` (26.3.1) | supplied by NeoForge |
| `fabric-1.20.1` | `modImplementation` | `v8.0.3-1.20.1-Fabric` | bundled in the port jar |
| `fabric-1.21.1` | `modImplementation` | `v21.1.6-1.21.1-Fabric` | `core`/`toml` 3.8.0 |
| `fabric-1.21.8` | `modImplementation` | `v21.8.2-1.21.8-Fabric` | `core`/`toml` 3.8.3 |
| `fabric-1.21.11` | `modImplementation` | `uXrWPsCu` (21.11.1) | `core`/`toml` 3.8.4 |
| `fabric-26.1.2` | `implementation` | `jUe0ucoE` (26.1.5) | `core`/`toml` 3.8.3 |
| `fabric-26.2` | `implementation` | `rSd3GiG8` (26.2.1) | `core`/`toml` 3.8.4 |
| `fabric-26.3` | `implementation` | `JpKvrr9J` (26.3.1) | `core`/`toml` 3.8.4 |

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
* Resource validation of all JSON, YAML and FreeMarker files of the plugin package: 0 failures.
* The 26 generator template sets are rendered and compiled against synthetic loader/port APIs: 0 errors, 0 warnings.
* `gradlew build`: `BUILD SUCCESSFUL`.
* The generated configuration pattern is also checked against the published Forge Config API Port registry signatures
  for the v2, forge v4 and v5 APIs.
* The `fabric.mod.json` entrypoint injection is exercised against the built package: entrypoint added, existing
  entrypoints preserved, repeated generation idempotent.
* All plugin classes are loaded against the MCreator EAP class files to verify the referenced MCreator API.

Workspace level build and in game verification of the generated configuration is performed per generator on the
target installation. Third party generator plugins are not bundled with this plugin; install the generator plugin for
the Minecraft version first.

## Troubleshooting

| Symptom | Cause | Resolution |
| --- | --- | --- |
| `cannot access UnmodifiableConfigWrapper` while compiling | Night Config is not on the compile classpath | Plugin `1.1.0-beta` or newer, then regenerate `mcreator.gradle` |
| `NoClassDefFoundError: com/electronwill/nightconfig/...` on client start | Night Config is missing from the development run classpath | Plugin `1.1.0-beta` or newer |
| `Could not find method modImplementation()` on a 26.x Fabric workspace | Old definition used the remapping Loom configuration | Plugin `1.1.0-beta` or newer |
| `Could not find maven.modrinth:forge-config-api-port:...` | Coordinate does not exist in the Modrinth Maven repository | Plugin `1.1.0-beta` or newer |
| Fabric/NeoForge editor reports `api_required` | The `forgeconfigapiport` external API is disabled | Enable it in **Workspace settings → External APIs** |
| An updated plugin does not change `mcreator.gradle` | MCreator keeps the generated script until the workspace is saved or opened again | Close and reopen the workspace, or delete `mcreator.gradle` and let MCreator regenerate it |
| The expected `.toml` file is not created | `SERVER` configs are loaded on server start, `CLIENT` configs only on a client | Check the configuration type and start the matching side |
| `IModBusEvent ... common NeoForge bus` on NeoForge 1.20.4/1.20.6 | Old generator template annotates `DispenseBehaviors` without the mod bus | Plugin `1.1.0-beta` or newer, then regenerate the workspace |
| `UnsupportedClassVersionError` | A package for another MCreator version is installed | Use the `2026.3-1.1.0-beta` package built with Java 25 |
| Workspace switching reports a missing `forgeconfigapiport` API | Native Forge/NeoForge 1.20.1 compatibility marker missing | Enable the API again for the target generator and restart MCreator |

## Known limitations

* No configuration screen is registered. The original plugin opened the NeoForge `ConfigurationScreen`; Forge Config
  API Port exposes `ConfigScreenFactoryRegistry` for that, which is not wired up yet. Values are edited in the
  generated `.toml` file while the game is not running.
* The ModAPI definition applies to every workspace that uses a supported port based generator, even when it contains
  no configuration element, so such mods also declare Forge Config API Port as a runtime dependency.
* Configuration file names are written verbatim (`"<file>.toml"`). Characters that are invalid in a file name are
  rejected by the operating system, not by the plugin.
* The Fabric 1.20.1 mod metadata template does not emit dynamically required mods. That generator receives the Gradle
  dependency but no `depends` entry for the port in `fabric.mod.json`.
* The port coordinates are pinned to published releases. Updating a generator to a newer port release requires editing
  `apis/forgeconfigapiport.yaml`.
* The first build of a port based workspace resolves the port from the Modrinth Maven repository and therefore needs
  network access.
* Forge 26.x still exposes `ModLoadingContext`, but it is marked deprecated for removal by Forge. The native Forge
  templates use it so the same initializer works from 1.16.5 through 26.3; if Forge removes it, the templates have to
  move to the constructor context form.
* The third party generator plugins for versions that MCreator does not bundle are not distributed with this plugin.

## Repository layout

| Path | Content |
| --- | --- |
| `src/main/java` | Plugin sources: launcher, mod element type, element and editor |
| `src/main/resources` | Plugin descriptor, Blockly definitions, procedures, language files, help, themes, ModAPI definition and the templates of all 26 supported generators |
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
