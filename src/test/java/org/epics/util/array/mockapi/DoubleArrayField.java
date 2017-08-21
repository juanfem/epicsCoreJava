/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListNumber;
import org.epics.util.array.ListNumbers;

/**
 *
 * @author carcassi
 */
public class DoubleArrayField implements NumericArrayField {

    public DoubleArrayField(double[] backendArray) {
        this.backendArray = backendArray;
    }
    
    double[] backendArray;

    @Override
    public ArrayDouble get() {
        return ListNumbers.unmodifiableListDouble(backendArray);
    }

    @Override
    public void put(int index, ListNumber data) {
        ListNumbers.toList(backendArray).setAll(index, data);
    }
    
}
