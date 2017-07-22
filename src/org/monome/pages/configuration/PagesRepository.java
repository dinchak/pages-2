package org.monome.pages.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import org.monome.pages.pages.BasePage;
import org.monome.pages.pages.Page;

public class PagesRepository {

    static Collection<Class<? extends BasePage>> pageTypes;

    static {
        try {
            loadPages();
        } catch (Exception e) {
            System.out.println("PagesRepository: couldn't load page implementations.");
            e.printStackTrace();
        }

    }

    public static String[] getPageNames(Class<? extends BasePage> pageClass) {
        int numPages = 0;
        for (Class<?> clz : pageTypes) {
            if (pageClass.isAssignableFrom(clz)) {
                numPages++;
            }
        }
        String[] res = new String[numPages];
        int i = 0;
        for (Class<?> clz : pageTypes) {
            if (pageClass.isAssignableFrom(clz)) {
                res[i++] = clz.getName();
            }
        }
        return res;
    }

    static <TPage extends BasePage> TPage getPageInstance(Class<TPage> pageClass, String name, DeviceConfiguration conf, int index) {
        TPage page;
        for (Class<? extends BasePage> clz : pageTypes) {
            try {
                Class<? extends TPage> clzTPage = clz.asSubclass(pageClass);
                if (clzTPage.getName().equals(name)) {
                    try {
                        Constructor<?>[] ctors = clzTPage.getConstructors();
                        for (Constructor<?> ctor : ctors) {
                            Class<?>[] params = ctor.getParameterTypes();
                            if (params.length == 2
                                    && params[1] == int.class
                                    && params[0].isAssignableFrom(conf.getClass())) {
                                page = (TPage)ctor.newInstance(conf, index);
                                return page;
                            }
                        }
                        Constructor<? extends TPage> ctor = clzTPage.getConstructor(conf.getClass(), int.class);
                        page = ctor.newInstance(conf, index);
                        return page;
                    } catch (Exception e) {
                        System.out.println("PagesRepository: Failed to create page with name " + name + " on index " + index);
                        e.printStackTrace();
                    }
                }
            } catch (ClassCastException ex) {
                continue;
            }
        }
        return null;
    }

    public static void loadPages() throws IOException {
        ClassLoader ldr = Thread.currentThread().getContextClassLoader();
        Collection<Class<? extends BasePage>> pages = new ArrayList<Class<? extends BasePage>>();

        Enumeration<URL> e = ldr.getResources("META-INF/services/" + Page.class.getName());

        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            InputStream is = url.openStream();

            try {

                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (true) {
                    String line = r.readLine();
                    if (line == null)
                        break;
                    int comment = line.indexOf('#');
                    if (comment >= 0)
                        line = line.substring(0, comment);
                    String name = line.trim();
                    if (name.length() == 0)
                        continue;

                    Class<?> clz = Class.forName(name, true, ldr);

                    if (BasePage.class.isAssignableFrom(clz)) {
                        pages.add(clz.asSubclass(BasePage.class));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (Exception ex) {
                }
                //do nothing
            }
        }
        pageTypes = pages;
    }

}
