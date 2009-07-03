/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.util.ArrayList;

import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.Type;


/**
 * Abstract base class for a PVField.
 * A factory that implements PVField must extend this class.
 * @author mrk
 *
 */
public abstract class AbstractPVField implements PVField{
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private int fieldOffset = 0;;
    private int nextFieldOffset = 0;;
    private PVAuxInfo pvAuxInfo = null;
    private boolean isImmutable = false;
    private String fullFieldName = "";
    private String fullName = "";
    private Field field;
    private PVStructure pvParent;
    private PVRecord pvRecord = null;
    // since list is mostly traversed an ArrayList is efficent.
    protected ArrayList<PVListener> pvListenerList = new ArrayList<PVListener>(0);
    
    /**
     * Convenience for derived classes that perform conversions.
     */
    protected static Convert convert = ConvertFactory.getConvert();
    /**
     * Constructor that must be called by derived classes.
     * @param pvParent The pvParent PVStructure.
     * @param field The introspection interface.
     * @throws IllegalArgumentException if field is null;
     */
    protected AbstractPVField(PVStructure parent, Field field) {
        if(field==null) {
            throw new IllegalArgumentException("field is null");
        }
        pvAuxInfo = new BasePVAuxInfo(this);
        this.field = field;
        this.pvParent = parent;
        if(parent==null) {
            fullFieldName = fullName = field.getFieldName();
            return;
        }
        pvRecord = parent.getPVRecord();
        createFullNameANDFullFieldName();
    }
    /**
     * Called by derived classes to replace a field.
     * @param field The new field.
     */
    protected void replaceField(Field field) {
        this.field = field;
        createFullNameANDFullFieldName();
    }
    /**
     * Called by derived class to specify the PVRecord interface.
     * @param pvRecord The PVRecord interface.
     */
    protected void setRecord(PVRecord record) {
        this.pvRecord = record;
        createFullNameANDFullFieldName();
    }
    /**
     * Called by BasePVStructure.postPut() and recursively by itself.
     * @param pvField The pvField to post.
     */
    protected void postPutNoParent(AbstractPVField pvField) {
        pvField.callListener();
        if(pvField.getField().getType()==Type.structure) {
            PVStructure pvStructure = (PVStructure)pvField;
            PVField[] pvFields = pvStructure.getPVFields();
            for(PVField pvf : pvFields) {
                postPutNoParent((AbstractPVField)pvf);
            }
        }
        
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#removeEveryListener()
     */
    protected void removeEveryListener() {
        if(pvListenerList.size()!=0) {
        pvListenerList.clear();
        }
    }
    /**
     * Called by BasePVStructure
     * @param fieldOffset The fieldOffset for this field.
     * @param nextFieldOffset The nextFieldOffset for this field.
     */
    protected void setOffsets(int offset,int nextOffset) {
        this.fieldOffset = offset;
        this.nextFieldOffset = nextOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getOffset()
     */
    @Override
    public int getFieldOffset() {
        return fieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNextOffset()
     */
    @Override
    public int getNextFieldOffset() {
        return nextFieldOffset;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getNumberFields()
     */
    @Override
    public int getNumberFields() {
        return (nextFieldOffset - fieldOffset);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVAuxInfo()
     */
    @Override
    public PVAuxInfo getPVAuxInfo() {
        return pvAuxInfo;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#getRequesterName()
     */
    @Override
    public String getRequesterName() {
        return getFullName();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.Requester#message(java.lang.String, org.epics.pvData.pv.MessageType)
     */
    @Override
    public void message(String message, MessageType messageType) {
        if(pvRecord==null) {
            System.out.println(
                    messageType.toString() + " " + fullFieldName + " " + message);
        } else {
            pvRecord.message(fullName + " " + message, messageType);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return(isImmutable);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#setMutable(boolean)
     */
    @Override
    public void setImmutable() {
        if(!isImmutable && pvRecord!=null) {
            pvRecord.removeEveryListener();
        }
        isImmutable = true;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullFieldName()
     */
    @Override
    public String getFullFieldName() {
        return fullFieldName;
    } 
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getFullName()
     */
    @Override
    public String getFullName() {
        return fullName;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getField()
     */
    public Field getField() {
        return field;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getParent()
     */
    @Override
    public PVStructure getParent() {
        return pvParent;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#getPVRecord()
     */
    @Override
    public PVRecord getPVRecord() {
       return pvRecord;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#replacePVField(org.epics.pvData.pv.PVField)
     */
    @Override
    public void replacePVField(PVField newPVField) {
        PVStructure parent = getParent();
        if(parent==null) throw new IllegalArgumentException("no pvParent");
        parent.replacePVField(field.getFieldName(), newPVField);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#renameField(java.lang.String)
     */
    @Override
    public void renameField(String newName) {
        switch(field.getType()) {
        case scalar: {
            Scalar scalar = (Scalar)field;
            scalar = fieldCreate.createScalar(newName, scalar.getScalarType());
            this.field = scalar;
            createFullNameANDFullFieldName();
            return;
        }
        case scalarArray: {
            Array array = (Array)field;
            array = fieldCreate.createArray(newName, array.getElementType());
            this.field = array;
            createFullNameANDFullFieldName();
            return;
        }
        case structure: {
            Structure structure = (Structure)field;
            Field[] origFields = structure.getFields();
            structure = fieldCreate.createStructure(newName, origFields);
            this.field = structure;
            createFullNameANDFullFieldName();
            PVStructure pvStructure = (PVStructure)this;
            // must make all subfields call createFullNameANDFullFieldName()
            for(PVField pvField: pvStructure.getPVFields()) {
                pvField.renameField(pvField.getField().getFieldName());
            }
            return;
        }
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#addListener(org.epics.pvData.pv.PVListener)
     */
    @Override
    public boolean addListener(PVListener pvListener) {
        if(pvRecord==null) return false;
        if(!pvRecord.isRegisteredListener(pvListener)) return false;
        return pvListenerList.add(pvListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#removeListener(org.epics.pvData.pv.PVListener)
     */
    @Override
    public void removeListener(PVListener recordListener) {
        pvListenerList.remove(recordListener);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#postPut()
     */
    @Override
    public void postPut() {
        if(nextFieldOffset==0) return; // setOffsets has never been called.
        callListener();
        if(pvParent!=null)pvParent.postPut(this);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toString(0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVField#toString(int)
     */
    @Override
    public String toString(int indentLevel) {
        return pvAuxInfo.toString(indentLevel);
    }
    
    private void callListener() {
        // don't create iterator
        for(int index=0; index<pvListenerList.size(); index++) {
            PVListener pvListener = pvListenerList.get(index);
            if(pvListener!=null) pvListener.dataPut(this);
        }
    }
    
    private void createFullNameANDFullFieldName() {
        if(this==pvRecord) {
            fullName = pvRecord.getRecordName();
            fullFieldName = "";
            return;
        }
        StringBuilder fieldName = new StringBuilder();
        Field field = getField();
        String name = field.getFieldName();
        if(name!=null && name.length()>0) {
            fieldName.append(name);
        }
        PVField parent = getParent();
        while(parent!=null && parent!=this.pvRecord) {
            field = parent.getField();
            name = field.getFieldName();
            if(name!=null && name.length()>0) {
                fieldName.insert(0,parent.getField().getFieldName()+ ".");
            }
            parent = parent.getParent();
        }
        fullFieldName = fieldName.toString();
        if(pvRecord!=null) {
            fullName = pvRecord.getRecordName() + "." + fullFieldName;
        } else {
            fullName = fullFieldName;
        }
    }
}
