package com.github.allanhladki.flat2java.exception;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that automatically translates exceptions for methods annotated with {@link
 * TranslateExceptions}. This class provides a clean way to add exception translation without
 * modifying the original method code.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * MyService service = new MyService();
 * MyService interceptedService = ExceptionTranslationInterceptor.intercept(service, MyService.class);
 *
 * // Now all methods annotated with @TranslateExceptions will have their exceptions translated
 * String result = interceptedService.serialize("value");
 * }</pre>
 */
public class ExceptionTranslationInterceptor {

    private static final Logger logger =
            LoggerFactory.getLogger(ExceptionTranslationInterceptor.class);

    /**
     * Creates a proxy that intercepts method calls and translates exceptions for methods annotated
     * with {@link TranslateExceptions}.
     *
     * @param <T> the type of the service
     * @param target the target object to intercept
     * @param serviceInterface the interface that the target implements
     * @return a proxy that translates exceptions
     */
    @SuppressWarnings("unchecked")
    public static <T> T intercept(T target, Class<T> serviceInterface) {
        return (T)
                Proxy.newProxyInstance(
                        serviceInterface.getClassLoader(),
                        new Class<?>[] {serviceInterface},
                        new ExceptionTranslationInvocationHandler(target));
    }

    /**
     * Checks if an object is a proxy created by this interceptor.
     *
     * @param obj the object to check
     * @return true if the object is a proxy, false otherwise
     */
    public static boolean isProxy(Object obj) {
        return obj != null && Proxy.isProxyClass(obj.getClass());
    }

    /**
     * Wraps a method execution with exception translation if the method is annotated with {@link
     * TranslateExceptions}. This is a utility method that can be used directly without creating a
     * proxy.
     *
     * @param <T> the return type
     * @param target the target object
     * @param method the method to execute
     * @param args the method arguments
     * @return the method result
     * @throws Flat2JavaException if the method throws an exception that gets translated
     */
    @SuppressWarnings("unchecked")
    public static <T> T executeWithTranslation(Object target, Method method, Object... args) {
        // First try to get annotation from the interface method (for proxy calls)
        TranslateExceptions annotation = method.getAnnotation(TranslateExceptions.class);

        // If not found on interface method, try implementation method
        if (annotation == null) {
            try {
                Method targetMethod =
                        target.getClass().getMethod(method.getName(), method.getParameterTypes());
                annotation = targetMethod.getAnnotation(TranslateExceptions.class);
            } catch (NoSuchMethodException e) {
                // Method not found on target, keep annotation as null
            }
        }

        if (annotation == null) {
            // No annotation, execute normally
            try {
                return (T) method.invoke(target, args);
            } catch (Exception e) {
                throw new RuntimeException("Method execution failed", e);
            }
        }

        // Get context from annotation or use method name
        String context = annotation.context().isEmpty() ? method.getName() : annotation.context();

        try {
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            // Get the actual exception that was thrown
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }

            // Translate the exception based on the annotation configuration
            Flat2JavaException translated =
                    translateException(cause, context, annotation.operationType());
            throw translated;
        }
    }

    /**
     * Translates an exception based on the operation type specified in the annotation.
     *
     * @param exception the exception to translate
     * @param context the context for the error message
     * @param operationType the type of operation being performed
     * @return the translated exception
     */
    private static Flat2JavaException translateException(
            Throwable exception, String context, TranslateExceptions.OperationType operationType) {
        logger.debug(
                "Translating exception for context '{}' with operation type '{}': {}",
                context,
                operationType,
                exception.getMessage());

        if (exception instanceof Flat2JavaException) {
            Flat2JavaException flat2JavaException = (Flat2JavaException) exception;
            return flat2JavaException;
        }

        String message = String.format("Error %s: %s", context, exception.getMessage());

        // Determine the target exception type based on operation type
        if (operationType == TranslateExceptions.OperationType.VALIDATION) {
            return new ValidationException(message, exception);
        } else if (operationType == TranslateExceptions.OperationType.DESERIALIZATION) {
            return new DeserializationException(message, exception);
        } else {
            // SERIALIZATION is the default for other operation types
            return new SerializationException(message, exception);
        }
    }

    /** Invocation handler that intercepts method calls and translates exceptions. */
    private static class ExceptionTranslationInvocationHandler implements InvocationHandler {

        private final Object target;

        public ExceptionTranslationInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return executeWithTranslation(target, method, args);
        }
    }
}
