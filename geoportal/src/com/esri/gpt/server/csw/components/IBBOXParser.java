/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.server.csw.components;

/**
 * BBOX parameter parser.
 */
public interface IBBOXParser {

    /**
     * Parses BBOX
     * @param context context
     * @param bboxDefinition bbox definition
     * @param crs CRS or <code>null</code>
   * @throws OwsException if validation fails
     */
    public void parseBBOX(OperationContext context, String[] bboxDefinition, String crs)
            throws OwsException;

}
