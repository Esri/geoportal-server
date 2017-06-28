/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.framework.geometry;

/**
 * Immutable envelope wrapper.
 */
final class ImmutableEnvelopeWrapper extends Envelope {
  private final Envelope source;

  public ImmutableEnvelopeWrapper(Envelope source) {
    this.source = source;
  }

  @Override
  public double getHeight() {
    return source.getHeight();
  }

  @Override
  public double getMaxX() {
    return source.getMaxX();
  }

  @Override
  public double getMaxY() {
    return source.getMaxY();
  }

  @Override
  public double getMinX() {
    return source.getMinX();
  }

  @Override
  public double getMinY() {
    return source.getMinY();
  }

  @Override
  public double getWidth() {
    return source.getWidth();
  }

  @Override
  public double getCenterX() {
    return source.getCenterX();
  }

  @Override
  public double getCenterY() {
    return source.getCenterY();
  }

  @Override
  public void echo(StringBuffer sb) {
    source.echo(sb);
  }

  @Override
  public String toString() {
    return source.toString();
  }

  @Override
  public boolean hasSize() {
    return source.hasSize();
  }

  @Override
  public boolean isEmpty() {
    return source.isEmpty();
  }

  @Override
  public boolean isValid() {
    return source.isValid();
  }

  @Override
  public Envelope clone() {
    return source.clone();
  }

  @Override
  public boolean equals(Object obj) {
    return source.equals(obj);
  }

  @Override
  public String getWkid() {
    return source.getWkid();
  }

  @Override
  public boolean isValidWGS84() {
    return source.isValidWGS84();
  }

  @Override
  public void setWkid(String wkid) {
  }

  @Override
  public void merge(Envelope envelope) {
  }

  @Override
  public void put(String minx, String miny, String maxx, String maxy) {
  }

  @Override
  public void put(double minx, double miny, double maxx, double maxy) {
  }

  @Override
  public void setMinY(String s) {
  }

  @Override
  public void setMinY(double d) {
  }

  @Override
  public void setMinX(String s) {
  }

  @Override
  public void setMinX(double d) {
  }

  @Override
  public void setMaxY(String s) {
  }

  @Override
  public void setMaxY(double d) {
  }

  @Override
  public void setMaxX(String s) {
  }

  @Override
  public void setMaxX(double d) {
  }
  
  
  
}
