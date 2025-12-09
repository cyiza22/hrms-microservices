package api.example.employeeservice.factory;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Purpose: Selects the appropriate factory at runtime based on employee type
 */
@Component
public class EmployeeFactoryProvider {

    private final Map<String, EmployeeFactory> factories;

    /**
     * Constructor injection of all EmployeeFactory implementations
     * Spring automatically finds all beans implementing EmployeeFactory
     */
    public EmployeeFactoryProvider(List<EmployeeFactory> factoryList) {
        this.factories = factoryList.stream()
                .collect(Collectors.toMap(
                        EmployeeFactory::getEmployeeType,
                        Function.identity()
                ));
    }

    /**
     * Gets the appropriate factory for the given employee type
     * @param type Employee type (PERMANENT, CONTRACT, etc.)
     * @return Factory for that type
     * @throws IllegalArgumentException if type is not supported
     */
    public EmployeeFactory getFactory(String type) {
        EmployeeFactory factory = factories.get(type.toUpperCase());
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported employee type: " + type);
        }
        return factory;
    }
}