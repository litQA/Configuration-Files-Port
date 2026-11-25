# Configuration Files (Forge Config API Port)

A MCreator 2026.3 plugin that ports the **Configuration Files** mod element by NerdyPuzzle to the Forge, NeoForge and
Fabric generators of MCreator 2026.3.

Forge and NeoForge workspaces use the native configuration API of their loader. Fabric workspaces use
[Forge Config API Port](https://github.com/Fuzss/forgeconfigapiport), which the plugin adds to the generated Gradle
build automatically. No external API has to be enabled in the workspace settings.

| | |
| --- | --- |
| Plugin id | `forge-config-api` |
| Plugin version | `1.1.1.2-beta` |
| MCreator | 2026.3 EAP (`202600344621`; the descriptor also accepts `2026003`) |
| Java | 25 |
| Plugin package | `com.litqa.forgeconfigapiport` |
| License | CC BY-NC 4.0; original element definitions MIT (NerdyPuzzle) |

## Supported generators

| Loader | Generators | Configuration API | Generated format |
| --- | --- | --- | --- |
| Forge | `forge-1.12.2` | `net.minecraftforge.common.config.Configuration` | legacy `.cfg` |
| Forge | `forge-1.20.1`, `forge-1.20.4`, `forge-1.21.1`, `forge-26.3` | `net.minecraftforge.common.ForgeConfigSpec` | `.toml` |
| NeoForge | `neoforge-1.21.1`, `neoforge-1.21.4`, `neoforge-1.21.8`, `neoforge-26.1.2`, `neoforge-26.2` | `net.neoforged.neoforge.common.ModConfigSpec` with `COMMON` / `SERVER` / `CLIENT` | `.toml` |
| NeoForge | `neoforge-26.3` | `ModConfigSpec` with `LOCAL` / `SYNCED` / `CLIENT` | `.toml` |
| Fabric | `fabric-1.20.1`, `fabric-1.21.1`, `fabric-1.21.8`, `fabric-1.21.11` | Forge Config API Port v2/v4 | `.toml` |
| Fabric | `fabric-26.1.2`, `fabric-26.2`, `fabric-26.3` | Forge Config API Port v5 | `.toml` |

The Forge 1.12.2 generator is a legacy generator: every configuration element is loaded from its own
`config/<file>.cfg` from the generated base mod `preInit` lifecycle method. The old Forge configuration system has no
equivalent of `COMMON`, `SERVER` or `CLIENT`, so the selected configuration type is ignored on that generator.

## Features

* Adds the `config` mod element type ("Configuration") with the original Blockly editor, block ids, field names, data
  list, mapping and language keys. Workspaces created with the original plugin open, edit and regenerate without data
  migration.
* Generates one loader-native configuration class per element:
  * Forge 1.12.2: `net.minecraftforge.common.config.Configuration` and `Property`.
  * Forge 1.20.1 and newer: `ForgeConfigSpec`, registered through `ModLoadingContext`.
  * NeoForge 1.21.1 and newer: `ModConfigSpec`, registered through `ModContainer#registerConfig`.
* Keeps the Fabric Forge Config API Port dependency in a managed block in the generated `build.gradle`. The block is
  added only when the workspace is a Fabric workspace with at least one configuration element, and it is removed when
  the workspace switches to a native Forge or NeoForge generator.
* Injects the generated Fabric configuration initializer into `fabric.mod.json` only for Fabric workspaces with
  configuration elements. Forge 1.20.1 and newer and NeoForge discover the generated initializer through their event
  bus. Forge 1.12.2 calls the initializer from the generated base mod `preInit` method, with a tick fallback for
  locked base mod files.
* Removes the legacy `forgeconfigapiport` workspace API id that older releases wrote into
  `WorkspaceSettings#getMCreatorDependenciesRaw()`.
* Ships empty ModAPI compatibility markers for every supported generator. They keep older workspaces openable and do
  not add a Gradle dependency.

## Installation

1. Build the plugin (see [Building](#building)) or use the packaged `forge-config-api-2026.3.zip`.
2. In MCreator open **Workspace settings > Plugins > Manage plugins > Load plugin from file** and select the package,
   or copy it into the MCreator `plugins` folder.
3. Restart MCreator and remove the original `configuration_files.zip` plugin if it is installed. Both plugins register
   the same element and Blockly editor names and must not coexist.
4. Select a supported generator and create a **Configuration** mod element. Do not enable Forge Config API Port in
   the workspace external API list; the plugin manages the Fabric dependency itself.

## Usage

| Field | Purpose |
| --- | --- |
| Configuration file name | Written verbatim as `<name>.toml` (or `<name>.cfg` on Forge 1.12.2) in the `config` directory. Required, must not be empty. |
| Configuration type | `COMMON`, `SERVER` or `CLIENT`, mapped to the matching `ModConfig.Type` of the loader. Ignored on Forge 1.12.2. |
| Configuration editor | Blockly document with categories and variables. |

Variables are declared inside a **Category** block with one of these variable blocks:

| Block | Generated Java type | Default value field |
| --- | --- | --- |
| Logic variable | `Boolean` | `true` / `false` drop down |
| Number variable | `Double` | number field |
| Text variable | `String` | text field |
| Item registry name variable | `String` (registry name of the selected item) | item selector |
| Text list variable | `List<? extends String>` | repeating text entries |

The category block opens a config section. The variable blocks define a loader-native config value with the configured
default and optional comment. The procedure blocks **Get logic variable...**, **Get number variable...**, **Get text
variable...** and **For loop over text list...** read the values at runtime.

## Generated code

Every configuration element generates a `<ElementName>Configuration` class. The workspace also gets one
`<JavaModName>Configs` initializer.

Forge 1.12.2:

```java
public class TestConfigConfiguration {
    public static Configuration CONFIG;
    public static Property TEST_CONFIG_NUM;

    public static void init(File file) {
        CONFIG = new Configuration(file);
        CONFIG.load();
        // generated variable definitions
        CONFIG.save();
    }
}
```

Forge 1.20.1 and newer:

```java
public class TestConfigConfiguration {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> TEST_CONFIG_NUM;

    static {
        TEST_CONFIG_NUM = BUILDER.comment("sample comment").define("testconfignum", (double) 1);
        SPEC = BUILDER.build();
    }
}
```

NeoForge 1.21.1 to 26.2:

```java
container.registerConfig(ModConfig.Type.COMMON, TestConfigConfiguration.SPEC, "test.toml");
```

NeoForge 26.3:

```java
container.registerConfig(ModConfig.Type.LOCAL, TestConfigConfiguration.SPEC, "test.toml");
```

Fabric generators use the same shape with `ForgeConfigSpec` and register through the Forge Config API Port registry
that is selected by the generator template.

## Managed Fabric dependency block

At the start of every Gradle build the plugin rewrites a marked block in the workspace `build.gradle`:

```groovy
// >>> forge_config_api managed block >>>
repositories {
    mavenCentral()
    maven {
        name = 'Modrinth'
        url = 'https://api.modrinth.com/maven'
    }
}

dependencies {
    modImplementation 'maven.modrinth:forge-config-api-port:v8.0.3-1.20.1-Fabric'
}
// <<< forge_config_api managed block <<<
```

The block is idempotent. It is removed for non-Fabric workspaces and for Fabric workspaces without configuration
elements. Fabric 1.21.1 and newer also receive the matching Night Config `core` and `toml` artifacts.

## Building

```text
gradlew build
```

The package is written to `build/libs/forge-config-api-2026.3.zip`. The wrapper uses the public Gradle 9.8.0
distribution URL by default. A local Gradle distribution may be used as a fallback when the standard distribution is
not available in the build environment.

The plugin sources are compiled with Java 25 and target MCreator EAP 2026.3. The plugin archive contains the compiled
plugin classes, `plugin.json`, templates, Blockly definitions, mappings, language files and themes at the archive root.
It does not contain `libs/` or test sources.

## Compatibility notes

* The mod element, field names, template names and data list are identical to the Configuration Files plugin this
  project ports. Existing workspaces do not need a data migration.
* Forge and NeoForge no longer depend on Forge Config API Port. Their ModAPI entries are empty compatibility markers
  only.
* Fabric dependency injection is not exposed through the workspace external API list.
* MCreator 2026.3 does not bundle every generator in the support matrix. Install the matching third-party generator
  plugin before opening a workspace for that generator.

## Known limitations

* **No configuration screen.** The original plugin opened the NeoForge `ConfigurationScreen`. This plugin does not
  register a screen; values are edited in the generated file while the game is not running.
* **Forge 1.12.2 ignores the configuration type.** The legacy `Configuration` API has no common/server/client split.
  Every element gets its own `.cfg` file loaded during pre-initialization.
* **No in-game verification for every generator.** The plugin sources, resources and rendered templates are verified
  locally. The generated workspace build and in-game launch must be checked per installed generator plugin on the
  target MCreator installation.
* **Fabric 1.20.1 metadata.** The Fabric 1.20.1 mod metadata template does not emit dynamically required mods. The
  workspace receives the Gradle dependency but no `depends` entry for the port in `fabric.mod.json`.
* **Network on first build.** Fabric workspaces resolve Forge Config API Port from the Modrinth Maven repository on the
  first build, so that build needs network access.
* **Pinned Fabric coordinates.** The port version for each Fabric generator is pinned in `Launcher.java`. Updating a
  generator to a newer port release requires a plugin source change.
* **Legacy Forge 1.12.2 API.** The generator uses the old `net.minecraftforge.common.config.Configuration` API. It
  does not support TOML, config screens or the modern `ModConfig` lifecycle.
* **Locked Forge 1.12.2 base mod files.** If the generated base mod class is locked, the `preInit` call is not
  injected. The configuration class then initializes on the first client or server tick instead.

## Credits

The mod element definition, Blockly blocks, user interface and language files are derived from the Configuration Files
plugin by NerdyPuzzle (MIT). See `THIRD-PARTY-NOTICES.md` for the full notices.
