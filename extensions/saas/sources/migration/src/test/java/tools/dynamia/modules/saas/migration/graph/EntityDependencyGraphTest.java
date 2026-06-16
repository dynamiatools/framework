/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package tools.dynamia.modules.saas.migration.graph;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute.PersistentAttributeType;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EntityDependencyGraph#topologicalSort(List)}.
 *
 * <p>Entity model used across tests:
 * <pre>
 *   Account  ←── Order  ←── OrderItem ──→ Product  ←── Category (no deps)
 * </pre>
 * Expected topological order: {Account, Category} before {Order, Product} before {OrderItem}.
 *
 * <p>Raw-typed mocks are required to work around the complex JPA generics on
 * {@code ManagedType.getSingularAttributes()} which returns
 * {@code Set<SingularAttribute<? super X, ?>>}.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@RunWith(MockitoJUnitRunner.class)
public class EntityDependencyGraphTest {

    // Marker classes used as stand-ins for real JPA entities
    static class Account {}
    static class Category {}
    static class Product {}
    static class Order {}
    static class OrderItem {}

    @Mock private EntityManagerFactory emf;
    @Mock private Metamodel metamodel;

    // Raw EntityType mocks avoid JPA wildcard inference problems
    private EntityType accountType;
    private EntityType categoryType;
    private EntityType productType;
    private EntityType orderType;
    private EntityType orderItemType;

    private EntityDependencyGraph graph;

    @Before
    public void setUp() {
        accountType   = mock(EntityType.class);
        categoryType  = mock(EntityType.class);
        productType   = mock(EntityType.class);
        orderType     = mock(EntityType.class);
        orderItemType = mock(EntityType.class);

        when(emf.getMetamodel()).thenReturn(metamodel);

        // Account: no singular attributes pointing to entities in our set
        when(metamodel.entity(Account.class)).thenReturn(accountType);
        doReturn(Set.of()).when(accountType).getSingularAttributes();

        // Category: no dependencies
        when(metamodel.entity(Category.class)).thenReturn(categoryType);
        doReturn(Set.of()).when(categoryType).getSingularAttributes();

        // Product depends on Category (ManyToOne)
        SingularAttribute productCategory = attrOf(PersistentAttributeType.MANY_TO_ONE, Category.class);
        when(metamodel.entity(Product.class)).thenReturn(productType);
        doReturn(Set.of(productCategory)).when(productType).getSingularAttributes();

        // Order depends on Account (ManyToOne)
        SingularAttribute orderAccount = attrOf(PersistentAttributeType.MANY_TO_ONE, Account.class);
        when(metamodel.entity(Order.class)).thenReturn(orderType);
        doReturn(Set.of(orderAccount)).when(orderType).getSingularAttributes();

        // OrderItem depends on Order (ManyToOne) and Product (ManyToOne)
        SingularAttribute itemOrder   = attrOf(PersistentAttributeType.MANY_TO_ONE, Order.class);
        SingularAttribute itemProduct = attrOf(PersistentAttributeType.MANY_TO_ONE, Product.class);
        when(metamodel.entity(OrderItem.class)).thenReturn(orderItemType);
        doReturn(Set.of(itemOrder, itemProduct)).when(orderItemType).getSingularAttributes();

        graph = new EntityDependencyGraph(emf);
    }

    @Test
    public void parentsAppearBeforeChildrenInOutput() {
        List<Class<?>> input = List.of(Account.class, Category.class, Product.class,
                                       Order.class, OrderItem.class);
        List<Class<?>> sorted = graph.topologicalSort(input);

        int idxAccount  = sorted.indexOf(Account.class);
        int idxCategory = sorted.indexOf(Category.class);
        int idxProduct  = sorted.indexOf(Product.class);
        int idxOrder    = sorted.indexOf(Order.class);
        int idxItem     = sorted.indexOf(OrderItem.class);

        Assert.assertTrue("Account before Order",     idxAccount  < idxOrder);
        Assert.assertTrue("Account before OrderItem", idxAccount  < idxItem);
        Assert.assertTrue("Category before Product",  idxCategory < idxProduct);
        Assert.assertTrue("Order before OrderItem",   idxOrder    < idxItem);
        Assert.assertTrue("Product before OrderItem", idxProduct  < idxItem);
    }

    @Test
    public void allInputClassesArePresent() {
        List<Class<?>> input = List.of(Account.class, Category.class, Product.class,
                                       Order.class, OrderItem.class);
        List<Class<?>> sorted = graph.topologicalSort(input);

        Assert.assertEquals(input.size(), sorted.size());
        Assert.assertTrue(sorted.containsAll(input));
    }

    @Test
    public void emptyInputReturnsEmptyList() {
        Assert.assertTrue(graph.topologicalSort(List.of()).isEmpty());
    }

    @Test
    public void nullInputReturnsEmptyList() {
        Assert.assertTrue(graph.topologicalSort(null).isEmpty());
    }

    @Test
    public void singleEntityWithNoDepsIsReturnedAsIs() {
        List<Class<?>> sorted = graph.topologicalSort(List.of(Account.class));
        Assert.assertEquals(1, sorted.size());
        Assert.assertEquals(Account.class, sorted.get(0));
    }

    @Test
    public void oneToOneRelationAlsoCreatesEdge() {
        SingularAttribute oneToOne = attrOf(PersistentAttributeType.ONE_TO_ONE, Account.class);
        doReturn(Set.of(oneToOne)).when(productType).getSingularAttributes();

        List<Class<?>> sorted = graph.topologicalSort(List.of(Account.class, Product.class));
        Assert.assertTrue("Account before Product (ONE_TO_ONE)",
                sorted.indexOf(Account.class) < sorted.indexOf(Product.class));
    }

    @Test
    public void basicAttributeDoesNotCreateDependencyEdge() {
        // A BASIC attr whose javaType happens to be another entity class must not create an edge
        SingularAttribute basic = attrOf(PersistentAttributeType.BASIC, Account.class);
        doReturn(Set.of(basic)).when(categoryType).getSingularAttributes();

        // Both are present, no ordering constraint — both orderings are valid
        List<Class<?>> sorted = graph.topologicalSort(List.of(Account.class, Category.class));
        Assert.assertEquals(2, sorted.size());
        Assert.assertTrue(sorted.contains(Account.class));
        Assert.assertTrue(sorted.contains(Category.class));
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private static SingularAttribute attrOf(PersistentAttributeType type, Class<?> javaType) {
        SingularAttribute attr = mock(SingularAttribute.class);
        when(attr.getPersistentAttributeType()).thenReturn(type);
        when(attr.getJavaType()).thenReturn(javaType);
        return attr;
    }
}
