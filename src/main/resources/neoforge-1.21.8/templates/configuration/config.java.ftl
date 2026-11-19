package ${package}.configuration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ${name}Configuration {
	public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	${variables}

	static {
		${code}
		SPEC = BUILDER.build();
	}
}
