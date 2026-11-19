package ${package}.configuration;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class ${name}Configuration {
	public static Configuration CONFIG;

	${variables}

	private static String CATEGORY = "";
	private static final Deque<String> CATEGORY_STACK = new ArrayDeque<>();

	public static void init(File file) {
		CONFIG = new Configuration(file);
		CONFIG.load();
		${code}
		CONFIG.save();
	}

	private static String category() {
		return CATEGORY.isEmpty() ? Configuration.CATEGORY_GENERAL : CATEGORY;
	}
}
