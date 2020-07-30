package site.pyyf.execute;

import java.util.Map;

public class HotSwapClassLoader extends ClassLoader {
    private Map<String, byte[]> modifiedByte;

    public HotSwapClassLoader(Map<String, byte[]> modifiedByte) {
        // 这里将应用类加载器作为父加载器，使得原本的双亲委派机制得以保持，同时，无法找到的类，通过这里的自定义类加载器进行加载
        super(HotSwapClassLoader.class.getClassLoader());
        this.modifiedByte = modifiedByte;
    }

    // loadClass -> findClass -> defineClass
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    // 加载某个类，如它所使用的类没有加载，则继续加载未加载的类
    @Override
    protected Class<?> findClass(String name) {
        return defineClass(name, modifiedByte.get(name), 0, modifiedByte.get(name).length);
    }
}
