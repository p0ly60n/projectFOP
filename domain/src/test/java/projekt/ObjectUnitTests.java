package projekt;

import java.util.function.Function;

import static org.tudalgo.algoutils.student.Student.crash;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectUnitTests<T> {

    private final Function<Integer, T> testObjectFactory;
    private final Function<T, String> toString;

    private T[] testObjects;
    private T[] testObjectsReferenceEquality;
    private T[] testObjectsContentEquality;
    private byte i;
    private byte j;

    public ObjectUnitTests(Function<Integer, T> testObjectFactory, Function<T, String> toString) {
        this.testObjectFactory = testObjectFactory;
        this.toString = toString;
    }

    @SuppressWarnings("unchecked")
    public void initialize(int testObjectCount) {
        crash(); // TODO: H12.1 - remove if implemented
    }

    public void testEquals(ObjectUnitTests o) {
        // TODO: H12.1
        ObjectUnitTests t = new ObjectUnitTests;
        assertEquals(t,testObjects);
        assertEquals(t,testObjectsReferenceEquality);
        assertEquals(t,testObjectsContentEquality);
        assertNotEquals(i!=j,testObjects);

    }

    public void testHashCode() {
        // TODO: H12.1
        ObjectUnitTests t = new ObjectUnitTests;
        assertEquals(t,testObjects);
        assertEquals(t,testObjectsReferenceEquality);
        assertEquals(t,testObjectsContentEquality);
        assertNotEquals(i!=j,testObjects);
    }

    public void testToString() {
        ObjectUnitTests t = new ObjectUnitTests;
        assertEquals(t,testObjects);
    }

}
