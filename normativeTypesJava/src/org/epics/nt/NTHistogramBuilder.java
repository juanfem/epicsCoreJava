/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.nt;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldBuilder;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;

import java.util.ArrayList;

/**
 * Interface for in-line creating of NTHistogram.
 *
 * One instance can be used to create multiple instances.
 * An instance of this object must not be used concurrently (an object has a state).
 * @author dgh
 */
public class NTHistogramBuilder
{
    /**
     * Sets the type of the value field of an NTHistogram.
     *
     * @param scalarType the scalar type of the value field elements
     * @return this instance of NTHistogramBuilder
     */
    public NTHistogramBuilder value(ScalarType scalarType)
    {
        valueType = scalarType;
        valueTypeSet = true;
        return this;
    }

    /**
     * Adds descriptor field to the NTHistogram.
     *
     * @return this instance of NTHistogramBuilder
     */
    public NTHistogramBuilder addDescriptor()
    {
        descriptor = true;
        return this;
    }

    /**
     * Adds alarm structure to the NTHistogram.
     *
     * @return this instance of NTHistogramBuilder
     */
    public NTHistogramBuilder addAlarm()
    {
        alarm = true;
        return this;
    }

    /**
     * Adds timeStamp field to the NTHistogram.
     *
     * @return this instance of NTHistogramBuilder
     */
    public NTHistogramBuilder addTimeStamp()
    {
        timeStamp = true;
        return this;
    }

    /**
     * Creates a Structure that represents NTHistogram.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a Structure
     */
    public Structure createStructure()
    {
        if (!valueTypeSet)
            throw new RuntimeException("value type not set");

        FieldBuilder builder =
            FieldFactory.getFieldCreate().createFieldBuilder().
               setId(NTHistogram.URI).
               addArray("ranges", ScalarType.pvDouble).
               addArray("value", valueType);

        NTField ntField = NTField.get();

        if (descriptor)
            builder.add("descriptor", ScalarType.pvString);

        if (alarm)
            builder.add("alarm", ntField.createAlarm());

        if (timeStamp)
            builder.add("timeStamp", ntField.createTimeStamp());

        int extraCount = extraFieldNames.size();
        for (int i = 0; i< extraCount; i++)
            builder.add(extraFieldNames.get(i), extraFields.get(i));

        Structure s = builder.createStructure();

        reset();
        return s;
    }

    /**
     * Creates a PVStructure that represents NTHistogram.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of a PVStructure
     */
    public PVStructure createPVStructure()
    {
        return PVDataFactory.getPVDataCreate().createPVStructure(createStructure());
    }

    /**
     * Creates an NTHistogram instance.
     * This resets this instance state and allows new instance to be created.
     *
     * @return a new instance of an NTHistogram
     */
    public NTHistogram create()
    {
        return new NTHistogram(createPVStructure());
    }

    /**
     * Adds extra Field to the type.
     *
     * @param name the name of the field
     * @param field the field to add
     * @return this instance of NTHistogramBuilder
     */
    public NTHistogramBuilder add(String name, Field field) 
    {
        extraFields.add(field);
        extraFieldNames.add(name);
        return this;
    }

    NTHistogramBuilder()
    {
        reset();
    }

    private void reset()
    {
        valueTypeSet = false;
        descriptor = false;
        alarm = false;
        timeStamp = false;
        extraFieldNames.clear();
        extraFields.clear();
    }
    private boolean valueTypeSet;
    private ScalarType valueType;

    private boolean descriptor;
    private boolean alarm;
    private boolean timeStamp;

    // NOTE: this preserves order, however it does not handle duplicates
    private ArrayList<String> extraFieldNames = new ArrayList<String>();
    private ArrayList<Field> extraFields = new ArrayList<Field>();
}

