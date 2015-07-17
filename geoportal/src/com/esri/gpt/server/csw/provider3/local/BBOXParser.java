/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.server.csw.provider3.local;

import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.DiscoveryFilter;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.catalog.discovery.LogicalClause;
import com.esri.gpt.catalog.discovery.PropertyMeaning;
import com.esri.gpt.catalog.discovery.PropertyMeaning.Geometry;
import com.esri.gpt.catalog.discovery.SpatialClause;
import com.esri.gpt.catalog.lucene.GeometryProperty;
import com.esri.gpt.server.csw.components.IBBOXParser;
import com.esri.gpt.server.csw.components.OperationContext;
import com.esri.gpt.server.csw.components.OwsException;
import com.esri.gpt.server.csw.components.QueryOptions;
import java.util.logging.Logger;

/**
 * BBOX parameter parser.
 */
public class BBOXParser extends DiscoveryAdapter implements IBBOXParser {

    /**
     * class variables =========================================================
     */
    /**
     * The Logger.
     */
    private static Logger LOGGER = Logger.getLogger(BBOXParser.class.getName());

    /**
     * instance variables ======================================================
     */
    private OperationContext opContext;

    /**
     * constructors ============================================================
     */
    /**
     * Default constructor
     */
    public BBOXParser(OperationContext context) {
        super(context);
        this.opContext = context;
    }

    /**
     * methods =================================================================
     */
    @Override
    public void parseBBOX(OperationContext context, String[] bboxDefinition, String crs) throws OwsException {

        QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
        DiscoveryQuery query = this.getDiscoveryContext().getDiscoveryQuery();
        DiscoveryFilter filter = query.getFilter();
        
        if (filter.getRootClause()==null) {
          filter.setRootClause(new LogicalClause.LogicalAnd());
        }
        LogicalClause rootClause = filter.getRootClause();
        SpatialClause spatialClause = new SpatialClause.GeometryBBOXIntersects();
        rootClause.getClauses().add(spatialClause);
        PropertyMeaning meaning = new Geometry();
        Discoverable target = new Discoverable(meaning.getName());
        target.setMeaning(meaning);
        target.setStoreable(new GeometryProperty(meaning.getName()));
        spatialClause.setTarget(target);
        
        spatialClause.getBoundingEnvelope().setMinX(parseNumber(bboxDefinition[0]));
        spatialClause.getBoundingEnvelope().setMinY(parseNumber(bboxDefinition[1]));
        spatialClause.getBoundingEnvelope().setMaxX(parseNumber(bboxDefinition[2]));
        spatialClause.getBoundingEnvelope().setMaxY(parseNumber(bboxDefinition[3]));
        spatialClause.setSrsName(crs!=null? crs: "4326");
    }

    private double parseNumber(String number) throws OwsException {
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException ex) {
            throw new OwsException(OwsException.OWSCODE_InvalidFormat, "bbox", "Invalid BBOX corner");
        }
    }
}
