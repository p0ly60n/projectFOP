package projekt;

import java.util.function.Function;

import static org.tudalgo.algoutils.student.Student.crash;
import static org.junit.jupiter.api.Assertions.*;


public class ComparableUnitTests<T extends Comparable<? super T>> {

    private final Function<Integer, T> testObjectFactory;

    private T[] testObjects;

    public ComparableUnitTests(Function<Integer, T> testObjectFactory) {
        this.testObjectFactory = testObjectFactory;
    }

    @SuppressWarnings("unchecked")
    public void initialize(int testObjectCount) {
        testObjects = (T[]) new Comparable<?>[testObjectCount];

        for (int i = 0; i < testObjectCount; i++) {
            testObjects[i] = testObjectFactory.apply(i);
        }
    }

    public void testBiggerThen() {
        for (int i = 0; i < testObjects.length; i++) {
            for (int j = 0; j < testObjects.length; j++) {
                if (i > j) {
                    assertTrue(testObjects[i].compareTo(testObjects[j]) < 0);
                }
            }
        }
    }

    @SuppressWarnings("EqualsWithItself")
    public void testAsBigAs() {
        crash(); // TODO: H12.1 - remove if implemented
    }

    public void testLessThen() {
        crash(); // TODO: H12.1 - remove if implemented
    }
}
