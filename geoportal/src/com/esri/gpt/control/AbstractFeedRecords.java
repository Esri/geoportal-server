/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.esri.gpt.control;

import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.control.georss.IFeedRecords;
import java.util.AbstractList;
import java.util.List;

/**
 * Abstract feed records
 */
public abstract class AbstractFeedRecords extends AbstractList<IFeedRecord> implements IFeedRecords {
    protected final List<IFeedRecord> records;

    public AbstractFeedRecords(List<IFeedRecord> records) {
        this.records = records;
    }

    @Override
    public IFeedRecord get(int index) {
        return records.get(index);
    }

    @Override
    public int size() {
        return records.size();
    }
    
}
