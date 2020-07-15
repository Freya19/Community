package site.pyyf.execute;

import java.util.Map;

public class HotSwapClassLoader extends ClassLoader {
    private Map<String, byte[]> modifiedByte;

    public HotSwapClassLoader(Map<String, byte[]> modifiedByte) {
        super(HotSwapClassLoader.class.getClassLoader());
        this.modifiedByte = modifiedByte;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println(name);
        return loadClass(name, false);
    }

    /*加载某个类，如它所使用的类没有加载，则继续加载未加载的类*/
    @Override
    protected Class<?> findClass(String name) {
        return defineClass(name, modifiedByte.get(name), 0, modifiedByte.get(name).length);
    }
}
