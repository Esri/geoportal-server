package com.esri.gpt.server.usage.api;

import com.esri.gpt.server.usage.harvester.HarvesterStatisticsBuilder;

/**
 * Usage information.
 */
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
    /**
     * Gets statistics builder.
     * @return statistics builder
     */
	public HarvesterStatisticsBuilder getHarvesterStats() {
		return harvesterStats;
	}

    /**
     * Sets statistics builder.
     * @param harvesterStats statistics builder
     */
	public void setHarvesterStats(HarvesterStatisticsBuilder harvesterStats) {
		this.harvesterStats = harvesterStats;
	}

	// method ==================================================================

}
