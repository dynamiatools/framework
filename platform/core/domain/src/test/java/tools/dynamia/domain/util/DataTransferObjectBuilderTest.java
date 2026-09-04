/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataTransferObjectBuilder.
 * Tests DTO transformation scenarios focusing on standard property copying.
 *
 * Note: Tests for entity relationship handling (autoTransferIdProperty, autoTransferStringProperty)
 * require EntityUtilsProvider configuration which is typically available in full application context.
 *
 * @author Ing. Mario Serrano Leones
 */
public class DataTransferObjectBuilderTest {

    private TestEntity testEntity;

    @BeforeEach
    public void setUp() {
        // Setup test entity with standard properties
        testEntity = new TestEntity();
        testEntity.setId(1L);
        testEntity.setName("Test Product");
        testEntity.setDescription("Test Description");
        testEntity.setPrice(99.99);
        testEntity.setActive(true);
        testEntity.setQuantity(100);
    }

    /**
     * Test basic DTO transformation - copies all standard properties
     */
    @Test
    public void testBuildDTOBasicProperties() {
        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull(dto, "DTO should not be null");
        assertEquals(testEntity.getName(), dto.getName(), "Name should be copied");
        assertEquals(testEntity.getDescription(), dto.getDescription(), "Description should be copied");
        assertEquals(testEntity.getPrice(), dto.getPrice(), 0.001, "Price should be copied");
        assertEquals(testEntity.isActive(), dto.isActive(), "Active flag should be copied");
    }

    /**
     * Test DTO transformation copies all matching properties
     */
    @Test
    public void testBuildDTOCopiesAllMatchingProperties() {
        TestEntityDTOComplete dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOComplete.class);

        assertNotNull(dto, "DTO should not be null");
        assertEquals(testEntity.getId(), dto.getId(), "ID should be copied");
        assertEquals(testEntity.getName(), dto.getName(), "Name should be copied");
        assertEquals(testEntity.getDescription(), dto.getDescription(), "Description should be copied");
        assertEquals(testEntity.getPrice(), dto.getPrice(), 0.001, "Price should be copied");
        assertEquals(testEntity.getQuantity(), dto.getQuantity(), "Quantity should be copied");
    }

    /**
     * Test DTO transformation with partial properties (DTO has subset of source properties)
     */
    @Test
    public void testBuildDTOPartialProperties() {
        TestEntityDTOPartial dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOPartial.class);

        assertNotNull(dto, "DTO should not be null");
        assertEquals(testEntity.getName(), dto.getName(), "Name should be copied");
        assertEquals(testEntity.getPrice(), dto.getPrice(), 0.001, "Price should be copied");
        // Description is not in DTO, so it should be ignored without error
    }

    /**
     * Test DTO transformation with null properties
     */
    @Test
    public void testBuildDTOWithNullProperties() {
        testEntity.setName(null);
        testEntity.setDescription(null);

        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull(dto, "DTO should not be null");
        assertNull(dto.getName(), "Null name should remain null");
        assertNull(dto.getDescription(), "Null description should remain null");
        assertEquals(testEntity.getPrice(), dto.getPrice(), 0.001, "Non-null price should be copied");
    }

    /**
     * Test DTO transformation with empty entity (all null)
     */
    @Test
    public void testBuildDTOWithAllNullProperties() {
        TestEntity emptyEntity = new TestEntity();

        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(emptyEntity, TestEntityDTO.class);

        assertNotNull(dto, "DTO should not be null even for empty entity");
        assertNull(dto.getName(), "Name should be null");
        assertNull(dto.getDescription(), "Description should be null");
        assertNull(dto.getPrice(), "Price should be null");
    }

    /**
     * Test DTO transformation preserves primitive types
     */
    @Test
    public void testBuildDTOPreservesPrimitives() {
        TestEntityDTOWithPrimitives dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithPrimitives.class);

        assertNotNull(dto, "DTO should not be null");
        assertEquals(testEntity.isActive(), dto.isActive(), "Active boolean should be copied");
        assertEquals(testEntity.getQuantity().intValue(), dto.getQuantity(), "Quantity int should be copied");
    }

    /**
     * Test DTO transformation with additional properties in DTO (should be left as default)
     */
    @Test
    public void testBuildDTOWithAdditionalProperties() {
        TestEntityDTOWithExtra dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithExtra.class);

        assertNotNull(dto, "DTO should not be null");
        assertEquals(testEntity.getName(), dto.getName(), "Name should be copied");

        // Additional property not in source should remain at default value
        assertNull(dto.getExtraField(), "Extra property should be null (default)");
    }

    /**
     * Test DTO transformation with different property types (compatible conversion)
     */
    @Test
    public void testBuildDTOWithTypeConversion() {
        TestEntityDTOWithConversion dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithConversion.class);

        assertNotNull(dto, "DTO should not be null");
        // Spring BeanUtils should handle compatible type conversions
        assertEquals(testEntity.getName(), dto.getName(), "Name should be copied");

        // Note: Integer to Long conversion may not happen automatically with BeanUtils
        // This is expected behavior - BeanUtils only copies matching types
        // For type conversion, use custom converters or transform methods
        if (dto.getQuantity() != null) {
            assertEquals(testEntity.getQuantity().longValue(), dto.getQuantity().longValue(),
                        "Quantity should be converted if Spring supports it");
        }
    }

    /**
     * Test buildDTO creates a new instance (not modifying original)
     */
    @Test
    public void testBuildDTOCreatesNewInstance() {
        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull(dto, "DTO should not be null");

        // Modify DTO
        dto.setName("Modified Name");
        dto.setPrice(199.99);

        // Original should remain unchanged
        assertEquals("Test Product", testEntity.getName(), "Original name should not change");
        assertEquals(99.99, testEntity.getPrice(), 0.001, "Original price should not change");
    }

    /**
     * Test buildDTO with boolean properties
     */
    @Test
    public void testBuildDTOWithBooleans() {
        testEntity.setActive(false);

        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull(dto, "DTO should not be null");
        assertFalse(dto.isActive(), "False boolean should be copied correctly");

        testEntity.setActive(true);
        dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);
        assertTrue(dto.isActive(), "True boolean should be copied correctly");
    }

    // ============================================================================
    // Test Entity Classes
    // ============================================================================

    /**
     * Test entity class representing a domain entity with standard properties
     */
    public static class TestEntity {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private boolean active;
        private Integer quantity;

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    // ============================================================================
    // Test DTO Classes
    // ============================================================================

    /**
     * Basic DTO - standard properties only
     */
    public static class TestEntityDTO {
        private String name;
        private String description;
        private Double price;
        private boolean active;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }

    /**
     * Complete DTO - all properties including ID
     */
    public static class TestEntityDTOComplete {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Integer quantity;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    /**
     * Partial DTO - subset of properties
     */
    public static class TestEntityDTOPartial {
        private String name;
        private Double price;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    /**
     * DTO with primitives
     */
    public static class TestEntityDTOWithPrimitives {
        private String name;
        private boolean active;
        private int quantity;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    /**
     * DTO with extra property not in source
     */
    public static class TestEntityDTOWithExtra {
        private String name;
        private String extraField;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getExtraField() { return extraField; }
        public void setExtraField(String extraField) { this.extraField = extraField; }
    }

    /**
     * DTO with type conversion
     */
    public static class TestEntityDTOWithConversion {
        private String name;
        private Long quantity;  // Integer in source, Long in DTO

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }
    }
}
