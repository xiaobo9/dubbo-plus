package net.dubboclub.restful.util;

import com.alibaba.dubbo.common.utils.StringUtils;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class ContainerPortUtils {

    private static final String HTTP_PROTOCOL = "HTTP/1.1";

    public static String getPort() {
        String port = getTasPortValue();
        if (StringUtils.isBlank(port)) {
            port = getTomcatPortValue();
        }
        return port;
    }

    /**
     * @return tas 的端口
     */
    private static String getTasPortValue() {
        try {
            Class<?> localTasLocatorClazz = ContainerPortUtils.class.getClassLoader().getParent()
                    .loadClass("com.thunisoft.tas.api.locator.LocalTasLocator");
            if (localTasLocatorClazz != null) {
                Object localTasLocator = localTasLocatorClazz.newInstance();
                Method initTasLocatorMethod = localTasLocator.getClass().getDeclaredMethod("initTasLocator");
                initTasLocatorMethod.setAccessible(true);
                initTasLocatorMethod.invoke(localTasLocator);
                Object serverService = getBeanProperties(localTasLocator, "serverService");
                Method getPortInfoMethod = serverService.getClass().getDeclaredMethod("getPort", String.class);
                Object portInfo = getPortInfoMethod.invoke(serverService, "web_http");
                return toString(getBeanProperties(portInfo, "port"));
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static Object getBeanProperties(Object bean, String name)
            throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (bean == null) {
            throw new IllegalArgumentException("No bean specified");
        }
        if (name == null) {
            throw new IllegalArgumentException("No name specified for bean class '" + bean.getClass() + "'");
        }
        PropertyDescriptor descriptor = getPropertyDescriptors(bean.getClass(), name);
        if (descriptor == null || descriptor.getReadMethod() == null) {
            throw new NoSuchMethodException("Unknown property '" + name + "' on class '" + bean.getClass() + "'");
        }
        return descriptor.getReadMethod().invoke(bean);
    }

    private static PropertyDescriptor getPropertyDescriptors(Class<?> beanClass, String name) {
        if (beanClass == null) {
            throw new IllegalArgumentException("No bean class specified");
        }
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            return null;
        }
        PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
        if (descriptors == null) {
            descriptors = new PropertyDescriptor[0];
        }
        for (PropertyDescriptor descriptor : descriptors) {
            if (name.equals(descriptor.getName())) {
                return descriptor;
            }
        }
        return null;
    }

    /**
     * 尝试获取 tomcat 端口
     *
     * @return tomcat port value
     */
    private static String getTomcatPortValue() {
        try {
            List<MBeanServer> serverList = MBeanServerFactory.findMBeanServer(null);
            for (MBeanServer server : serverList) {
                Set<ObjectName> objectNames = server.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
                for (ObjectName name : objectNames) {
                    String protocol = (String) server.getAttribute(name, "protocol");
                    if (StringUtils.isEquals(HTTP_PROTOCOL, protocol)) {
                        return toString(server.getAttribute(name, "port"));
                    }
                }
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
}
