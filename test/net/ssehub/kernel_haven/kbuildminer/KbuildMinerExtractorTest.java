/*
 * Copyright 2017-2019 University of Hildesheim, Software Systems Engineering
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ssehub.kernel_haven.kbuildminer;

import static net.ssehub.kernel_haven.util.logic.FormulaBuilder.or;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.PipelineConfigurator;
import net.ssehub.kernel_haven.SetUpException;
import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.config.DefaultSettings;
import net.ssehub.kernel_haven.test_utils.TestConfiguration;
import net.ssehub.kernel_haven.util.ExtractorException;
import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests the full {@link KbuildMinerExtractor} class.
 * 
 * @author Adam
 */
public class KbuildMinerExtractorTest {

    private static final @NonNull File RESOURCE_DIR = new File("testdata/tmp_res");
    
    /**
     * Creates the temporary resource dir.
     */
    @BeforeClass
    public static void createTmpRes() {
        RESOURCE_DIR.mkdir();
    }
    
    /**
     * Deletes the temporary resource directory.
     * 
     * @throws IOException If deleting fails.
     */
    @AfterClass
    public static void deleteTmpRes() throws IOException {
        Util.deleteFolder(RESOURCE_DIR);
    }
    
    /**
     * Runs the {@link KbuildMinerExtractor} on the given target.
     * 
     * @param sourceTree The source tree to run on.
     * @param topFolders The top folders in the source tree.
     * 
     * @return The build model created by the extractor.
     * 
     * @throws SetUpException If creating the configuration or initializing the extractor fails.
     * @throws ExtractorException If the extractor throws an exception.
     */
    @SuppressWarnings("null")
    private BuildModel run(File sourceTree, String ... topFolders) throws SetUpException, ExtractorException {
        StringBuilder topFoldersString = new StringBuilder();
        for (int i = 0; i < topFolders.length; i++) {
            topFoldersString.append(topFolders[i]);
            if (i != topFolders.length - 1) {
                topFoldersString.append(',');
            }
        }
        
        TestConfiguration config = new TestConfiguration(new Properties());
        config.registerSetting(KbuildMinerExtractor.TOP_FOLDERS);
        config.setValue(KbuildMinerExtractor.TOP_FOLDERS, topFoldersString.toString());
        config.setValue(DefaultSettings.RESOURCE_DIR, RESOURCE_DIR);
        config.setValue(DefaultSettings.SOURCE_TREE, sourceTree);
        
        PipelineConfigurator configurator = PipelineConfigurator.instance();
        configurator.init(config);
        configurator.instantiateExtractors();
        configurator.createProviders();
        
        KbuildMinerExtractor extractor = new KbuildMinerExtractor();
        extractor.init(config);
        
        return extractor.runOnFile(sourceTree);
    }
    
    /**
     * Tests the extractor on testdata/pseudo_linux.
     * 
     * @throws ExtractorException unwanted. 
     * @throws SetUpException unwanted.
     */
    @Test
    @SuppressWarnings("null")
    public void testPseudoLinux() throws SetUpException, ExtractorException {
        BuildModel bm = run(new File("testdata/pseudo_linux"), "arch/x86", "drivers", "kernel");
        
        assertThat(bm.getSize(), is(3));
        assertThat(bm.getPc(new File("arch/x86/kernel.c")), is(True.INSTANCE));
        assertThat(bm.getPc(new File("drivers/driver.c")), is(or("CONFIG_A", "CONFIG_A_MODULE")));
        assertThat(bm.getPc(new File("kernel/core/core.c")), is(True.INSTANCE));
    }
    
}
