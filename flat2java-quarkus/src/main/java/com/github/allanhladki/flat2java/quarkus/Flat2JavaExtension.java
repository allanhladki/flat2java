package com.github.allanhladki.flat2java.quarkus;

/**
 * Quarkus integration marker for Flat2Java.
 * 
 * <p>This class serves as a marker for the Quarkus integration.
 * The actual CDI beans are provided by the {@link Flat2JavaProducers} class.</p>
 * 
 * <p>The integration provides:</p>
 * <ul>
 *   <li>CDI producers for all Flat2Java interfaces</li>
 *   <li>Automatic bean registration in Quarkus applications</li>
 *   <li>Dependency injection support for serialization services</li>
 * </ul>
 * 
 * @since 0.0.1-rc2
 */
public class Flat2JavaExtension {
    // This class serves as a marker for the Quarkus integration
    // The actual CDI beans are in Flat2JavaProducers class
}
