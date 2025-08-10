package com.github.allanhladki.flat2java.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test demonstrating how to use the interceptor approach for exception translation.
 * This shows how to create a clean, annotation-based exception handling system.
 */
@DisplayName("Interceptor Example")
class InterceptorExampleTest {
    
    // Test interface for demonstrating the interceptor
    interface TestService extends com.github.allanhladki.flat2java.fixtures.TestFixtures.TestService {
        @TranslateExceptions(operationType = TranslateExceptions.OperationType.VALIDATION)
        boolean validateUserData(String data);
        
        @TranslateExceptions(operationType = TranslateExceptions.OperationType.SERIALIZATION)
        String serializeUserData(Object data);
        
        @TranslateExceptions(context = "processing user data", operationType = TranslateExceptions.OperationType.SERIALIZATION)
        String processUserData(String data);
    }
    
    // Test implementation
    static class TestServiceImpl implements TestService {
        @Override
        public boolean validateUserData(String data) {
            if (data == null) {
                throw new NullPointerException("Data cannot be null");
            }
            
            if (data.isEmpty()) {
                throw new IllegalArgumentException("Data cannot be empty");
            }
            
            if (data.length() > 100) {
                throw new IllegalArgumentException("Data length cannot exceed 100 characters");
            }
            
            return true;
        }
        
        @Override
        public String serializeUserData(Object data) {
            if (data == null) {
                throw new NullPointerException("Data cannot be null");
            }
            
            try {
                if (data instanceof String) {
                    return (String) data;
                } else {
                    return data.toString();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize data: " + e.getMessage(), e);
            }
        }
        
        @Override
        public String processUserData(String data) {
            if (data == null) {
                throw new NullPointerException("Data cannot be null");
            }
            
            if (data.contains("error")) {
                throw new RuntimeException("Processing failed due to error in data");
            }
            
            return "Processed: " + data;
        }
    }
    
    private TestService service;
    
    @BeforeEach
    void setUp() {
        // Create the service implementation
        TestServiceImpl impl = new TestServiceImpl();
        
        // Create an intercepted version that automatically translates exceptions
        service = ExceptionTranslationInterceptor.intercept(impl, TestService.class);
    }
    
    @Test
    @DisplayName("Should translate validation exceptions using interceptor")
    void testValidationExceptionTranslation() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.validateUserData(null);
        });
        
        assertTrue(exception.getMessage().contains("validateUserData"));
        assertTrue(exception.getMessage().contains("Data cannot be null"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }
    
    @Test
    @DisplayName("Should translate serialization exceptions using interceptor")
    void testSerializationExceptionTranslation() {
        SerializationException exception = assertThrows(SerializationException.class, () -> {
            service.serializeUserData(null);
        });
        
        assertTrue(exception.getMessage().contains("serializeUserData"));
        assertTrue(exception.getMessage().contains("Data cannot be null"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }
    
    @Test
    @DisplayName("Should translate processing exceptions using interceptor with custom context")
    void testProcessingExceptionTranslation() {
        SerializationException exception = assertThrows(SerializationException.class, () -> {
            service.processUserData(null);
        });
        
        assertTrue(exception.getMessage().contains("processing user data"));
        assertTrue(exception.getMessage().contains("Data cannot be null"));
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }
    
    @Test
    @DisplayName("Should handle validation errors with specific operation type")
    void testValidationErrorsWithSpecificOperationType() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            service.validateUserData("");
        });
        
        assertTrue(exception.getMessage().contains("validateUserData"));
        assertTrue(exception.getMessage().contains("Data cannot be empty"));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }
    
    @Test
    @DisplayName("Should handle serialization errors with specific operation type")
    void testSerializationErrorsWithSpecificOperationType() {
        SerializationException exception = assertThrows(SerializationException.class, () -> {
            service.serializeUserData(new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("Serialization failed");
                }
            });
        });
        
        assertTrue(exception.getMessage().contains("serializeUserData"));
        assertTrue(exception.getMessage().contains("Failed to serialize data"));
        assertInstanceOf(RuntimeException.class, exception.getCause());
    }
    
    @Test
    @DisplayName("Should work with valid input")
    void testValidInput() {
        boolean isValid = service.validateUserData("valid data");
        assertTrue(isValid);
        
        String serialized = service.serializeUserData("test data");
        assertEquals("test data", serialized);
        
        String processed = service.processUserData("valid input");
        assertEquals("Processed: valid input", processed);
    }
    
    @Test
    @DisplayName("Should preserve original exception details")
    void testPreservesOriginalExceptionDetails() {
        try {
            service.validateUserData("a".repeat(101)); // Exceeds 100 characters
            fail("Expected ValidationException");
        } catch (ValidationException e) {
            assertNotNull(e.getCause());
            assertInstanceOf(IllegalArgumentException.class, e.getCause());
            assertEquals("Data length cannot exceed 100 characters", e.getCause().getMessage());
        }
    }
} 