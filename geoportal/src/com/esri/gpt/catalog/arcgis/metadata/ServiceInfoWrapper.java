/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.catalog.arcgis.metadata;

import com.esri.arcgisws.Envelope;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.http.HttpClientRequest;
import java.util.List;

/**
 * Service info wrapper.
 */
public class ServiceInfoWrapper extends ServiceInfo {
    private final ServiceInfo serviceInfo;

    public ServiceInfoWrapper(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    @Override
    public String getText() {
        return serviceInfo.getText();
    }

    @Override
    public void setText(String text) {
        serviceInfo.setText(text);
    }

    @Override
    public List<LayerInfo> getLayersInfo() {
        return serviceInfo.getLayersInfo();
    }

    @Override
    public void setLayersInfo(List<LayerInfo> layersInfo) {
        serviceInfo.setLayersInfo(layersInfo);
    }

    @Override
    public String getCopyright() {
        return serviceInfo.getCopyright();
    }

    @Override
    public void setCopyright(String copyright) {
        serviceInfo.setCopyright(copyright);
    }

    @Override
    public ServiceInfo getParentInfo() {
        return serviceInfo.getParentInfo();
    }

    @Override
    public void setParentInfo(ServiceInfo parentInfo) {
        serviceInfo.setParentInfo(parentInfo);
    }

    @Override
    public String getCreator() {
        return serviceInfo.getCreator();
    }

    @Override
    public void setCreator(String creator) {
        serviceInfo.setCreator(creator);
    }

    @Override
    public String getCapabilities() {
        return serviceInfo.getCapabilities();
    }

    @Override
    public void setCapabilities(String capabilities) {
        serviceInfo.setCapabilities(capabilities);
    }

    @Override
    public String getDescription() {
        return serviceInfo.getDescription();
    }

    @Override
    public void setDescription(String description) {
        serviceInfo.setDescription(description);
    }

    @Override
    public Envelope getEnvelope() {
        return serviceInfo.getEnvelope();
    }

    @Override
    public void setEnvelope(Envelope envelope) {
        serviceInfo.setEnvelope(envelope);
    }

    @Override
    public StringSet getKeywords() {
        return serviceInfo.getKeywords();
    }

    @Override
    public String getName() {
        return serviceInfo.getName();
    }

    @Override
    public void setName(String name) {
        serviceInfo.setName(name);
    }

    @Override
    public String getParentType() {
        return serviceInfo.getParentType();
    }

    @Override
    public void setParentType(String type) {
        serviceInfo.setParentType(type);
    }

    @Override
    public String getResourceUrl() {
        return serviceInfo.getResourceUrl();
    }

    @Override
    public void setResourceUrl(String url) {
        serviceInfo.setResourceUrl(url);
    }

    @Override
    public String getRestUrl() {
        return serviceInfo.getRestUrl();
    }

    @Override
    public void setRestUrl(String url) {
        serviceInfo.setRestUrl(url);
    }

    @Override
    public String getSoapUrl() {
        return serviceInfo.getSoapUrl();
    }

    @Override
    public void setSoapUrl(String url) {
        serviceInfo.setSoapUrl(url);
    }

    @Override
    public String getThumbnailUrl() {
        return serviceInfo.getThumbnailUrl();
    }

    @Override
    public void setThumbnailUrl(String url) {
        serviceInfo.setThumbnailUrl(url);
    }

    @Override
    public String getType() {
        return serviceInfo.getType();
    }

    @Override
    public void setType(String type) {
        serviceInfo.setType(type);
    }

    @Override
    public void addKeywords(String words, String delimiter) {
        serviceInfo.addKeywords(words, delimiter);
    }

    @Override
    public void addRDFPair(String predicate, String value) {
        serviceInfo.addRDFPair(predicate, value);
    }

    @Override
    public String asDublinCore(AGSProcessor processor) throws Exception {
        return serviceInfo.asDublinCore(processor);
    }

    @Override
    public String asDublinCore(ApplicationConfiguration cfg, HttpClientRequest http) throws Exception {
        return serviceInfo.asDublinCore(cfg, http);
    }

    @Override
    public String toString() {
        return serviceInfo.toString();
    }
    
    
}
