package com.hbm.util;

import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArmorRegistry {

	public static HashMap<Item, ArrayList<HazardClass>> hazardClasses = new HashMap<>();
	
	public static void registerHazard(Item item, HazardClass... hazards) {
		hazardClasses.put(item, new ArrayList<HazardClass>(Arrays.asList(hazards)));
	}
	
	public static boolean hasAllProtection(EntityLivingBase entity, EntityEquipmentSlot slot, HazardClass... clazz) {
		
		if(ArmorUtil.checkArmorNull(entity, slot))
			return false;
		
		List<HazardClass> list = getProtectionFromItem(entity.getItemStackFromSlot(slot));
		return list.containsAll(Arrays.asList(clazz));
	}
	
	public static boolean hasAnyProtection(EntityLivingBase entity, EntityEquipmentSlot slot, HazardClass... clazz) {
		
		if(ArmorUtil.checkArmorNull(entity, slot))
			return false;
		
		List<HazardClass> list = getProtectionFromItem(entity.getItemStackFromSlot(slot));
		
		if(list == null)
			return false;
		
		for(HazardClass haz : clazz) {
			if(list.contains(haz)) return true;
		}
		
		return false;
	}
	
	public static boolean hasProtection(EntityLivingBase entity, EntityEquipmentSlot slot, HazardClass clazz) {
		
		if(ArmorUtil.checkArmorNull(entity, slot))
			return false;
		
		List<HazardClass> list = getProtectionFromItem(entity.getItemStackFromSlot(slot));
		
		if(list == null)
			return false;
		
		return list.contains(clazz);
	}
	
	public static List<HazardClass> getProtectionFromItem(ItemStack stack) {

		List<HazardClass> prot = new ArrayList<>();
		
		Item item = stack.getItem();
		
		//if the item has HazardClasses assigned to it, add those
		if(hazardClasses.containsKey(item))
			prot.addAll(hazardClasses.get(item));
		
		if(item instanceof IGasMask) {
			IGasMask mask = (IGasMask) item;
			ItemStack filter = mask.getFilter(stack);

			if(filter != null && !filter.isEmpty()) {
				//add the HazardClasses from the filter, then remove the ones blacklisted by the mask
				List<HazardClass> filProt = hazardClasses.get(filter.getItem());
				
				for(HazardClass c : mask.getBlacklist(stack))
					filProt.remove(c);
				
				prot.addAll(filProt);
			}
		}
		
		if(ArmorModHandler.hasMods(stack)) {
			
			ItemStack[] mods = ArmorModHandler.pryMods(stack);
			
			for(ItemStack mod : mods) {
				
				//recursion! run the exact same procedure on every mod, in case future mods will have filter support
				if(mod != null)
					prot.addAll(getProtectionFromItem(mod));
			}
		}
		
		return prot;
	}
	
	public static enum HazardClass {
		GAS_CHLORINE("hazard.gasChlorine"),				//also attacks eyes -> no half mask (chlorine seal)
		GAS_MONOXIDE("hazard.gasMonoxide"),				//only affects lungs (nether coal gas)
		GAS_INERT("hazard.gasInert"),					//SA
		PARTICLE_COARSE("hazard.particleCoarse"),		//only affects lungs (coal dust)
		PARTICLE_FINE("hazard.particleFine"),			//only affects lungs (asbestos dust)
		BACTERIA("hazard.bacteria"),					//no half masks (cloud/taint)
		NERVE_AGENT("hazard.nerveAgent"),				//aggressive nerve agent, also attacks skin (poison vent)
		GAS_CORROSIVE("hazard.corrosive"),				//corrosive substance, also attacks skin (cloud gas particles)
		SAND("hazard.sand"),							//blinding sand particles
		LIGHT("hazard.light"),							//blinding light (blinding)
		RAD_GAS("hazard.radGas");						//radon and wastegases
		
		public final String lang;
		
		private HazardClass(String lang) {
			this.lang = lang;
		}
	}
	
	/*public static enum ArmorClass {
		MASK_FILTERED,
		MASK_OXY,
		GOGGLES,
		HAZMAT_HEAT,
		HAZMAT_RADIATION,
		HAZMAT_BIO;
	}*/
}
