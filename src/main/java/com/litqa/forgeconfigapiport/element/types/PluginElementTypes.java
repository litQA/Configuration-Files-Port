package com.litqa.forgeconfigapiport.element.types;

import net.mcreator.element.ModElementType;
import net.mcreator.generator.GeneratorFlavor;
import com.litqa.forgeconfigapiport.ui.modgui.ConfigGUI;

import static net.mcreator.element.ModElementTypeLoader.register;

public class PluginElementTypes {
	public static ModElementType<?> CONFIG;

	public static void load() {
		// The element is only covered on Fabric, the only platform Forge Config API Port targets.
		CONFIG = register(new ModElementType<>("config", (Character) null, ConfigGUI::new, Config.class)
				.coveredOn(GeneratorFlavor.FABRIC));
	}
}
