/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.dcat;

/**
 * DCAT parse exception.
 */
public class DcatParseException extends Exception {

  /**
   * Creates a new instance of
   * <code>DcatParseException</code> without detail message.
   */
  public DcatParseException() {
  }

  /**
   * Constructs an instance of
   * <code>DcatParseException</code> with the specified detail message.
   *
   * @param msg the detail message.
   */
  public DcatParseException(String msg) {
    super(msg);
  }

  /**
   * Constructs an instance of
   * <code>DcatParseException</code> with the specified detail message and cause.
   *
   * @param msg the detail message.
   * @param t cause
   */
  public DcatParseException(String msg, Throwable t) {
    super(msg, t);
  }
}
