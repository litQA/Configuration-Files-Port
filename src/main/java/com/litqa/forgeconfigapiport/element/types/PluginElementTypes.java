package com.litqa.forgeconfigapiport.element.types;

import net.mcreator.element.ModElementType;
import net.mcreator.generator.GeneratorFlavor;
import com.litqa.forgeconfigapiport.ui.modgui.ConfigGUI;

import static net.mcreator.element.ModElementTypeLoader.register;

public class PluginElementTypes {
	public static ModElementType<?> CONFIG;

	public static void load() {
		// The element is covered by the three generator flavors that this branch provides templates for. The generator
		// coverage of MCreator still hides the element on generator versions without a config definition.
		CONFIG = register(new ModElementType<>("config", (Character) null, ConfigGUI::new, Config.class)
				.coveredOn(GeneratorFlavor.FORGE, GeneratorFlavor.FABRIC, GeneratorFlavor.NEOFORGE));
	}
}
