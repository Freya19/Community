package site.pyyf.compile;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringSourceCompiler {
    private static Map<String, JavaFileObject> fileObjectMap = new ConcurrentHashMap<>();

    /** 使用 Pattern 预编译功能 */
    private static Pattern PUBLIC_CLASS_PATTERN = Pattern.compile("public class\\s+([_a-zA-Z][_a-zA-Z0-9]*)\\s*");

    public static String matchPublicClassName(String source){
        // 从源码字符串中匹配类名
        Matcher matcher = PUBLIC_CLASS_PATTERN.matcher(source);
        String className;
        if (matcher.find()) {
            className = matcher.group(1);
        } else {
            throw new IllegalArgumentException("No valid class");
        }
        return className;
    }

    public static Map<String, JavaFileObject> compile(String source, DiagnosticCollector<JavaFileObject> compileCollector) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        /**
         * 管理器，负责控制fileObjectMap中javaFileObject的放入和取出
         */
        JavaFileManager javaFileManager =
                new TmpJavaFileManager(compiler.getStandardFileManager(compileCollector, null, null));

        final String publicClassName = matchPublicClassName(source);

        // 把源码字符串构造成JavaFileObject，供编译使用
        JavaFileObject sourceJavaFileObject = new TmpJavaFileObject(publicClassName, source);

        Boolean result = compiler.getTask(null, javaFileManager, compileCollector,
                null, null, Arrays.asList(sourceJavaFileObject)).call();

        if (result && fileObjectMap != null) {
            return fileObjectMap;
        }

        return null;
    }

    /**
     * 管理JavaFileObject对象的工具
     */
    public static class TmpJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        protected TmpJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, JavaFileObject.Kind kind) throws IOException {
            JavaFileObject javaFileObject = fileObjectMap.get(className);
            if (javaFileObject == null) {
                return super.getJavaFileForInput(location, className, kind);
            }
            return javaFileObject;
        }

        /**
         * 第二步，编译器将编译好的字节码放到某个javafileobject中，但是放到哪个javafileobject呢，
         * 实际上，编译器工具类会调用getJavaFileForOutput方法，向fileObjectMap中new一个javafileobject，并将引用传出来，
         * 随后工具类调用javafileobject的openOutputStream方法，将字节码扔进去，因为是引用，这时fileObjectMap中就有一个类的javafileObject了
         *
         * 可见，一开始我们将代码放到javafileobject中，然后通过一系列调用，做成javafileobject，就行了
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            JavaFileObject javaFileObject = new TmpJavaFileObject(className, kind);
            fileObjectMap.put(className, javaFileObject);
            return javaFileObject;
        }
    }

    /**
     * 用来封装表示源码与字节码的对象
     */
    public static class TmpJavaFileObject extends SimpleJavaFileObject {
        private String source;
        private ByteArrayOutputStream outputStream;

        /**
         * 构造用来存储源代码的JavaFileObject
         * 需要传入源码source，然后调用父类的构造方法创建kind = Kind.SOURCE的JavaFileObject对象
         */
        public TmpJavaFileObject(String name, String source) {
            super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        /**
         * 构造用来存储字节码的JavaFileObject
         * 需要传入kind，即我们想要构建一个存储什么类型文件的JavaFileObject
         */
        public TmpJavaFileObject(String name, Kind kind) {
            super(URI.create("String:///" + name + Kind.SOURCE.extension), kind);
            this.source = null;
        }

        /**
         * 供内部调用
         * 第一步：将源代码从javafileobject中取出，之前已经将源代码封装成javafileobject了
         *        所以java编译器工具类利用这个函数取出来，编译成字节码
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            if (source == null) {
                throw new IllegalArgumentException("source == null");
            }
            return source;
        }


        /**
         * 供内部调用
         *  第三步：这就是第二步中的openOutputStream，
         *  工具类调用javafileobject的openOutputStream方法，将字节码扔进去
         */
        @Override
        public OutputStream openOutputStream() throws IOException {
            outputStream = new ByteArrayOutputStream();
            return outputStream;
        }

        /**
         * 外部调用
         * 当所有的代码都编译完成后，需要进行字节码替换时，需要取出来，然后做些修改后存成一个map
         */
        public byte[] getCompiledBytes() {
            return outputStream.toByteArray();
        }
    }
}
