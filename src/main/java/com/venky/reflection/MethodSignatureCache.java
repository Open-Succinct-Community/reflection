package com.venky.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.venky.cache.Cache;

public class MethodSignatureCache extends Cache<Method,String>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5591934380604234015L;

	public MethodSignatureCache(){
		super(1000,0.2);
	}
	private static String computeMethodSignature(Method method){
    	StringBuilder sign = new StringBuilder();
    	int modifiers = method.getModifiers();
		sign.append(Modifier.isPublic(modifiers) ? "public " : Modifier.isProtected(modifiers) ? "protected " : Modifier.isPrivate(modifiers)? "private " : "");
		sign.append(method.getReturnType().toString() + " ");
		sign.append(method.getName() + "(");
		Class<?>[] pt = method.getParameterTypes();
		for (int i = 0 ; i< pt.length ; i++ ){
			if (i > 0){
				sign.append(",");
			}
			sign.append(pt[i]);
		}
		sign.append(")");
		return sign.toString();
    }
    
	@Override
	protected String getValue(Method method) {
		return computeMethodSignature(method);
	}

}
