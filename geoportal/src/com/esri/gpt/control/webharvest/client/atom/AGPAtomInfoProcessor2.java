package com.esri.gpt.control.webharvest.client.atom;

/**
 * This is class is used to perform harvesting Portal for ArcGIS.
 * It harvests standard metadata xml if available.
 */
public class AGPAtomInfoProcessor2 extends BaseAtomInfoProcessor {

	@Override
	public void preInitialize() {	
	}
	/**
	 * This method sets specific class to be used to collect hit count, 
	 * entry processor and initialize hit count collector.
	 * @param atomInfo the BaseAtomInfo
	 */
	@Override
	public void postCreate(BaseAtomInfo atomInfo) {
		atomInfo.setHitCountCollectorClassName("com.esri.gpt.control.webharvest.client.atom.AGPHitCountCollector");
		atomInfo.setEntryProcessorClassName("com.esri.gpt.control.webharvest.client.atom.AGPEntryProcessor2");
		initializeHitCountCollector();
		try {
			atomInfo.setTotalResults(atomInfo.getHitCountCollector().collectHitCount(atomInfo.getUrl()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// add more logic if needed
	}

}
