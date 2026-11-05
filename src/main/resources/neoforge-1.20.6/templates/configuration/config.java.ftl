package ${package}.configuration;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ${name}Configuration {
	public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec SPEC;

	${variables}

	static {

	    ${code}

		SPEC = BUILDER.build();
 	}

}