package com.esri.gpt.server.usage.api;

import com.esri.gpt.server.usage.harvester.HarvesterStatisticsBuilder;

public class GeoportalUsageInformation {

	// class variables
	// =============================================================

	// instance variables
	// ==========================================================
	private HarvesterStatisticsBuilder harvesterStats;

	// constructors
	// ================================================================

	/** Default constructor. */

	// properties
	// ==================================================================
	public HarvesterStatisticsBuilder getHarvesterStats() {
		return harvesterStats;
	}

	public void setHarvesterStats(HarvesterStatisticsBuilder harvesterStats) {
		this.harvesterStats = harvesterStats;
	}

	// method ==================================================================

}
