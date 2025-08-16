package com.github.allanhladki.flat2java.quarkus;

/**
 * Quarkus integration for Flat2Java.
 * 
 * <p>This class provides CDI producers for Flat2Java services, making them
 * available for dependency injection in Quarkus applications.</p>
 * 
 * <p>The integration:</p>
 * <ul>
 *   <li>Provides CDI producers for all Flat2Java interfaces</li>
 *   <li>Ensures proper bean scoping and lifecycle management</li>
 *   <li>Works seamlessly with Quarkus CDI implementation</li>
 * </ul>
 * 
 * @since 0.0.1-rc2
 */
public class Flat2JavaExtension {
    // This class serves as a marker for the Quarkus integration
    // The actual CDI producers are in Flat2JavaProducers class
}
