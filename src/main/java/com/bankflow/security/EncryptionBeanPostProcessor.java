package com.bankflow.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
@RequiredArgsConstructor
@Slf4j
public class EncryptionBeanPostProcessor implements BeanPostProcessor {

    private final EncryptionService encryptionService;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        try {
            Class<?> beanClass = bean.getClass();

            if (isSpringFrameworkClass(beanClass)) {
                return bean;
            }

            Field[] fields = beanClass.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(Encrypted.class)) {
                    Encrypted encryptedAnnotation = field.getAnnotation(Encrypted.class);
                    String description = encryptedAnnotation.value();

                    field.setAccessible(true);

                    try {
                        Object value = field.get(bean);

                        if (value instanceof String plaintext) {

                            if (!plaintext.isEmpty() && !encryptionService.isEncrypted(plaintext)) {

                                String encrypted = encryptionService.encrypt(plaintext);
                                field.set(bean, encrypted);

                                log.info("AUDIT: Field encrypted during bean initialization - Bean: {}, Field: {}, Description: {}",
                                        beanName, field.getName(), description);
                            }
                        }
                    } catch (IllegalAccessException ex) {
                        log.error("AUDIT: Failed to access field for encryption - Bean: {}, Field: {}, Error: {}",
                                beanName, field.getName(), ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("AUDIT: BeanPostProcessor failed to process bean: {} - Error: {}",
                    beanName, ex.getMessage());
            throw new BeansException("Failed to process bean: " + beanName, ex) {};
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private boolean isSpringFrameworkClass(Class<?> clazz) {
        String className = clazz.getName();
        return className.startsWith("org.springframework.") ||
               className.startsWith("org.hibernate.") ||
               className.startsWith("java.") ||
               className.startsWith("javax.");
    }
}

