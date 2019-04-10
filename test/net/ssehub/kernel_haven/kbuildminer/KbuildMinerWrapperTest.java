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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.ssehub.kernel_haven.util.Util;
import net.ssehub.kernel_haven.util.null_checks.NonNull;

/**
 * Tests if the KbuildMinerWrapper runs correctly.
 * This test is disabled if the linux source tree as a directory not exists.
 * 
 * @author Johannes
 * @author Moritz
 * @author Adam
 */
public class KbuildMinerWrapperTest {

    private static final @NonNull File RESOURCE_DIR = new File("testdata/tmp_res");
    
    private KbuildMinerWrapper wrapper;
    
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
     * Creates a new KbuildMinerWrapper for each test.
     */
    @Before
    public void setUp() {
        wrapper = new KbuildMinerWrapper(RESOURCE_DIR);
    }
    
    /**
     * Tests whether kbuildminer correctly executes.
     * 
     * @throws IOException unwanted.
     */
    @Test
    @SuppressWarnings("null")
    public void testRunKbuildMiner() throws IOException {
        File result = wrapper.runKbuildMiner(new File("testdata/pseudo_linux"), "arch/x86,drivers,kernel");

        assertThat(result, notNullValue());
        
        BufferedReader in = new BufferedReader(new FileReader(result));
        List<String> lines = new LinkedList<>();
        String line;
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }

        in.close();
        result.delete();
        
        assertThat(lines.size(), is(3));
        
        for (String lin : lines) {
            assertThat(lin, anyOf(
                    is("arch/x86/kernel.c: [TRUE]"),
                    is("drivers/driver.c: ((A == \"y\") || (A == \"m\"))"),
                    is("kernel/core/core.c: [TRUE]")
                    ));
        }
    }
    
    /**
     * Tests whether kbuildminer correctly detects missing makefiles.
     * 
     * @throws IOException unwanted.
     */
    @Test
    public void testRunKbuildMinerMissingMakefile() throws IOException {
        File result = wrapper.runKbuildMiner(new File("testdata/missing_makefile"), "drivers");
        assertThat(result, nullValue());
    }
    
}
