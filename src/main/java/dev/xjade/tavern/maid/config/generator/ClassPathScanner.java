package dev.xjade.tavern.maid.config.generator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

// Made with the help of AI. Cause it's complicated and not worth the time for what it does.
public class ClassPathScanner {
  public static List<Class<?>> getClasses(String packageName) throws Exception {
    List<Class<?>> classes = new ArrayList<>();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

    while (resources.hasMoreElements()) {
      File directory = new File(resources.nextElement().getFile());
      if (directory.isDirectory()) {
        for (String file : directory.list()) {
          if (file.endsWith(".class")) {
            String className = packageName + '.' + file.substring(0, file.length() - 6);
            classes.add(Class.forName(className));
          }
        }
      }
    }
    return classes;
  }
}
