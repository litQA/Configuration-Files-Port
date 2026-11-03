package com.litqa.forgeconfigapiport;

import net.mcreator.blockly.IBlockGenerator;
import net.mcreator.blockly.InternalBlocksLoader;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.ModifyTemplateResultEvent;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.workspace.misc.WorkspaceInfo;
import com.litqa.forgeconfigapiport.element.types.PluginElementTypes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plugin entry point.
 * <p>
 * Registers the configuration Blockly editor, the configuration mod element type and the Fabric mod metadata hook that
 * wires the generated configuration registry into the mod entrypoints.
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
	/** Matches the main entrypoint array of a generated fabric.mod.json. */
	private static final Pattern MAIN_ENTRYPOINT_ARRAY = Pattern.compile("\"main\"\\s*:\\s*\\[");

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

		LOG.info("Forge Config API Port configuration plugin was loaded");
	}

	/**
	 * Adds the generated configuration registry as an additional mod entrypoint.
	 * <p>
	 * Forge Config API Port requires configuration specs to be registered while the mod is initialized. The entrypoint
	 * is only added for workspaces that contain at least one configuration element, so workspaces that do not use this
	 * plugin keep their mod metadata untouched.
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
}
