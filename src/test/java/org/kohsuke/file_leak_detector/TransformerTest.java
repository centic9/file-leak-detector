package org.kohsuke.file_leak_detector;

import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kohsuke.asm5.ClassReader;
import org.kohsuke.asm5.util.CheckClassAdapter;
import org.kohsuke.file_leak_detector.transform.ClassTransformSpec;
import org.kohsuke.file_leak_detector.transform.TransformerImpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@RunWith(Parameterized.class)
public class TransformerTest {
    List<ClassTransformSpec> specs = AgentMain.createSpec();

    Class<?> c;
    
    public TransformerTest(Class<?> c) {
        this.c = c;
    }

    @Test
    public void testInstrumentations() throws Exception {
        TransformerImpl t = new TransformerImpl(specs);

        String name = c.getName().replace('.', '/');
        byte[] data = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream(name + ".class"));
        byte[] data2 = t.transform(name,data);

//        File classFile = new File("/tmp/" + name + ".class");
//        classFile.getParentFile().mkdirs();
//        FileOutputStream o = new FileOutputStream(classFile);
//        o.write(data2);
//        o.close();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(data2), false, pw);
        System.err.print(sw.toString());
        assertTrue(sw.toString(), sw.toString().isEmpty());
    }
    
    @Parameters
    public static List<Object[]> specs() throws Exception {
        List<Object[]> r = new ArrayList<Object[]>();
        for (ClassTransformSpec s : AgentMain.createSpec()) {
            Class<?> c = TransformerTest.class.getClassLoader().loadClass(s.name.replace('/', '.'));
            r.add(new Object[]{c});
        }
        return r;
    }
}
