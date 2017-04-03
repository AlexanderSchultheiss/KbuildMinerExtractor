package de.uni_hildesheim.sse.kernel_haven.kbuildminer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The Class AllTests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    KbuildMinerWrapperTest.class,
    ConverterTest.class,
    KbuildMinerPcGrammarTest.class,
    })
public class AllTests {
    // runs tests defined in SuiteClasses
}
