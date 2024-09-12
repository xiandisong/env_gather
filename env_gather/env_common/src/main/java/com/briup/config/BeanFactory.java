package com.briup.config;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author horry
 * @Description bean工厂，基于配置文件创建用户所需的bean对象，并对其进行初始化操作
 * @date 2024/8/29-9:20
 */
public class BeanFactory {

	// 维护与管理bean对象的容器
	private static final Map<String, Object> beanMap;
	// 维护bean对象中属性与属性值的联系的Map
	// fieldMap中key为bean对象的名称，Value为维护该bean对象中属性与属性值的关系的Map集合
	private static final Map<String, Map<String, String>> fieldMap;

	static {
		// 初始化容器
		beanMap = new HashMap<>();
		fieldMap = new HashMap<>();

		try {
			// 读取配置文件
			parseXML("bean.xml");
			// 完成依赖注入工作
			di();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 解析XML文件中的内容，并且创建bean对象
	 *
	 * @param path 文件路径
	 */
	private static void parseXML(String path) throws Exception {
		// 读取文件获取输入流
		InputStream in = BeanFactory.class.getClassLoader()
				.getResourceAsStream(path);
		// 创建SAXReader对象
		SAXReader saxReader = new SAXReader();
		// 获取xml文档中的内容，将其放入到Document对象中，形成文档树
		Document document = saxReader.read(in);
		// 根据document 对象获取根元素节点
		Element root = document.getRootElement();
		// 获取一级元素节点，一级元素节点就是我们所配置的bean所在区域
		List<Element> beanElements = root.elements();
		// 遍历所有节点
		for (Element beanElement : beanElements) {
			if (!"bean".equals(beanElement.getName()))
				continue;
			// 获取bean对象的名称，及其类路径
			String beanName = beanElement.attributeValue("name");
			String className = beanElement.attributeValue("class");
			// 通过反射以及类路径，给该类创建对象
			Class<?> aClass = Class.forName(className);
			Object o = aClass.newInstance();
			// 将bean对象维护在容器中
			beanMap.put(beanName, o);
			// 继续读取beanElement子节点（即bean的属性节点）
			List<Element> fieldElements = beanElement.elements();
			for (Element fieldElement : fieldElements) {
				if (!"property".equals(fieldElement.getName()))
					continue;
				// 获取属性名
				String fieldName = fieldElement.attributeValue("name");
				// 获取属性值
				String fieldValue = fieldElement.attributeValue("value");
				if (fieldValue == null) {
					// fieldValue获取的是bean对象在Map容器中的key
					fieldValue = fieldElement.attributeValue("ref");
				}
				// 将bean与其对应的属性及属性值的关系维护在fieldMap中
				Map<String, String> map;
				if (!fieldMap.containsKey(beanName)) {
					map = new HashMap<>();
				} else {
					map = fieldMap.get(beanName);
				}
				map.put(fieldName, fieldValue);
				fieldMap.put(beanName, map);
			}
		}
	}

	private static void di() throws Exception {
		// 遍历fieldMap集合，给所有需要设置属性值的bean对象完成属性设置的工作
		Set<Map.Entry<String, Map<String, String>>> entries = fieldMap.entrySet();
		for (Map.Entry<String, Map<String, String>> entry : entries) {
			// 获取bean对象的名称
			String beanName = entry.getKey();
			// 从beanMap中获取bean对象
			Object o = beanMap.get(beanName);
			// 获取bean对象中属性和属性值的关系
			Map<String, String> fieldValueMap = entry.getValue();
			// 遍历属性与属性值的集合
			Set<Map.Entry<String, String>> fieldEntries = fieldValueMap.entrySet();
			for (Map.Entry<String, String> fieldEntry : fieldEntries) {
				// 获取属性名
				String fieldName = fieldEntry.getKey();
				// 获取属性值
				String fieldValue = fieldEntry.getValue();
				// 给对应的对象设置属性值
				setFieldValue(o, fieldName, fieldValue);
			}
		}
	}

	private static void setFieldValue(Object o, String fieldName, String fieldValue) throws Exception {
		// 根据属性名获取对应的属性
		Class<?> aClass = o.getClass();
		Field field = aClass.getDeclaredField(fieldName);
		// 获取属性对应的set方法
		Class<?> type = field.getType();
		Method method = aClass.getDeclaredMethod(
				setMethodName(fieldName), type);
		// 设置属性值
		if (beanMap.containsKey(fieldValue)) {
			// 说明该属性值是在beanMap中的对象
			method.invoke(o, beanMap.get(fieldValue));
		} else {
			// 以int为例，设置非String类型的属性值时的操作
			if ("int".equals(type.getSimpleName()) ||
					"Integer".equals(type.getSimpleName())) {
				method.invoke(o, Integer.parseInt(fieldValue));
			} else {
				// 如果属性的类型是String，那么直接设置即可
				method.invoke(o, fieldValue);
			}
		}
	}

	private static String setMethodName(String fieldName) {
		return "set" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
	}


	/**
	 * 根据bean对象的名称获取bean对象
	 *
	 * @param beanName bean对象的名称
	 * @return bean对象
	 */
	public static Object getBean(String beanName) {
		return beanMap.get(beanName);
	}

	/**
	 * 根据bean对象的名称以及bean对象的所属类型，获取bean对象
	 *
	 * @param beanName bean对象名称
	 * @param tClass   bean对象类型的字节码对象
	 * @param <T>      bean对象的类型
	 * @return bean对象
	 */
	public static <T> T getBean(String beanName, Class<T> tClass) {
		Object bean = beanMap.get(beanName);
		if (tClass.isInstance(bean)) {
			return tClass.cast(bean);
		} else {
			throw new RuntimeException("Bean with name '" + beanName +
					"' is not of type " + tClass.getName());
		}
	}
}
