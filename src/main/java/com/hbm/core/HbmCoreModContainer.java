package com.hbm.core;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

public class HbmCoreModContainer extends DummyModContainer {

	public HbmCoreModContainer() {
		super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "hbmcore";
        meta.name = "FaszomHBM 2 Core";
		meta.credits = "nzoliii";
        meta.description = "FaszomHBM 2 Core Mod";
        meta.version = "Community-Edition";
		meta.authorList = Arrays.asList("HBMMods", "Drillgon200", "TheOriginalGolem", "nzoliii");
		meta.url = "https://github.com/nzoliii/faszomhbm2";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
}
