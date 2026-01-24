package tools.dynamia.integration.reactive;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.valueOf;
import static org.junit.Assert.*;
import static tools.dynamia.integration.reactive.Reactive.*;

/**
 * Comprehensive test suite for the Reactive framework.
 * Tests all major features including refs, computed values, effects, and their interactions.
 */
public class ReactiveTest {

    @Test
    public void testRefBasicGetSet() {
        var name = ref("John");

        assertEquals("John", name.get());

        name.set("Jane");
        assertEquals("Jane", name.get());
    }

    @Test
    public void testRefUpdate() {
        Ref<Integer> counter = ref(0);

        counter.update(n -> n + 1);
        assertEquals(valueOf(1), counter.get());

        counter.update(n -> n * 2);
        assertEquals(valueOf(2), counter.get());
    }

    @Test
    public void testRefSetSameValueDoesNotTrigger() {
        Ref<String> name = ref("John");
        AtomicInteger effectCount = new AtomicInteger(0);

        effect(() -> {
            name.get();
            effectCount.incrementAndGet();
        });

        int initialCount = effectCount.get();

        // Setting the same value should not trigger the effect
        name.set("John");
        assertEquals(initialCount, effectCount.get());

        // Setting a different value should trigger
        name.set("Jane");
        assertEquals(initialCount + 1, effectCount.get());
    }

    @Test
    public void testSimpleEffect() {
        Ref<Integer> count = ref(0);
        List<Integer> captured = new ArrayList<>();

        effect(() -> captured.add(count.get()));

        // Effect should run immediately
        assertEquals(1, captured.size());
        assertEquals(valueOf(0), captured.getFirst());

        // Effect should run when count changes
        count.set(5);
        assertEquals(2, captured.size());
        assertEquals(valueOf(5), captured.get(1));

        count.set(10);
        assertEquals(3, captured.size());
        assertEquals(valueOf(10), captured.get(2));
    }

    @Test
    public void testEffectWithMultipleDependencies() {
        Ref<Integer> a = ref(1);
        Ref<Integer> b = ref(2);
        List<Integer> sums = new ArrayList<>();

        effect(() -> {
            sums.add(a.get() + b.get());
        });

        // Initial execution
        assertEquals(1, sums.size());
        assertEquals(valueOf(3), sums.getFirst());

        // Changing 'a' should trigger
        a.set(10);
        assertEquals(2, sums.size());
        assertEquals(valueOf(12), sums.get(1));

        // Changing 'b' should trigger
        b.set(5);
        assertEquals(3, sums.size());
        assertEquals(valueOf(15), sums.get(2));
    }

    @Test
    public void testComputedBasic() {
        Ref<Integer> count = ref(0);
        Computed<Integer> doubled = computed(() -> count.get() * 2);

        assertEquals(valueOf(0), doubled.get());

        count.set(5);
        assertEquals(valueOf(10), doubled.get());

        count.set(20);
        assertEquals(valueOf(40), doubled.get());
    }

    @Test
    public void testComputedWithMultipleDependencies() {
        Ref<Integer> width = ref(10);
        Ref<Integer> height = ref(20);
        Computed<Integer> area = computed(() -> width.get() * height.get());

        assertEquals(valueOf(200), area.get());

        width.set(15);
        assertEquals(valueOf(300), area.get());

        height.set(10);
        assertEquals(valueOf(150), area.get());
    }

    @Test
    public void testComputedCaching() {
        Ref<Integer> count = ref(0);
        AtomicInteger computeCount = new AtomicInteger(0);

        Computed<Integer> doubled = computed(() -> {
            computeCount.incrementAndGet();
            return count.get() * 2;
        });

        // Initial computation (runs twice: once for initialization, once for effect setup)
        assertEquals(valueOf(0), doubled.get());
        int initialComputes = computeCount.get();
        assertTrue("Expected at least 1 computation", initialComputes >= 1);

        // Getting again without changes should use cached value
        assertEquals(valueOf(0), doubled.get());
        assertEquals("Should not recompute when getting cached value", initialComputes, computeCount.get());

        // Changing the ref should trigger recomputation
        count.set(5);
        assertEquals(valueOf(10), doubled.get());
        assertTrue("Should have recomputed after change", computeCount.get() > initialComputes);
    }

    @Test
    public void testComputedInEffect() {
        Ref<Integer> count = ref(0);
        Computed<Integer> doubled = computed(() -> count.get() * 2);
        List<Integer> captured = new ArrayList<>();

        effect(() -> {
            captured.add(doubled.get());
        });

        assertEquals(1, captured.size());
        assertEquals(valueOf(0), captured.getFirst());

        count.set(5);
        assertEquals(2, captured.size());
        assertEquals(valueOf(10), captured.get(1));
    }

    @Test
    public void testChainedComputed() {
        Ref<Integer> base = ref(10);
        Computed<Integer> doubled = computed(() -> base.get() * 2);
        Computed<Integer> tripled = computed(() -> doubled.get() * 3);

        assertEquals(valueOf(20), doubled.get());
        assertEquals(valueOf(60), tripled.get());

        base.set(5);
        assertEquals(valueOf(10), doubled.get());
        assertEquals(valueOf(30), tripled.get());
    }

    @Test
    public void testComplexReactiveGraph() {
        // Build a complex reactive graph
        Ref<Integer> a = ref(1);
        Ref<Integer> b = ref(2);
        Computed<Integer> sum = computed(() -> a.get() + b.get());
        Computed<Integer> product = computed(() -> a.get() * b.get());

        // Test individual computed values
        assertEquals(valueOf(3), sum.get());      // 1 + 2 = 3
        assertEquals(valueOf(2), product.get());  // 1 * 2 = 2

        // Change a to 2
        a.set(2);
        assertEquals(valueOf(4), sum.get());      // 2 + 2 = 4
        assertEquals(valueOf(4), product.get());  // 2 * 2 = 4

        // Change b to 3
        b.set(3);
        assertEquals(valueOf(5), sum.get());      // 2 + 3 = 5
        assertEquals(valueOf(6), product.get());  // 2 * 3 = 6
    }

    @Test
    public void testRefToString() {
        Ref<String> name = ref("Alice");
        assertEquals("Alice", name.toString());

        name.set("Bob");
        assertEquals("Bob", name.toString());
    }

    @Test
    public void testComputedToString() {
        Ref<Integer> count = ref(5);
        Computed<Integer> doubled = computed(() -> count.get() * 2);

        assertEquals("10", doubled.toString());
    }

    @Test
    public void testRefWithNullValue() {
        Ref<String> nullable = ref(null);
        Assert.assertNull(nullable.get());

        nullable.set("value");
        assertEquals("value", nullable.get());

        nullable.set(null);
        Assert.assertNull(nullable.get());
    }

    @Test
    public void testComputedWithNullValue() {
        Ref<String> base = ref(null);
        Computed<Integer> length = computed(() -> {
            String value = base.get();
            return value != null ? value.length() : 0;
        });

        assertEquals(valueOf(0), length.get());

        base.set("Hello");
        assertEquals(valueOf(5), length.get());

        base.set(null);
        assertEquals(valueOf(0), length.get());
    }

    @Test
    public void testMultipleEffectsOnSameRef() {
        Ref<Integer> count = ref(0);
        List<String> log = new ArrayList<>();

        effect(() -> log.add("Effect1: " + count.get()));

        effect(() -> log.add("Effect2: " + count.get()));

        assertEquals(2, log.size());

        count.set(5);
        assertEquals(4, log.size());
        assertTrue(log.contains("Effect1: 5"));
        assertTrue(log.contains("Effect2: 5"));
    }

    @Test
    public void testConditionalDependency() {
        Ref<Boolean> condition = ref(true);
        Ref<String> a = ref("A");
        Ref<String> b = ref("B");
        List<String> captured = new ArrayList<>();

        effect(() -> {
            String value = condition.get() ? a.get() : b.get();
            captured.add(value);
        });

        // Initially uses 'a'
        assertFalse("Effect should run at least once", captured.isEmpty());
        assertEquals("A", captured.getLast());

        // Changing 'a' should trigger since it's the active branch
        int sizeBefore = captured.size();
        a.set("A2");
        assertTrue("Effect should have been triggered", captured.size() > sizeBefore);
        assertEquals("A2", captured.getLast());

        // Switch condition to use 'b'
        sizeBefore = captured.size();
        condition.set(false);
        assertTrue("Effect should have been triggered", captured.size() > sizeBefore);
        assertEquals("B", captured.getLast());
    }

    @Test
    public void testUpdateWithComplexObject() {
        class Person {
            final String name;
            int age;

            Person(String name, int age) {
                this.name = name;
                this.age = age;
            }

            @Override
            public String toString() {
                return name + ":" + age;
            }
        }

        Ref<Person> person = ref(new Person("John", 30));

        person.update(p -> {
            p.age++;
            return p;
        });

        assertEquals(31, person.get().age);
    }

    @Test
    public void testComputedWithComplexCalculation() {
        Ref<Integer> price = ref(100);
        Ref<Double> taxRate = ref(0.15);
        Ref<Double> discount = ref(0.10);

        Computed<Double> finalPrice = computed(() -> {
            double base = price.get();
            double afterDiscount = base * (1 - discount.get());
            return afterDiscount * (1 + taxRate.get());
        });

        // 100 * 0.9 * 1.15 = 103.5
        assertEquals(103.5, finalPrice.get(), 0.001);

        price.set(200);
        // 200 * 0.9 * 1.15 = 207
        assertEquals(207.0, finalPrice.get(), 0.001);

        discount.set(0.20);
        // 200 * 0.8 * 1.15 = 184
        assertEquals(184.0, finalPrice.get(), 0.001);
    }

    @Test
    public void testEffectWithSideEffects() {
        Ref<String> status = ref("idle");
        StringBuilder log = new StringBuilder();

        effect(() -> log.append(status.get()).append(";"));

        assertEquals("idle;", log.toString());

        status.set("loading");
        assertEquals("idle;loading;", log.toString());

        status.set("success");
        assertEquals("idle;loading;success;", log.toString());
    }
}
