package com.venky.reflection;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class Reflector<U, C extends U> {
    protected final Class<C> reflectedClass;
    protected final Class<U> upperBoundClass;
    protected final List<Method> allMethods ;
    protected final List<Class<? extends U>> classHierarchy ;
    protected final List<Class<?>> classForest ;
    protected Reflector(Class<C> reflectedClass,Class<U> upperBoundClass){
    	assert(upperBoundClass != null);
    	assert(reflectedClass != null);
    	assert(upperBoundClass.isAssignableFrom(reflectedClass));

    	this.reflectedClass = reflectedClass ;
    	this.upperBoundClass = upperBoundClass;
    	this.allMethods = new ArrayList<Method>(reflectedClass.getMethods().length);
    	this.classHierarchy = new ArrayList<Class<? extends U>>();
    	this.classForest = new ArrayList<Class<?>>();
    	
        Class<? extends U> rClass = reflectedClass;
        List<Class<?>> interfaces = new ArrayList<Class<?>>();
    	do {
    		loadMethods(rClass);
			classHierarchy.add(rClass);
			classForest.add(rClass);

    		@SuppressWarnings("unchecked")
			Class<? extends U> parentClass = (Class<? extends U>)getParentClass(rClass,upperBoundClass);

    		for (Class<?> i : rClass.getInterfaces()){
    			if (i == parentClass){
    				continue;
    			}
        		interfaces.clear(); //Instead of new ArrayList each time.
    			loadAllInterfaces(i, interfaces);
        		for (Class<?> c : interfaces){
        			loadMethods(c);
        		}
        		classForest.addAll(interfaces);
    		}
    		
            rClass = parentClass;
        }while(rClass != null );
    	
    }
    
    
    private void loadMethods(Class<?> clazz){
    	int index = 0;
    	if (clazz == upperBoundClass){
    		index = allMethods.size();
    	}
    	for(Method m:getDeclaredMethods(clazz)){
        	List<Method> methodsForSignature = getMethodsForSignature(getMethodSignature(m));
        	if (methodsForSignature.isEmpty()){
    			allMethods.add(index,m);
        		index++;
        	}
        	methodsForSignature.add(m);
		}
    }
    
    
    public List<Class<? extends U>> getClassHierarchy(){ 
    	return classHierarchy;
    }
    
    public List<Class<?>> getClassForest(){
    	return classForest;
    }

    
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass){
    	return getAnnotation(annotationClass) != null; 
    }
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
    	T annotation = null;
    	for (Class<?> clazz:getClassForest()){
    		annotation = clazz.getAnnotation(annotationClass);
    		if (annotation != null){
    			break;
    		}
    	}
    	return annotation;
    }

    public boolean isAnnotationPresent(Method method, Class<? extends Annotation> annotationClass){
    	return getAnnotation(method,annotationClass) != null;
    }
    
    public <T extends Annotation> T getAnnotation(Method method,Class<T> annotationClass){
    	T annotation = null;
    	List<Method> methods = getMethodsForSignature(getMethodSignature(method)); 
    	for (int i = 0 ; annotation == null && i < methods.size() ; i ++){
    		Method m = methods.get(i);
    		annotation = m.getAnnotation(annotationClass);
    	}
    	return annotation;
    }
    
    private MethodSignatureCache signatureCache = new MethodSignatureCache(); 
    public String getMethodSignature(Method method){
    	return signatureCache.get(method);
    }

    private Map<String,List<Method>> methodsWithSameSignature = new HashMap<String, List<Method>>();
    protected List<Method> getMethodsForSignature(String signature){
    	List<Method> methods = methodsWithSameSignature.get(signature);
    	if (methods == null){
    		methods = new ArrayList<Method>();
    		methodsWithSameSignature.put(signature, methods);
    	}
    	return methods;
    }

    protected List<Method> getDeclaredMethods(Class<?> forClass){ 
        List<Method> methods = new ArrayList<Method>(); 
        methods.addAll(Arrays.asList(forClass.getDeclaredMethods()));
        try {
        	ClassLoader cl = forClass.getClassLoader();
        	if (cl != null){
        		InputStream is = cl.getResourceAsStream(forClass.getName().replace('.', '/')+ ".class");
        		if (is == null){
        			throw new IOException("Could not find class " +forClass.getName() + " as resource" );
        		}
	            ClassReader reader = new ClassReader(is);
	            ClassVisitorImpl mv = new ClassVisitorImpl();
	            reader.accept(mv, 0);
	
	            final Map<String,Integer> mSeq = mv.getMethodSequenceMap();
	            Collections.sort(methods,new Comparator<Method>(){
	                public int compare(Method o1, Method o2) {
	                    return mSeq.get(o1.getName()).compareTo(mSeq.get(o2.getName()));
	                }
	            });
        	}
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        return methods;
    }
    public Class<?> getParentClass(){
    	return getParentClass(reflectedClass);
    }
    public Class<?> getParentClass(Class<?> clazz){
    	int i = classHierarchy.indexOf(clazz);
    	if (i  >= 0 && i < classHierarchy.size() - 1){
    		return classHierarchy.get(i+1); 
    	}
    	return null;
    }

    private static class ClassVisitorImpl implements ClassVisitor {
        private Map<String, Integer> methodSequenceMap = new HashMap<String, Integer>();
        
        public Map<String, Integer> getMethodSequenceMap() {
            return methodSequenceMap;
        }

        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {
            
            
        }

        public void visitSource(String source, String debug) {
            
            
        }

        public void visitOuterClass(String owner, String name, String desc) {
            
            
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            
            return null;
        }

        public void visitAttribute(Attribute attr) {
            
            
        }

        public void visitInnerClass(String name, String outerName,
                String innerName, int access) {
            
            
        }

        public FieldVisitor visitField(int access, String name, String desc,
                String signature, Object value) {
            
            return null;
        }

        public MethodVisitor visitMethod(int access, String name, String desc,
                String signature, String[] exceptions) {
            
            methodSequenceMap.put(name,methodSequenceMap.size());
            return null;
        }

        public void visitEnd() {
            
            
        }
    }
    
    public final List<Method> getMethods(MethodMatcher matcher){
        List<Method> methods = new ArrayList<Method>();
        for (Method method:allMethods){
            if (matcher.matches(method)){
                methods.add(method);
            }
        }
        return methods;
    }
    

    public static interface MethodMatcher {
        public boolean matches(Method method);
    }

    private static void loadAllInterfaces(Class<?> clazz,List<Class<?>> interfaces){
    	if (clazz.isInterface()){
    		interfaces.add(clazz);
    	}
    	
    	for (Class<?> infcClass: clazz.getInterfaces()){
    		loadAllInterfaces(infcClass, interfaces);
    	}
    }
    
    public static Class<?> getParentClass(Class<?> clazz,Class<?> aSuperInfcOrClass){
    	
    	Class<?> c = clazz;
    	if (aSuperInfcOrClass != null && !aSuperInfcOrClass.isAssignableFrom(c)){
    		throw new RuntimeException(c.getName() + " is not a "+ aSuperInfcOrClass.getName());
    	}
    	if (c.isInterface()){
        	List<Class<?>> interfaces =  new ArrayList<Class<?>>();
        	for (Class<?> infc: c.getInterfaces()){
        		if (aSuperInfcOrClass == null || aSuperInfcOrClass.isAssignableFrom(infc)){
        			interfaces.add(infc);
        		}
        		if (interfaces.size() > 1){
        			break;
        		}
        	}
        	if (interfaces.isEmpty()){
        		return null;
        	}else if (interfaces.size() > 1){
        		throw new RuntimeException ("multiple interfaces implement " + aSuperInfcOrClass.getName());
        	}else {
        		return interfaces.get(0);
        	}    		
    	}else {
    		c = c.getSuperclass();
        	if (c != null && (aSuperInfcOrClass == null || aSuperInfcOrClass.isAssignableFrom(c))){
        		return c;
        	}else {
        		return null;
        	}
    	}
    }
}
