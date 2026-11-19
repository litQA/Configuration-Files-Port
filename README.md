# Configuration Files (Forge / NeoForge / Fabric)

A MCreator 2026.1 plugin that ports the **Configuration Files** mod element by NerdyPuzzle to thirteen generators:

- Native Forge: `forge-1.16.5`, `forge-1.17.1`, `forge-1.18.2`, `forge-1.19.2`, `forge-1.19.4`, `forge-1.20.1`
- Native NeoForge: `neoforge-1.20.1`, `neoforge-1.20.4`, `neoforge-1.20.6`, `neoforge-1.21.1`, `neoforge-1.21.4`,
  `neoforge-1.21.8`
- Fabric: `fabric-1.21.8`, using Forge Config API Port

Native Forge generators use `net.minecraftforge.common.ForgeConfigSpec`. NeoForge 1.20.1 keeps the legacy
`ForgeConfigSpec` API because its MCreator generator is based on the Forge 1.20.1 generator. NeoForge 1.20.4 and newer
use the built-in `net.neoforged.neoforge.common.ModConfigSpec`; they no longer need the Port. Only Fabric 1.21.8 still
uses [Forge Config API Port](https://github.com/Fuzss/forgeconfigapiport).

| | |
| --- | --- |
| Plugin id | `forge-config-api` |
| Plugin version | `2026.1-2.2.1-beta` |
| MCreator | 2026.1 (`2026001`) |
| Java | 21 |
| Plugin Java package | `com.litqa.forgeconfigapiport` |
| Supported generators | 6 native Forge, 6 native NeoForge, 1 Fabric Port |
| License | CC BY-NC 4.0; original element definitions MIT (NerdyPuzzle) |

## Features

* Adds the `config` mod element type ("Configuration") with the original Blockly editor, block ids, field names, data
  list, mapping and language keys. Workspaces created with the original plugin open, edit and regenerate without data
  migration.
* Generates one `ForgeConfigSpec` or `ModConfigSpec` configuration class per element and registers every spec at mod
  initialization, so values are loaded and usable through the regular `ConfigValue#get` API.
* Native Forge and native NeoForge generators need no external API and no Port dependency. Fabric uses the
  `forgeconfigapiport` ModAPI definition.
* Injects the generated configuration initializer into `fabric.mod.json` only for Fabric workspaces that contain
  configuration elements. Forge and NeoForge workspaces discover the generated initializer through an event bus
  annotation.
* Provides no-op `forgeconfigapiport` API implementations for the six Forge and six NeoForge generators. These
  compatibility markers let a workspace that already has the Port API enabled switch to a native generator without
  MCreator reporting a missing API. They add no Gradle dependency and no generated code.
* Reports an `api_required` compile note in the configuration editor only when a Fabric workspace has the
  `forgeconfigapiport` API disabled. Forge and NeoForge workspaces never show this note.
* Fixes the MCreator 2026.1 NeoForge 1.20.4/1.20.6 dispense behavior template, which otherwise registers an
  `FMLCommonSetupEvent` handler on the common bus and crashes during client start.

## Requirements

| Component | Version |
| --- | --- |
| MCreator | 2026.1 (`2026001`) |
| Java (MCreator) | 21 |
| Gradle | 9.8.0 through the bundled wrapper, resolved from the local distribution archive |
| Forge | built into the six native Forge generators |
| NeoForge | built into the six native NeoForge generators |
| Forge Config API Port | required only for Fabric 1.21.8 |

MCreator's external API (Java plugin support) must be enabled to load this plugin. If it is disabled, MCreator reports
that the Java plugin cannot be loaded; the plugin package does not start in that state.

The external `forgeconfigapiport` API is only needed for Fabric. Native Forge workspaces use the Forge configuration
API and native NeoForge workspaces use the NeoForge configuration API; neither needs the Port at runtime.

## Installation

**Replacement plugin:** This package cannot be enabled together with the original `configuration_files` plugin. Both
plugins register the same `config` Blockly editor and the same `config` mod element type, and MCreator stops during
plugin initialization when both are present. MCreator 2026.1 ships `configuration_files.zip` in its `plugins` folder;
remove or disable that package, and any older `forge-config-api` package, before installing this one.

1. Build the plugin (see [Building](#building)) or use the packaged `forge-config-api-port.zip`.
2. Remove or disable `configuration_files.zip` and any older `forge-config-api` package from the MCreator `plugins`
   folder.
3. In MCreator open **Workspace settings -> Plugins -> Manage plugins -> Load plugin from file** and select this
   package, or copy it into the MCreator `plugins` folder.
4. Restart MCreator, select one of the thirteen supported generators for the workspace and create a **Configuration**
   mod element.

## Usage

The element has three parts:

| Field | Purpose |
| --- | --- |
| Configuration file name | Written verbatim as `<name>.toml` in the `config` directory. Required, must not be empty. |
| Configuration type | `COMMON`, `SERVER` or `CLIENT`, mapped to the matching `ModConfig.Type` of the target platform. |
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
`ForgeConfigSpec.ConfigValue` or `ModConfigSpec.ConfigValue` with the configured default and optional comment. The
procedure blocks **Get logic variable ...**, **Get number variable ...**, **Get text variable ...** and **For loop
over text list ...** read the values at runtime.

The procedure blocks use the configuration variable's identifier name, not its display name. For example, a variable
with the display name `Warden upgrade template drop chance` and the identifier
`warden_upgrade_template_drop_chance` must be referenced as `warden_upgrade_template_drop_chance`; spaces in the
identifier field produce invalid Java code.

## Generated code

Every configuration element generates two files.

`<ElementName>Configuration` in the `configuration` package. Native Forge and NeoForge 1.20.1 use `ForgeConfigSpec`:

```java
package net.mcreator.testp.configuration;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
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

NeoForge 1.20.4 and newer use `ModConfigSpec`:

```java
package net.mcreator.testp.configuration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class TestConfigConfiguration {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final ModConfigSpec.ConfigValue<Double> TEST_CONFIG_NUM;

	static {
		BUILDER.push("set_1");
		TEST_CONFIG_NUM = BUILDER.comment("sample comment").define("testconfignum", (double) 1);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
```

The item registry name variable resolves the selected item through `ForgeRegistries.ITEMS` on Forge and through
`BuiltInRegistries.ITEM` on NeoForge. The text list variable uses `Arrays.asList` on Forge 1.16.5, which targets Java 8,
and `List.of` on the other generators.

`<JavaModName>Configs` in the `init` package is generated once per workspace.

Native Forge sample:

```java
package net.mcreator.testp.init;

import net.mcreator.testp.configuration.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = "testp", bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestpConfigs {
	@SubscribeEvent
	public static void register(FMLConstructModEvent event) {
		event.enqueueWork(() -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TestConfigConfiguration.SPEC,
					"testmod.toml");
		});
	}
}
```

Native NeoForge sample for 1.20.4:

```java
package net.mcreator.testp.init;

import net.mcreator.testp.configuration.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

public class TestpConfigs {
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class CommonRegistry {
		@SubscribeEvent
		public static void register(FMLConstructModEvent event) {
			event.enqueueWork(() -> {
				ModContainer container = ModList.get().getModContainerById("testp").get();
				container.registerConfig(ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "testmod.toml");
			});
		}
	}
}
```

NeoForge 1.20.6 uses `@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)`; NeoForge 1.21.1, 1.21.4 and 1.21.8
use a plain `@EventBusSubscriber`. The client registration class additionally uses `Dist.CLIENT`.

Fabric uses `fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry` and a `ModInitializer`. The generated
`<JavaModName>Configs` class is added to the `main` entrypoints of `fabric.mod.json`.

The registration API and event bus annotation differ per platform:

| Generator | Configuration API | Registration |
| --- | --- | --- |
| `forge-1.16.5` ... `forge-1.20.1` | Native `ForgeConfigSpec` | `@Mod.EventBusSubscriber` + `FMLConstructModEvent` + `ModLoadingContext.registerConfig` |
| `neoforge-1.20.1` | Legacy `ForgeConfigSpec` | `@Mod.EventBusSubscriber` + `FMLConstructModEvent` + `ModLoadingContext.registerConfig` |
| `neoforge-1.20.4` | Native `ModConfigSpec` | Nested `@Mod.EventBusSubscriber(bus = Bus.MOD)` + `ModContainer.registerConfig` |
| `neoforge-1.20.6` | Native `ModConfigSpec` | `@EventBusSubscriber(bus = Bus.MOD)` + `ModContainer.registerConfig` |
| `neoforge-1.21.1` | Native `ModConfigSpec` | Plain `@EventBusSubscriber` + `ModContainer.registerConfig` |
| `neoforge-1.21.4` | Native `ModConfigSpec` | Plain `@EventBusSubscriber` + `ModContainer.registerConfig` |
| `neoforge-1.21.8` | Native `ModConfigSpec` | Plain `@EventBusSubscriber` + `ModContainer.registerConfig` |
| `fabric-1.21.8` | Port `fabric.api.v5.ConfigRegistry` | `ModInitializer#onInitialize` |

## Dependency matrix

| Generator | Gradle configuration | Port coordinate | Night Config |
| --- | --- | --- | --- |
| `forge-1.16.5` ... `forge-1.20.1` | none | none (native Forge) | supplied by Forge |
| `neoforge-1.20.1` | none | none (legacy native Forge API) | supplied by NeoForge |
| `neoforge-1.20.4` ... `neoforge-1.21.8` | none | none (native NeoForge) | supplied by NeoForge |
| `fabric-1.21.8` | `modImplementation` | `v21.8.2-1.21.8-Fabric` | `core`/`toml` 3.8.3 |

Fabric uses the remapping Loom, where a mod dependency is declared with `modImplementation`. Forge Config API Port
bundles Night Config as a jar-in-jar on Fabric, which Loom does not expose to the compile classpath; the definition
therefore adds the matching Night Config artifacts as regular dependencies. They are not packaged into the mod jar and
an installed mod receives them from the Port itself.

## Platform conversion and the compatibility markers

The `apis/forgeconfigapiport.yaml` file contains an implementation for every supported generator. The six native Forge
entries and the six native NeoForge entries are deliberately empty:

```yaml
neoforge-1.21.1:
  required_when_enabled: false
  version_range: "[0,)"
  gradle: ""
```

They exist for workspace conversion. MCreator compares the workspace's raw enabled API ids with the APIs implemented
for the target generator before a generator switch. Without a Forge or NeoForge implementation, switching a workspace
that has `forgeconfigapiport` enabled to that platform is blocked with a missing API error.

The empty entries:

* add no Gradle dependency (`gradle` is an empty string, so the generated `mcreator.gradle` stays unchanged);
* generate no Port code and never make Forge or NeoForge load the Port;
* are not required in platform metadata (`required_when_enabled: false`);
* may leave an optional `forgeconfigapiport` entry in an existing `mods.toml` or `neoforge.mods.toml` if the API
  remains enabled after conversion. This optional entry does not require the Port and does not change the build. It is
  accepted as a compatibility marker, not a real dependency.

The configuration editor does not use `required_apis` for the blocks. Instead, `ConfigGUI` adds an `api_required`
compile note only for Fabric workspaces when `forgeconfigapiport` is disabled. Native Forge and NeoForge builds and
editor behavior are identical whether the compatibility marker is enabled or disabled; only the optional metadata
entry described above can differ.

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
cd plugin-branch-2026.1
gradlew build
```

The build needs a Java 21 JDK and uses the Gradle 9.8.0 distribution from `Reference resources` through the wrapper.
The plugin package is written to `plugin-branch-2026.1/build/libs/forge-config-api-port.zip`.

Set `GRADLE_USER_HOME` to a workspace local directory when the build must not touch the user profile, for example
`GRADLE_USER_HOME=<workspace>/caches/gradle`.

Gradle 9.8 validates that the project directory is writable. In restricted shells where Java reports a writable
directory as read only, run the build from a regular terminal session.

## Verification

The following checks are performed on this branch:

* `javac --release 21 -Xlint:all` against the MCreator 2026.1 class files: 0 errors, 0 warnings.
* Resource validation of all JSON, YAML and FreeMarker files of the plugin package, including the new
  `neoforge-1.20.1` generator directory.
* Gradle 9.8.0 build: `BUILD SUCCESSFUL`, `plugin.json` at the archive root, compiled classes targeting Java 21, and no
  compile time jars included.
* Configuration and initializer templates rendered for all thirteen supported generators.
* Native NeoForge configuration and initializer classes checked against the `ModConfigSpec`, `ModContainer`,
  `ModList` and event bus shapes of NeoForge 1.20.4 through 1.21.8.
* Native Forge 1.16.5 and 1.20.1 full initializer and configuration classes compiled against their matching Forge,
  FML, EventBus and Night Config API jars; native Forge 1.17.1 to 1.19.4 configuration classes compiled against their
  matching Forge universal jars.
* NeoForge 1.20.1 templates checked against the legacy Forge API shape of the `neoforge-generator-1.20.1` generator.
* The NeoForge 1.20.4/1.20.6 dispense behavior annotation rewrite was validated against the generated
  `<JavaModName>DispenseBehaviors` class shape that produced the common-bus crash.

Workspace level build and in-game verification of the generated configuration is performed per generator on the
target installation.

## Troubleshooting

| Symptom | Cause | Resolution |
| --- | --- | --- |
| `cannot access UnmodifiableConfigWrapper` while compiling | Night Config is not on the compile classpath | This branch adds the artifacts for Fabric 1.21.8; NeoForge supplies them. Regenerate `mcreator.gradle`. |
| `NoClassDefFoundError: com/electronwill/nightconfig/...` on client start | Night Config is missing from the development run classpath | Use this branch or newer and regenerate `mcreator.gradle`. |
| `cannot find symbol: class ModConfigSpec` on NeoForge | The generator is older than NeoForge 1.20.4 or the wrong NeoForge generator is selected | Select `neoforge-1.20.4` or newer. NeoForge 1.20.1 uses `ForgeConfigSpec`. |
| `Could not find method modImplementation()` on a NeoForge workspace | A Fabric-specific dependency configuration was used | NeoForge needs no Port dependency in this branch; remove the stale dependency and regenerate `mcreator.gradle`. |
| The configuration element is missing from the element list | The selected generator has no `config` definition in this branch | Select one of the thirteen supported generators. |
| An `api_required` note is shown in the configuration editor | The `forgeconfigapiport` API is disabled in a Fabric workspace | Enable it in **Workspace settings -> External APIs**. Native Forge and NeoForge do not show this note. |
| Switching a Port workspace to Forge or NeoForge reports a missing API | The native compatibility marker is missing or the plugin was not reloaded | Use this branch and restart MCreator after installing it. |
| An existing platform metadata file lists `forgeconfigapiport` as optional | The compatibility marker remained enabled after conversion | This is accepted; the entry does not require the Port and does not change the build. Uncheck the API in Workspace settings if desired. |
| NeoForge 1.20.4/1.20.6 client crashes with `IModBusEvent events are not allowed on the common NeoForge bus` | The MCreator generator registers the generated `<JavaModName>DispenseBehaviors` class on the common bus even though it handles `FMLCommonSetupEvent` | Use plugin `2026.1-2.2.1-beta` or newer and regenerate the workspace. The plugin rewrites the generated annotation to the mod bus. |
| An updated plugin does not change `mcreator.gradle` | MCreator keeps the generated script until the workspace is saved or opened again | Close and reopen the workspace, or delete `mcreator.gradle` and let MCreator regenerate it. |

## Known limitations

* No configuration screen is registered. Values are edited in the generated `.toml` file while the game is not
  running.
* NeoForge 1.20.1 uses the legacy `ForgeConfigSpec` API because its MCreator generator is based on Forge 1.20.1. It is
  a native NeoForge configuration and needs no Port, but it does not use `ModConfigSpec`.
* Native Forge 1.16.5 targets Java 8. The native Forge templates therefore use `Arrays.asList` and
  `ForgeRegistries` instead of the newer `List.of` and `BuiltInRegistries` helpers used by the NeoForge generators.
* The six native Forge and six native NeoForge API entries are compatibility markers, not Port dependencies. If they
  remain enabled after a workspace conversion, the platform metadata may contain an optional `forgeconfigapiport`
  entry. It has no build or runtime impact.
* The ModAPI definition applies to every supported workspace that uses a supported generator, even when it contains
  no configuration element.
* Configuration file names are written verbatim (`"<file>.toml"`). Characters that are invalid in a file name are
  rejected by the operating system, not by the plugin.
* The Port coordinate is pinned to the published `21.8.2` Fabric release. Updating the Fabric generator to a newer Port
  release requires editing `apis/forgeconfigapiport.yaml`.
* The six Forge generator plugins and the NeoForge 1.20.4, 1.20.6 and 1.21.4 generator plugins are marked deprecated,
  but remain loadable in MCreator 2026.1.
* The branch does not target Forge 1.14.4, Forge 1.15.2, Forge 1.19.3, Forge 1.20.2, Fabric 1.20.x/1.21.x other than
  1.21.8, or any NeoForge version not listed above.

## Repository layout

| Path | Content |
| --- | --- |
| `src/main/java` | Plugin sources: launcher, mod element type, element and editor |
| `src/main/resources` | Plugin descriptor, Blockly definitions, procedures, language files, help, themes, ModAPI definition and the templates of all thirteen generators |
| `libs` | Compile time only MCreator 2026.1 API and Log4j API jars |
| `gradle`, `gradlew`, `gradlew.bat` | Gradle wrapper pinned to the local 9.8.0 distribution |
| `CHANGELOG.md` | Independent changelog of this branch |
| `build/libs` | Built plugin package |

`libs/mcreator-api.jar` contains the class files of the MCreator 2026.1 runtime and is used exclusively to compile this
plugin. It is not packaged into `forge-config-api-port.zip`; MCreator provides these classes at runtime.

## License and attribution

The plugin is licensed under CC BY-NC 4.0, see `LICENSE`. The element definition, Blockly definitions, user interface
and language files are derived from the Configuration Files plugin by NerdyPuzzle, which is distributed under the MIT
license; the original notice is reproduced in `THIRD-PARTY-NOTICES.md`. Forge Config API Port is a separate mod by Fuzs
under MPL-2.0 and is not redistributed with this plugin.
