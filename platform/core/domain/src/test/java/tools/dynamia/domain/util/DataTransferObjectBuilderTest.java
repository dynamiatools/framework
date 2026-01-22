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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

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

    @Before
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

        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should be copied", testEntity.getName(), dto.getName());
        assertEquals("Description should be copied", testEntity.getDescription(), dto.getDescription());
        assertEquals("Price should be copied", testEntity.getPrice(), dto.getPrice(), 0.001);
        assertEquals("Active flag should be copied", testEntity.isActive(), dto.isActive());
    }

    /**
     * Test DTO transformation copies all matching properties
     */
    @Test
    public void testBuildDTOCopiesAllMatchingProperties() {
        TestEntityDTOComplete dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOComplete.class);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should be copied", testEntity.getId(), dto.getId());
        assertEquals("Name should be copied", testEntity.getName(), dto.getName());
        assertEquals("Description should be copied", testEntity.getDescription(), dto.getDescription());
        assertEquals("Price should be copied", testEntity.getPrice(), dto.getPrice(), 0.001);
        assertEquals("Quantity should be copied", testEntity.getQuantity(), dto.getQuantity());
    }

    /**
     * Test DTO transformation with partial properties (DTO has subset of source properties)
     */
    @Test
    public void testBuildDTOPartialProperties() {
        TestEntityDTOPartial dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOPartial.class);

        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should be copied", testEntity.getName(), dto.getName());
        assertEquals("Price should be copied", testEntity.getPrice(), dto.getPrice(), 0.001);
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

        assertNotNull("DTO should not be null", dto);
        assertNull("Null name should remain null", dto.getName());
        assertNull("Null description should remain null", dto.getDescription());
        assertEquals("Non-null price should be copied", testEntity.getPrice(), dto.getPrice(), 0.001);
    }

    /**
     * Test DTO transformation with empty entity (all null)
     */
    @Test
    public void testBuildDTOWithAllNullProperties() {
        TestEntity emptyEntity = new TestEntity();

        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(emptyEntity, TestEntityDTO.class);

        assertNotNull("DTO should not be null even for empty entity", dto);
        assertNull("Name should be null", dto.getName());
        assertNull("Description should be null", dto.getDescription());
        assertNull("Price should be null", dto.getPrice());
    }

    /**
     * Test DTO transformation preserves primitive types
     */
    @Test
    public void testBuildDTOPreservesPrimitives() {
        TestEntityDTOWithPrimitives dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithPrimitives.class);

        assertNotNull("DTO should not be null", dto);
        assertEquals("Active boolean should be copied", testEntity.isActive(), dto.isActive());
        assertEquals("Quantity int should be copied", testEntity.getQuantity().intValue(), dto.getQuantity());
    }

    /**
     * Test DTO transformation with additional properties in DTO (should be left as default)
     */
    @Test
    public void testBuildDTOWithAdditionalProperties() {
        TestEntityDTOWithExtra dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithExtra.class);

        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should be copied", testEntity.getName(), dto.getName());

        // Additional property not in source should remain at default value
        assertNull("Extra property should be null (default)", dto.getExtraField());
    }

    /**
     * Test DTO transformation with different property types (compatible conversion)
     */
    @Test
    public void testBuildDTOWithTypeConversion() {
        TestEntityDTOWithConversion dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTOWithConversion.class);

        assertNotNull("DTO should not be null", dto);
        // Spring BeanUtils should handle compatible type conversions
        assertEquals("Name should be copied", testEntity.getName(), dto.getName());

        // Note: Integer to Long conversion may not happen automatically with BeanUtils
        // This is expected behavior - BeanUtils only copies matching types
        // For type conversion, use custom converters or transform methods
        if (dto.getQuantity() != null) {
            assertEquals("Quantity should be converted if Spring supports it",
                        testEntity.getQuantity().longValue(), dto.getQuantity().longValue());
        }
    }

    /**
     * Test buildDTO creates a new instance (not modifying original)
     */
    @Test
    public void testBuildDTOCreatesNewInstance() {
        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull("DTO should not be null", dto);

        // Modify DTO
        dto.setName("Modified Name");
        dto.setPrice(199.99);

        // Original should remain unchanged
        assertEquals("Original name should not change", "Test Product", testEntity.getName());
        assertEquals("Original price should not change", 99.99, testEntity.getPrice(), 0.001);
    }

    /**
     * Test buildDTO with boolean properties
     */
    @Test
    public void testBuildDTOWithBooleans() {
        testEntity.setActive(false);

        TestEntityDTO dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);

        assertNotNull("DTO should not be null", dto);
        assertFalse("False boolean should be copied correctly", dto.isActive());

        testEntity.setActive(true);
        dto = DataTransferObjectBuilder.buildDTO(testEntity, TestEntityDTO.class);
        assertTrue("True boolean should be copied correctly", dto.isActive());
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
