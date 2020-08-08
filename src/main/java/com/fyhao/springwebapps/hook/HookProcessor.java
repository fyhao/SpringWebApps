package com.fyhao.springwebapps.hook;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    public void executeHookCC(String methodName, Conversation conversation, String input) {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(HookCC.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            try {
                Method method = entry.getValue().getClass().getDeclaredMethod(methodName, Conversation.class, String.class);
                if (method != null) {
                    method.invoke(entry.getValue(), conversation, input);
                }
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
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