package com.litqa.forgeconfigapiport;

import com.litqa.forgeconfigapiport.element.types.PluginElementTypes;
import net.mcreator.blockly.IBlockGenerator;
import net.mcreator.blockly.InternalBlocksLoader;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.ModifyTemplateResultEvent;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.plugin.events.workspace.MCreatorLoadedEvent;
import net.mcreator.plugin.events.workspace.WorkspaceBuildStartedEvent;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.misc.WorkspaceInfo;
import net.mcreator.workspace.settings.WorkspaceSettings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plugin entry point.
 * <p>
 * Registers the configuration Blockly editor and the configuration mod element type, wires the Fabric mod metadata
 * entrypoint and keeps the Fabric Forge Config API Port dependency in a managed {@code build.gradle} block. Forge and
 * NeoForge use the native configuration API of their loader and do not need a Gradle injection.
 */
@SuppressWarnings("this-escape")
public class Launcher extends JavaPlugin {

	private static final Logger LOG = LogManager.getLogger("Configuration Files");
	public static final BlocklyEditorType CONFIG_EDITOR = new BlocklyEditorType("config", "cfg", "config_start");
	/** Registry name of the configuration mod element type, identical to the plugin this port is based on. */
	private static final String CONFIG_ELEMENT_TYPE = "config";
	/** Template of the Fabric mod metadata file. All supported Fabric generators use this template name. */
	private static final String FABRIC_MOD_JSON_TEMPLATE = "modbase/fabric.mod.json.ftl";
	/** Suffix of the generated class that registers every configuration spec of the workspace. */
	private static final String CONFIG_ENTRYPOINT_SUFFIX = "Configs";
	/** Legacy API id written by older releases of this plugin. */
	private static final String LEGACY_API_ID = "forgeconfigapiport";
	/** Markers of the dependency block that this plugin owns in the generated build script. */
	private static final String MANAGED_BLOCK_START = "// >>> forge_config_api managed block >>>";
	private static final String MANAGED_BLOCK_END = "// <<< forge_config_api managed block <<<";
	/** Matches the main entrypoint array of a generated fabric.mod.json. */
	private static final Pattern MAIN_ENTRYPOINT_ARRAY = Pattern.compile("\"main\"\\s*:\\s*\\[");

	/** Fabric Forge Config API Port coordinates and the Gradle configuration that each generator expects. */
	private static final Map<String, FabricDependency> FABRIC_DEPENDENCIES = Map.ofEntries(
			Map.entry("fabric-1.20.1", new FabricDependency("v8.0.3-1.20.1-Fabric", "modImplementation", null)),
			Map.entry("fabric-1.21.1", new FabricDependency("v21.1.6-1.21.1-Fabric", "modImplementation", "3.8.0")),
			Map.entry("fabric-1.21.8", new FabricDependency("v21.8.2-1.21.8-Fabric", "modImplementation", "3.8.3")),
			Map.entry("fabric-1.21.11", new FabricDependency("uXrWPsCu", "modImplementation", "3.8.4")),
			Map.entry("fabric-26.1.2", new FabricDependency("jUe0ucoE", "implementation", "3.8.3")),
			Map.entry("fabric-26.2", new FabricDependency("rSd3GiG8", "implementation", "3.8.4")),
			Map.entry("fabric-26.3", new FabricDependency("JpKvrr9J", "implementation", "3.8.4")));

	public Launcher(Plugin plugin) {
		super(plugin);

		addListener(PreGeneratorsLoadingEvent.class, event -> {
			try {
				// Registers the configuration editor as a Blockly editor without internal blocks. The blocks
				// themselves are supplied by the config resource folder of this plugin.
				Field internalBlocksField = InternalBlocksLoader.class.getDeclaredField("internalBlocks");
				internalBlocksField.setAccessible(true);
				@SuppressWarnings("unchecked") Map<BlocklyEditorType, List<IBlockGenerator>> internalBlocks =
						(Map<BlocklyEditorType, List<IBlockGenerator>>) internalBlocksField.get(null);
				internalBlocks.put(CONFIG_EDITOR, List.of());
			} catch (ReflectiveOperationException exception) {
				LOG.warn("Could not register the configuration Blockly editor internal blocks", exception);
			}
			BlocklyLoader.INSTANCE.registerBlockLoader(CONFIG_EDITOR);
			PluginElementTypes.load();
		});

		addListener(ModifyTemplateResultEvent.class, Launcher::addConfigurationEntrypoint);
		addListener(MCreatorLoadedEvent.class, event -> {
			Workspace workspace = event.getMCreator().getWorkspace();
			if (workspace != null)
				removeLegacyApi(workspace);
		});
		addListener(WorkspaceBuildStartedEvent.class, Launcher::updateFabricDependencies);

		LOG.info("Configuration Files plugin was loaded");
	}

	/**
	 * Adds the generated configuration registry as an additional mod entrypoint.
	 * <p>
	 * Forge Config API Port requires configuration specs to be registered while the mod is initialized. The entrypoint
	 * is only added for Fabric workspaces that contain at least one configuration element, so workspaces that do not use
	 * this plugin keep their mod metadata untouched.
	 *
	 * @param event template generation event, only the Fabric mod metadata template is handled
	 */
	private static void addConfigurationEntrypoint(ModifyTemplateResultEvent event) {
		if (!FABRIC_MOD_JSON_TEMPLATE.equals(event.getTemplateName()))
			return;

		Map<String, Object> dataModel = event.getDataModel();
		Object workspaceInfo = dataModel.get("w");
		if (!(workspaceInfo instanceof WorkspaceInfo info) || !info.hasElementsOfType(CONFIG_ELEMENT_TYPE))
			return;

		Object packageName = dataModel.get("package");
		Object javaModName = dataModel.get("JavaModName");
		if (!(packageName instanceof String packagePath) || !(javaModName instanceof String modName))
			return;

		String entrypoint = packagePath + ".init." + modName + CONFIG_ENTRYPOINT_SUFFIX;
		event.setTemplateOutput(addMainEntrypoint(event.getTemplateOutput(), entrypoint));
	}

	/**
	 * Keeps the Forge Config API Port dependency in a managed block of the generated Fabric build script.
	 * <p>
	 * The Gradle integration of MCreator regenerates the workspace build script before this event fires and starts
	 * the Gradle build after it. Rewriting the script here makes the dependency independent of the workspace external
	 * API list, which is reserved for user selectable integrations. The block is removed for non Fabric workspaces and
	 * for Fabric workspaces without configuration elements.
	 *
	 * @param event workspace build event fired after base generation and before Gradle starts
	 */
	private static void updateFabricDependencies(WorkspaceBuildStartedEvent event) {
		Workspace workspace = event.getMCreator().getWorkspace();
		removeLegacyApi(workspace);

		File buildGradle = new File(workspace.getFolderManager().getWorkspaceFolder(), "build.gradle");
		if (!buildGradle.isFile())
			return;

		try {
			String original = Files.readString(buildGradle.toPath(), StandardCharsets.UTF_8);
			String stripped = stripManagedBlock(original);
			String updated = stripped;

			if (workspace.getGeneratorConfiguration().getGeneratorFlavor() == GeneratorFlavor.FABRIC
					&& workspace.getWorkspaceInfo().hasElementsOfType(CONFIG_ELEMENT_TYPE)) {
				String generatorName = workspace.getGenerator().getGeneratorName();
				FabricDependency dependency = FABRIC_DEPENDENCIES.get(generatorName);
				if (dependency == null) {
					LOG.warn("No Forge Config API Port dependency is defined for Fabric generator {}", generatorName);
				} else {
					updated = appendManagedBlock(stripped, dependency);
				}
			}

			if (!updated.equals(original))
				Files.writeString(buildGradle.toPath(), updated, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			LOG.warn("Could not update the Fabric dependency block in {}", buildGradle, exception);
		}
	}

	/**
	 * Removes the external API id that older plugin releases wrote into the workspace settings.
	 *
	 * @param workspace workspace whose dependency list is cleaned
	 */
	private static void removeLegacyApi(Workspace workspace) {
		WorkspaceSettings settings = workspace.getWorkspaceSettings();
		Set<String> dependencies = new HashSet<>(settings.getMCreatorDependenciesRaw());
		if (dependencies.remove(LEGACY_API_ID)) {
			settings.setMCreatorDependencies(dependencies);
			workspace.markDirty();
			LOG.info("Removed legacy {} workspace dependency", LEGACY_API_ID);
		}
	}

	/**
	 * Builds the managed dependency block for one Fabric generator.
	 *
	 * @param content    current build script without a managed block
	 * @param dependency port coordinates and Gradle configuration of the generator
	 * @return build script with the managed block appended
	 */
	private static String appendManagedBlock(String content, FabricDependency dependency) {
		StringBuilder block = new StringBuilder();
		block.append(MANAGED_BLOCK_START).append('\n');
		block.append("// This block is rewritten by the Configuration Files plugin on every build.\n");
		block.append("repositories {\n");
		block.append("    mavenCentral()\n");
		block.append("    maven {\n");
		block.append("        name = 'Modrinth'\n");
		block.append("        url = 'https://api.modrinth.com/maven'\n");
		block.append("    }\n");
		block.append("}\n\n");
		block.append("dependencies {\n");
		block.append("    ").append(dependency.configuration())
				.append(" 'maven.modrinth:forge-config-api-port:").append(dependency.coordinate()).append("'\n");
		if (dependency.nightConfigVersion() != null) {
			block.append("    implementation 'com.electronwill.night-config:core:")
					.append(dependency.nightConfigVersion()).append("'\n");
			block.append("    implementation 'com.electronwill.night-config:toml:")
					.append(dependency.nightConfigVersion()).append("'\n");
		}
		block.append("}\n");
		block.append(MANAGED_BLOCK_END).append('\n');

		String base = content.stripTrailing();
		return base.isEmpty() ? block.toString() : base + "\n\n" + block;
	}

	/**
	 * Removes a previously written managed dependency block.
	 *
	 * @param content current build script
	 * @return build script without the managed block
	 */
	private static String stripManagedBlock(String content) {
		int start = content.indexOf(MANAGED_BLOCK_START);
		if (start < 0)
			return content;

		int end = content.indexOf(MANAGED_BLOCK_END, start);
		if (end < 0)
			return content.substring(0, start).stripTrailing();

		String before = content.substring(0, start).stripTrailing();
		String after = content.substring(end + MANAGED_BLOCK_END.length()).stripLeading();
		if (before.isEmpty())
			return after;
		if (after.isEmpty())
			return before;
		return before + "\n\n" + after;
	}

	/**
	 * Inserts an additional main entrypoint into the main entrypoint array of a fabric.mod.json document.
	 *
	 * @param json       generated mod metadata, may be {@code null}
	 * @param entrypoint fully qualified entrypoint class name
	 * @return the document with the entrypoint added, or the unchanged document when it cannot be modified
	 */
	private static String addMainEntrypoint(String json, String entrypoint) {
		if (json == null || json.isBlank())
			return json;

		Matcher matcher = MAIN_ENTRYPOINT_ARRAY.matcher(json);
		if (!matcher.find())
			return json;

		int arrayStart = matcher.end() - 1;
		int arrayEnd = findClosingBracket(json, arrayStart);
		if (arrayEnd < 0)
			return json;

		String entry = "\"" + entrypoint + "\"";
		String arrayContent = json.substring(arrayStart + 1, arrayEnd);
		if (arrayContent.contains(entry))
			return json;

		StringBuilder result = new StringBuilder(json);
		if (arrayContent.isBlank()) {
			result.insert(arrayStart + 1, entry);
		} else {
			result.insert(arrayStart + 1, entry + ",");
		}
		return result.toString();
	}

	/**
	 * Finds the closing bracket that belongs to the given opening bracket, ignoring brackets inside string literals.
	 *
	 * @param text      document to inspect
	 * @param openIndex index of the opening bracket
	 * @return index of the matching closing bracket, or {@code -1} when the document is malformed
	 */
	private static int findClosingBracket(String text, int openIndex) {
		int depth = 0;
		boolean inString = false;
		for (int index = openIndex; index < text.length(); index++) {
			char character = text.charAt(index);
			if (inString) {
				if (character == '\\') {
					index++;
				} else if (character == '"') {
					inString = false;
				}
			} else if (character == '"') {
				inString = true;
			} else if (character == '[') {
				depth++;
			} else if (character == ']') {
				depth--;
				if (depth == 0)
					return index;
			}
		}
		return -1;
	}

	/**
	 * Forge Config API Port dependency of one Fabric generator.
	 *
	 * @param coordinate          Modrinth Maven version
	 * @param configuration       Gradle dependency configuration
	 * @param nightConfigVersion  Night Config version, or {@code null} when the port bundles the library
	 */
	private record FabricDependency(String coordinate, String configuration, String nightConfigVersion) {
	}
}
