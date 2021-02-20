import org.testng.annotations.*;

public class SimpleTest {

    @BeforeClass
    public void setUp() {
        // code that will be invoked when this test is instantiated
    }

    @org.testng.annotations.Test(groups = { "fast" })
    public void aFastTest() {
        System.out.println("Fast test");
    }

    @org.testng.annotations.Test(groups = { "slow" })
    public void aSlowTest() {
        System.out.println("Slow test");
    }

}
