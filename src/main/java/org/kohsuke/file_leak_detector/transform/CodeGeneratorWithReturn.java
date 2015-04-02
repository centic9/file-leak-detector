package org.kohsuke.file_leak_detector.transform;

import org.kohsuke.asm5.Opcodes;
import org.kohsuke.asm5.Type;
import org.kohsuke.asm5.commons.LocalVariablesSorter;

public class CodeGeneratorWithReturn extends LocalVariablesSorter implements CodeGenerator {
	private final Class<?> returnType;
	private final CodeGeneratorImpl cg;

	public CodeGeneratorWithReturn(String desc, CodeGeneratorImpl cg, Class<?> returnType) {
		super(Opcodes.ASM5, Opcodes.ACC_STATIC, desc, cg);
		
		this.returnType = returnType;
		this.cg = cg;
	}
	
	public void invokeAppStatic(String userClassName, String userMethodName,
			Class[] argTypes, int[] localIndex) {
    	// create a new local variable
    	int retValueIndex = newLocal(Type.getType(returnType));
    	
    	// remember the return value for ARETURN if necessary
		cg.astore(retValueIndex);

		// invoke the normal handling of local variables
    	cg.invokeAppStatic(userClassName, userMethodName, argTypes, localIndex);

        // store the value for ARETURN if necessary
        if(retValueIndex != -1) {
            cg.dup();
            cg.iconst(0);
            cg.aload(retValueIndex);
            cg.aastore();
        }
        
        // ensure ARETURN has the correct value again
    	cg.aload(retValueIndex);
    }

	public void invokeVirtual(String owner, String name, String desc) {
		cg.invokeVirtual(owner, name, desc);
	}

	public void invokeAppStatic(Class userClass, String userMethodName,
			Class[] argTypes, int[] localIndex) {
		cg.invokeAppStatic(userClass, userMethodName, argTypes, localIndex);
	}
}
