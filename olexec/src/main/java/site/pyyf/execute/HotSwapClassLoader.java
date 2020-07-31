package site.pyyf.execute;

import java.util.Map;

/**
 * 1. 首先应用类加载器加载所有的我们自己写的代码
 * 2. 当用户发来代码后，如Main1类，则我们的项目创建一个自定义类加载器进行加载
 * 3. 当遇到了arraylist这些类时，会通过super()的双亲委派机制交由应用类加载器和扩展类加载器，系统类加载器进行加载
 * 4. 当用户发来代码后，如Main2类，则我们的项目创建一个自定义类加载器进行加载
 * 5. 当遇到了arraylist这些类时，会通过super()的双亲委派机制交由应用类加载器和扩展类加载器，系统类加载器进行加载
 * 6. 这样就可以完成，
 */
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
        System.out.println("即将加载 "+name);
        return loadClass(name, false);
    }

    // 加载某个类，如它所使用的类没有加载，则继续加载未加载的类
    @Override
    protected Class<?> findClass(String name) {
        System.out.println("使用自定义类加载了 "+name);
        return defineClass(name, modifiedByte.get(name), 0, modifiedByte.get(name).length);
    }
    /**
     * 即将加载 Main
     * 使用自定义类加载了 Main
     * 即将加载 java.lang.Object
     * 即将加载 java.lang.String
     * 即将加载 site.pyyf.execute.HackSystem
     * 即将加载 java.util.Arrays
     * 即将加载 java.io.PrintStream
     *
     * 可以看出：
     *  1.用到谁加载谁
     *  2.自定义类加载器没加载系统的那些类
     *  3.当使用A类加载器加载一个类时，类中的子类的加载接口也为这个类
     *  4.不能代表系统类之前没加载过，只是要去搜一下
     *  5.其他类没有被new过，所以类相同，虚拟机相同，类加载器相同，这个条件没有被破坏，所以不能重复加载
     */
}
