package io.github.allanhladki.flat2java.quarkus;

/**
 * Quarkus integration marker for Flat2Java.
 *
 * <p>This class serves as a marker for the Quarkus integration. The actual CDI beans are provided
 * by the {@link Flat2JavaProducers} class.
 *
 * <p>The integration provides:
 *
 * <ul>
 *   <li>CDI producers for all Flat2Java interfaces
 *   <li>Automatic bean registration in Quarkus applications
 *   <li>Dependency injection support for serialization services
 * </ul>
 *
 * @since 0.0.1-rc2
 */
public class Flat2JavaExtension {
    // This class serves as a marker for the Quarkus integration
    // The actual CDI beans are in Flat2JavaProducers class
}
