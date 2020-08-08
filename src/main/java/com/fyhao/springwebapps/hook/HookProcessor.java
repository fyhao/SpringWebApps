package com.fyhao.springwebapps.hook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import com.fyhao.springwebapps.entity.Conversation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class HookProcessor {
    static Logger logger = LoggerFactory.getLogger(HookProcessor.class);
    @Autowired
    ApplicationContext applicationContext;

    public void execute(Class clazz, String methodName, Object... objs) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(clazz);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            try {
                Method[] methods = entry.getValue().getClass().getDeclaredMethods();
                for(Method method : methods) {
                    if(method.getName().equals(methodName)) {
                        Type[] types = method.getParameterTypes();
                        boolean allMatched = true;
                        for(int i = 0; i < types.length; i++) {
                            Type type = types[i];
                            if(!type.getTypeName().equals(objs[i].getClass().getName())) {
                                allMatched = false;
                            }
                        }
                        if(allMatched) {
                            method.invoke(entry.getValue(), objs);
                        }
                    }
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}