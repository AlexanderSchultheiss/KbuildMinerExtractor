package net.ssehub.kernel_haven.kbuildminer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The Class AllTests.
 */
@RunWith(Suite.class)
@SuiteClasses({
    ConverterTest.class,
    KbuildMinerExtractorTest.class,
    KbuildMinerPcGrammarTest.class,
    KbuildMinerWrapperTest.class,
    })
public class AllTests {
    // runs tests defined in SuiteClasses
}
