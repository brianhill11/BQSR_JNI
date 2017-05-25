
public class TestClass implements TestInterface {
    private String name;
    private Integer local_start;
    private int local_start_prim;
    private Integer end;

    public TestClass() {
        this.local_start = 12;
        this.local_start_prim = 13;
        this.name = new String("foo bar");
//        final long startTime1 = System.currentTimeMillis();
//        // copy value from Int class to primiative type
//        final int tmp_int = this.local_start;
//        // call native method, passing the primiative value
//        final int res = cPassValue(tmp_int);
//        // measure total execution time
//        final long endTime1 = System.currentTimeMillis();
//        System.out.println("Test 1 execution time: " + (endTime1 - startTime1));
//
//        final long startTime2 = System.currentTimeMillis();
//        // call native method, which fetches primiative value through accessor function
//        final int res2 = cGetValue();
//        // measure total execution time
//        final long endTime2 = System.currentTimeMillis();
//        System.out.println("Test 2 execution time: " + (endTime2 - startTime2));



    }

    public int getStart() {
        return this.local_start_prim;
    };

    public String getName() {
        return this.name;
    };



    //public Int getEnd() {
    //    return this.end;
    //}
}
