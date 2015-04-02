package org.kohsuke.file_leak_detector.transform;

public interface CodeGenerator {

	public abstract void invokeVirtual(String owner, String name, String desc);

	/**
	 * Invokes a static method on the class in the system classloader.
	 *
	 * This is used for instrumenting classes in the bootstrap classloader,
	 * which cannot see the classes in the system classloader.
	 */
	//    public void invokeAppStatic(String userClassName, String userMethodName, Class[] argTypes, int[] localIndex) {
	//        visitMethodInsn(INVOKESTATIC,"java/lang/ClassLoader","getSystemClassLoader","()Ljava/lang/ClassLoader;");
	//        ldc(userClassName);
	//        invokeVirtual("java/lang/ClassLoader","loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
	//        ldc(userMethodName);
	//        newArray("java/lang/Class",0);
	////        for (int i = 0; i < argTypes.length; i++)
	////            storeConst(i, argTypes[i]);
	//
	//        invokeVirtual("java/lang/Class","getDeclaredMethod","(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
	//        pop();
	//    }

	public abstract void invokeAppStatic(Class userClass,
			String userMethodName, Class[] argTypes, int[] localIndex);

	public abstract void invokeAppStatic(String userClassName,
			String userMethodName, Class[] argTypes, int[] localIndex);
}
