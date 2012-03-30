/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import java.nio.ByteBuffer;

import org.epics.pvData.pv.DeserializableControl;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarArray;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Structure;
import org.epics.pvData.pv.StructureArray;

/**
 * FieldFactory creates Field instances.
 * User code creates introspection objects via FieldCreate,
 * which is obtained via a call to <i>FieldFactory.getFieldCreate</i>.
 * This is a complete factory for the <i>PV</i> reflection.
 * Most <i>PV</i> database implementations should find this sufficient for
 * <i>PV</i> reflection.
 * @author mrk
 *
 */
public final class FieldFactory {   
    private FieldFactory(){} // don't create
    /**
     * Get the FieldCreate interface.
     * @return The interface for creating introspection objects.
     */
    public static FieldCreate getFieldCreate() {
        return FieldCreateImpl.getFieldCreate();
    }
    
    private static final class FieldCreateImpl implements FieldCreate{
    	private static FieldCreateImpl singleImplementation = null;
        private static synchronized FieldCreateImpl getFieldCreate() {
                if (singleImplementation==null) {
                    singleImplementation = new FieldCreateImpl();
                }
                return singleImplementation;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#create(java.lang.String, org.epics.pvData.pv.Field)
         */
        @Override
        public Field create(Field field) {
        	switch(field.getType()) {
        	case scalar: {
        		Scalar scalar = (Scalar)field;
        		return createScalar(scalar.getScalarType());
        	}
        	case scalarArray:{
        		ScalarArray array = (ScalarArray)field;
        		return createScalarArray(array.getElementType());
        	}
        	case structure: {
        		throw new IllegalArgumentException("can not create a structure without fieldNames");
        	}
        	case structureArray: {
        		StructureArray structureArray = (StructureArray)field;
        		return createStructureArray(structureArray.getStructure());
        	}
        	}
        	throw new IllegalStateException("Logic error. Should never get here");
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public ScalarArray createScalarArray(ScalarType elementType)
        {
            return new BaseScalarArray(elementType);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createArray(java.lang.String, org.epics.pvData.pv.Structure)
         */
        @Override
		public StructureArray createStructureArray(Structure elementStructure)
        {
			return new BaseStructureArray(elementStructure);
		}
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createScalar(java.lang.String, org.epics.pvData.pv.ScalarType)
         */
        public Scalar createScalar(ScalarType type)
        {
            return new BaseScalar(type);
        }
		/* (non-Javadoc)
         * @see org.epics.pvData.pv.FieldCreate#createStructure(java.lang.String, org.epics.pvData.pv.Field[])
         */
        public Structure createStructure(String[] fieldNames, Field[] fields)
        {
            return new BaseStructure(fieldNames,fields);
        }
        
        
        
        
        
    	static final ScalarType integerLUT[] =
    	{
    		ScalarType.pvByte,  // 8-bits
    		ScalarType.pvShort, // 16-bits
    		ScalarType.pvInt,   // 32-bits
    		ScalarType.pvLong,  // 64-bits
    		null, 
    		null, 
    		null, 
    		null, 
    		ScalarType.pvUByte,  // unsigned 8-bits
    		ScalarType.pvUShort, // unsigned 16-bits
    		ScalarType.pvUInt,   // unsigned 32-bits
    		ScalarType.pvULong,  // unsigned 64-bits
    		null,
    		null,
    		null,
    		null
    	};

    	static final ScalarType floatLUT[] =
    	{
    		null, // reserved
    		null, // 16-bits
    		ScalarType.pvFloat,   // 32-bits
    		ScalarType.pvDouble,  // 64-bits
    		null, 
    		null,
    		null, 
    		null,
    		null, 
    		null,
    		null, 
    		null,
    		null, 
    		null,
    		null, 
    		null
    	};

    	static final ScalarType decodeScalar(byte code)
    	{
    		// bits 7-5
    		switch (code >> 5)
    		{
    		case 0: return ScalarType.pvBoolean;
    		case 1: return integerLUT[code & 0x0F];
    		case 2: return floatLUT[code & 0x0F];
    		case 3: return ScalarType.pvString;
    		default: return null;
    		}
    	}
        
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.FieldCreate#deserialize(java.nio.ByteBuffer, org.epics.pvData.pv.DeserializableControl)
		 */
		@Override
		public Field deserialize(ByteBuffer buffer, DeserializableControl control) {
    		control.ensureData(1);
    		final byte code = buffer.get();
    		if (code == (byte)-1)
    			return null;
    		
    		final int typeCode = code & 0xE0;
    		final boolean notArray = ((code & 0x10) == 0);
    		if (notArray)
    		{			
    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalar;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalar type encoding");
    				return new BaseScalar(scalarType);
    			}
    			else if (typeCode == 0x80)
    			{
    				// Type type = Type.structure;
    				return BaseStructure.deserializeStructureField(buffer, control);
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
    		else // array
    		{
    			if (typeCode < 0x80)
    			{
    				// Type type = Type.scalarArray;
    				ScalarType scalarType = decodeScalar(code);
    				if (scalarType == null)
    					throw new IllegalArgumentException("invalid scalarArray type encoding");
    				return new BaseScalar(scalarType);
    			}
    			else if (typeCode == 0x80)
    			{
    				// Type type = Type.structureArray;
    				final Structure elementStructure = (Structure)control.cachedDeserialize(buffer);
    				return new BaseStructureArray(elementStructure);
    			}
    			else
    				throw new IllegalArgumentException("invalid type encoding");
    		}
		}  
        
        
    }
}
