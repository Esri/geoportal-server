/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.server.csw.components;

import javax.xml.xpath.XPathExpressionException;

/**
 * Query parser.
 */
public interface IQueryParser {

  /**
   * Parses query.
   * @param context operation context
   * @param keywords keywords
   * @throws OwsException if validation fails
   */
  public void parseQuery(OperationContext context, String [] keywords) 
    throws OwsException;
}
