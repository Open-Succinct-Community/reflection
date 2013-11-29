package com.venky.reflection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.venky.cache.Cache;

public class MethodSignatureCache {
	
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
	Cache<Method,String> signatures = new Cache<Method,String>() {

		private static final long serialVersionUID = 3032669256312714388L;

		@Override
		protected String getValue(Method k) {
			return computeMethodSignature(k);
		}
	};
	Boolean methodHashCodeHasHighCollisions = null;
	public String get(Method method) {
		if (methodHashCodeHasHighCollisions == null){
			methodHashCodeHasHighCollisions = (method.hashCode() == method.getName().hashCode()); //Android Code has this kind of code.
		}
		if (methodHashCodeHasHighCollisions) {
			//Don't cache. hash collisions would result in more computations of the signatures. 
			return computeMethodSignature(method);
		}else {
			return signatures.get(method);
		}
	}

}
