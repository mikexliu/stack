package stack.annotations;

public @interface Paginate {
    public int maxNumberResults() default 25;
}
