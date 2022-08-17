package ca.discotek.deepdive.security.gui;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class NodeRegistry {

    public static final String EAR_NODE = "ear.node";
    public static final String EJB_JAR_NODE = "ejb.jar.node";
    public static final String WAR_NODE = "war.node";
    public static final String JAR_NODE = "jar.node";
    public static final String CLASSFILE_NODE = "classfile.node";
    public static final String APPLICATION_XML_NODE = "application.xml.node";
    public static final String EJB_JAR_XML_NODE = "ejb.jar.xml.node";
    public static final String MANIFEST_NODE = "manifest.node";
    public static final String MODULES_NODE = "modules.node";
    public static final String WEB_XML_NODE = "web.xml.node";
    public static final String LIBS_NODE = "libs.node";
    public static final String CSS_NODE = "css.node";
    public static final String JAVASCRIPT_NODE = "javascript.node";
    public static final String IMAGE_NODE = "image.node";
    public static final String JSP_NODE = "jsp.node";
    public static final String HTML_NODE = "html.node";
    public static final String WEB_INF_NODE = "web-inf.node";
    public static final String WEB_INF_CLASSES_NODE = "web-inf-classes.node";
    public static final String APK_NODE = "apk.node";
    public static final String CLASSES_DEX_NODE = "classes-dex.node";

    public static final Class DEFAULT_APK_NODE_CLASS = ApkNode.class;
    public static final Class DEFAULT_CLASSES_DEX_NODE_CLASS = ClassesDexNode.class;
    public static final Class DEFAULT_EAR_NODE_CLASS = EarNode.class;
    public static final Class DEFAULT_EJB_JAR_NODE_CLASS = JarNode.class;
    public static final Class DEFAULT_WAR_NODE_CLASS = WarNode.class;
    public static final Class DEFAULT_JAR_NODE_CLASS = JarNode.class;
    public static final Class DEFAULT_CLASSFILE_NODE_CLASS = ClassFileNode.class;
    public static final Class DEFAULT_APPLICATION_XML_NODE_CLASS = ApplicationXmlNode.class;
    public static final Class DEFAULT_EJB_JAR_XML_NODE_CLASS = EjbJarXmlNode.class;
    public static final Class DEFAULT_MANIFEST_NODE_CLASS = ManifestNode.class;
    public static final Class DEFAULT_MODULES_NODE_CLASS = ModulesNode.class;
    public static final Class DEFAULT_WEB_XML_NODE_CLASS = WebXmlNode.class;
    public static final Class DEFAULT_LIBS_NODE_CLASS = LibsNode.class;
    public static final Class DEFAULT_CSS_NODE_CLASS = CssNode.class;
    public static final Class DEFAULT_JAVASCRIPT_NODE_CLASS = JavascriptNode.class;
    public static final Class DEFAULT_IMAGE_NODE_CLASS = ImagesNode.class;
    public static final Class DEFAULT_JSP_NODE_CLASS = JspsNode.class;
    public static final Class DEFAULT_HTML_NODE_CLASS = HtmlNode.class;
    public static final Class DEFAULT_WEB_INF_NODE_CLASS = WebInfNode.class;
    public static final Class DEFAULT_WEB_INF_CLASSES_NODE_CLASS = WebInfClassesNode.class;
    

    static Map<String, Class> map = new HashMap<String, Class>();

    
    static {
        registerNodeClass(APK_NODE, DEFAULT_APK_NODE_CLASS);
        registerNodeClass(CLASSES_DEX_NODE, DEFAULT_CLASSES_DEX_NODE_CLASS);
        registerNodeClass(EAR_NODE, DEFAULT_EAR_NODE_CLASS);
        registerNodeClass(EJB_JAR_NODE, DEFAULT_EJB_JAR_NODE_CLASS);
        registerNodeClass(WAR_NODE, DEFAULT_WAR_NODE_CLASS);
        registerNodeClass(JAR_NODE, DEFAULT_JAR_NODE_CLASS);
        registerNodeClass(CLASSFILE_NODE, DEFAULT_CLASSFILE_NODE_CLASS);
        registerNodeClass(APPLICATION_XML_NODE, DEFAULT_APPLICATION_XML_NODE_CLASS);
        registerNodeClass(EJB_JAR_XML_NODE, DEFAULT_EJB_JAR_XML_NODE_CLASS);
        registerNodeClass(MANIFEST_NODE, DEFAULT_MANIFEST_NODE_CLASS);
        registerNodeClass(MODULES_NODE, DEFAULT_MODULES_NODE_CLASS);
        registerNodeClass(WEB_XML_NODE, DEFAULT_WEB_XML_NODE_CLASS);
        registerNodeClass(LIBS_NODE, DEFAULT_LIBS_NODE_CLASS);
        registerNodeClass(CSS_NODE, DEFAULT_CSS_NODE_CLASS);
        registerNodeClass(HTML_NODE, DEFAULT_HTML_NODE_CLASS);
        registerNodeClass(JAVASCRIPT_NODE, DEFAULT_JAVASCRIPT_NODE_CLASS);
        registerNodeClass(JSP_NODE, DEFAULT_JSP_NODE_CLASS);
        registerNodeClass(IMAGE_NODE, DEFAULT_IMAGE_NODE_CLASS);
        registerNodeClass(WEB_INF_NODE, DEFAULT_WEB_INF_NODE_CLASS);
        registerNodeClass(WEB_INF_CLASSES_NODE, DEFAULT_WEB_INF_CLASSES_NODE_CLASS);
    }

    public static void registerNodeClass(String nodeName, Class nodeClass) {
        map.put(nodeName, nodeClass);
    }
    
    public static Class getNodeClass(String nodeName) {
        return map.get(nodeName);
    }

    public static Object getNodeObject(String type) {
        try {
            Class c = NodeRegistry.getNodeClass(type);
            return c.newInstance();
        } 
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Object getNodeObject(String type, Class paramType, Object argument) {
        return getNodeObject(type, new Class[]{paramType}, new Object[]{argument});
    }
    
    public static Object getNodeObject(String type, Class paramTypes[], Object arguments[]) {
        try {
            Class c = NodeRegistry.getNodeClass(type);
            Constructor constructor =  c.getConstructor(paramTypes);
            return constructor.newInstance(arguments);
        } 
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
