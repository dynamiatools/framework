package tools.dynamia.modules.saas.api;

/**
 * Interface for calculating quantities of additional services consumed by an account.
 * <p>
 * This interface enables modules to define custom metrics or usage calculators that can be tracked
 * per account. These calculations can be used for billing purposes, usage analytics, or quota enforcement.
 * <p>
 * Common use cases include:
 * <ul>
 *   <li>Calculating storage space used</li>
 *   <li>Counting API calls or transactions</li>
 *   <li>Measuring bandwidth consumption</li>
 *   <li>Tracking feature usage</li>
 * </ul>
 * <p>
 * Implementations should be registered as Spring beans and will be automatically discovered
 * by the SaaS module for service quantity tracking.
 * <p>
 * Example usage:
 * <pre>{@code
 * @Component
 * public class StorageCalculator implements AccountServiceQuantityCalculator {
 *     @Autowired
 *     private FileRepository fileRepository;
 *
 *     public String getId() {
 *         return "storage-used";
 *     }
 *
 *     public String getName() {
 *         return "Storage Used (MB)";
 *     }
 *
 *     public long calculate(Long accountId) {
 *         return fileRepository.getTotalSizeByAccount(accountId) / (1024 * 1024);
 *     }
 * }
 * }</pre>
 *
 * @author Mario Serrano Leones
 */
public interface AccountServiceQuantityCalculator {

    /**
     * Returns the unique identifier for this quantity calculator.
     * This ID is used to reference the calculator throughout the system.
     *
     * @return the calculator identifier (e.g., "storage-used", "api-calls")
     */
    String getId();

    /**
     * Returns the human-readable name of this quantity calculator.
     * This name is typically displayed in reports and billing statements.
     *
     * @return the calculator display name
     */
    String getName();

    /**
     * Calculates the current quantity for the specified account.
     * <p>
     * This method should perform any necessary queries or calculations to determine
     * the current usage or quantity value for the given account.
     *
     * @param accountId the unique identifier of the account
     * @return the calculated quantity value
     */
    long calculate(Long accountId);
}
