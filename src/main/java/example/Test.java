package example;

import java.lang.reflect.Method;
import java.util.Arrays;

public class Test {
    static abstract class Z {
        
    }
    
    static class Y extends Z {
        
    }
    
    static abstract class A<I extends Z> {
        public abstract I a(I a);
    }
    
    static class B extends A<Y> {
        @Override
        public Y a(Y a) {
            return a;
        }
    }
    
    public static void main(String[] args) {
        B b = new B();
        for (Method m : b.getClass().getMethods()) {
            System.out.println(m);
            System.out.println(Arrays.asList(m.getParameterTypes()));
        }
    }
}
