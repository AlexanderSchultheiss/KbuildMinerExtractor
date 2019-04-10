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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * Tests the converter.
 * 
 * @author Adam
 * @author Moritz
 */
@SuppressWarnings("null")
public class ConverterTest {
    
    /**
     * Tests whether the converter reads the correct files and PCs.
     * 
     * @throws IOException unwanted.
     */
    @Test
    public void testConvert() throws IOException {
        Set<VariabilityVariable> vars = new HashSet<>();
        vars.add(new VariabilityVariable("CONFIG_ALPHA", "bool"));
        vars.add(new VariabilityVariable("CONFIG_BETA", "tristate"));
        VariabilityModel varModel = new VariabilityModel(null, vars);
        
        Converter c = new Converter(varModel);
        
        BuildModel model = c.convert(new File("testdata/pcs.txt"));
        assertThat(model.getSize(), is(4));
        
        Formula f1 = model.getPc(new File("file1.c"));
        assertThat(f1, notNullValue());
        assertThat(f1.toString(), is("CONFIG_ALPHA"));
        
        Formula f2 = model.getPc(new File("file2.c"));
        assertThat(f2, notNullValue());
        assertThat(f2, instanceOf(True.class));
        
        Formula f3 = model.getPc(new File("dir/file1.c"));
        assertThat(f3, notNullValue());
        assertThat(f3.toString(), is("CONFIG_BETA || CONFIG_BETA_MODULE"));
        
        Formula f4 = model.getPc(new File("dir/file2.c"));
        assertThat(f4, notNullValue());
        assertThat(f4.toString(), is("(CONFIG_BETA || CONFIG_BETA_MODULE) && CONFIG_ALPHA"));
        
        assertThat(model.getPc(new File("notExisting.c")), nullValue());
    }
    
    /**
     * Tests that the InvalidExpression() parts produced by KbuildMiner are correctly handled.
     * 
     * @throws IOException unwanted.
     */
    @Test
    public void testInvalidExpressionOutput() throws IOException {
        Converter c = new Converter(new VariabilityModel(null, new HashSet<VariabilityVariable>()));
        
        BuildModel model = c.convert(new File("testdata/invalid_pcs.txt"));
        
        assertThat(model.getSize(), is(4));
        
        Formula f1 = model.getPc(new File("file1.c"));
        assertThat(f1, notNullValue());
        assertThat(f1, instanceOf(False.class));
        
        Formula f2 = model.getPc(new File("file2.c"));
        assertThat(f2, notNullValue());
        assertThat(f2, instanceOf(False.class));
        
        Formula f3 = model.getPc(new File("file3.c"));
        assertThat(f3, notNullValue());
        assertThat(f3, instanceOf(True.class));
        
        Formula f4 = model.getPc(new File("file4.c"));
        assertThat(f4, notNullValue());
        assertThat(f4, instanceOf(False.class));
    }
    
}
