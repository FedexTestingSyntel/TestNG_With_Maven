package SupportClasses;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class SetEnvironment {
    @Test
    @Parameters("Levels")
    public void testMethod(String Levels) {
        SupportClasses.DriverFactory.LevelsToTest = Levels;
    }
}