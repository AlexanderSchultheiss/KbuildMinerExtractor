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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.True;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;

/**
 * Tests the {@link KbuildMinerPcGrammar}.
 * 
 * @author Adam  (from KernelMiner project)
 */
public class KbuildMinerPcGrammarTest {

    /**
     * Tests parsing of simple variables.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleVariable() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        Formula f1 = parser.parse("A == \"y\"");
        assertVariable(f1, "CONFIG_A");
        
        Formula f2 = parser.parse("A == \"m\"");
        assertVariable(f2, "CONFIG_A_MODULE");
        
        Formula f3 = parser.parse("A == \"yes\"");
        assertVariable(f3, "CONFIG_A");
        
        Formula f4 = parser.parse("A != \"y\"");
        assertVariable(assertNegation(f4), "CONFIG_A");
        
        Formula f5 = parser.parse("A != \"m\"");
        assertVariable(assertNegation(f5), "CONFIG_A_MODULE");
        
        Formula f6 = parser.parse("A");
        assertVariable(f6, "CONFIG_A");
    }
    
    /**
     * Tests parsing of malformed variables.
     */
    @Test
    public void testMalformedVariable() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        try {
            parser.parse("");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A = \"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A=\"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A ! \"y\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A == \"ja\"");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("A == yes");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
    }
    
    /**
     * Tests parsing of simple constants.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleConstant() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        Formula f1 = parser.parse("[TRUE]");
        Assert.assertTrue(f1 instanceof True);
    }
    
    /**
     * Tests parsing of malformed constants.
     */
    @Test
    public void testMalformedConstant() {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        
        try {
            parser.parse("[TREU]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("[FALSE]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("[TRUE");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
        
        try {
            parser.parse("TRUE]");
            Assert.fail("Expected exception");
        } catch (ExpressionFormatException e) {
            
        }
    }
    
    /**
     * Tests parsing of negations.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleNegation() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "!(A == \"y\")";
        
        Formula f = parser.parse(str);
        
        assertVariable(assertNegation(f), "CONFIG_A");
        Assert.assertEquals(1, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of conjunctions.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleConjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\") && B == \"m\"";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(t[0], "CONFIG_A");
        assertVariable(t[1], "CONFIG_B_MODULE");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of disjunctions.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleDisjunction() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"m\")||B";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "CONFIG_A_MODULE");
        assertVariable(t[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of multiple operators with precedence.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testPrecedence() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "!A == \"y\" && B == \"y\"";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "CONFIG_A");
        assertVariable(t[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of parathensis.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleParenthesis1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\") || (!(B == \"y\") && (C == \"m\"))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertDisjunction(f);
        assertVariable(t[0], "CONFIG_A");
        Formula[] t2 = assertConjunction(t[1]);
        assertVariable(assertNegation(t2[0]), "CONFIG_B");
        assertVariable(t2[1], "CONFIG_C_MODULE");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of paranthesis.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testSimpleParenthesis2() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(!(A == \"y\") && ((B == \"m\") || (C == \"yes\")))";
        
        Formula f = parser.parse(str);
        
        Formula[] t = assertConjunction(f);
        assertVariable(assertNegation(t[0]), "CONFIG_A");
        Formula[] t2 = assertDisjunction(t[1]);
        assertVariable(t2[0], "CONFIG_B_MODULE");
        assertVariable(t2[1], "CONFIG_C");
        Assert.assertEquals(3, cache.getNumVariables());
    }
    
    /**
     * Tests parsing of complex formula.
     * 
     * @throws ExpressionFormatException unwanted.
     */
    @Test
    public void testComplex1() throws ExpressionFormatException {
        VariableCache cache = new VariableCache();
        Parser<Formula> parser = new Parser<>(new KbuildMinerPcGrammar(cache));
        String str = "(A == \"y\" && B == \"y\" && (!A == \"y\" || B))";
        
        Formula f = parser.parse(str);
        
        Formula[] t1 = assertConjunction(f);
        assertVariable(t1[0], "CONFIG_A");
        Formula[] t2 = assertConjunction(t1[1]);
        assertVariable(t2[0], "CONFIG_B");
        Formula[] t3 = assertDisjunction(t2[1]);
        assertVariable(assertNegation(t3[0]), "CONFIG_A");
        assertVariable(t3[1], "CONFIG_B");
        Assert.assertEquals(2, cache.getNumVariables());
    }
    
    /**
     * Checks if the given formula is a variable with the given name.
     * 
     * @param formula The formula that must be a variable.
     * @param expectedName The name that the variable should have.
     */
    private static void assertVariable(Formula formula, String expectedName) {
        assertTrue(formula instanceof Variable);
        assertEquals(expectedName, ((Variable) formula).getName());
    }
    
    /**
     * Checks that the given formula is a negation.
     * 
     * @param formula The formula that must be a negation.
     * @return The nested formula inside the negation.
     */
    private static Formula assertNegation(Formula formula) {
        assertTrue(formula instanceof Negation);
        return ((Negation) formula).getFormula();
    }
    
    /**
     * Checks that the given formula is a conjunction.
     * 
     * @param formula The formula that must be a conjunction.
     * @return The nested formulas.
     */
    private static Formula[] assertConjunction(Formula formula) {
        assertTrue(formula instanceof Conjunction);
        Conjunction c = (Conjunction) formula;
        return new Formula[] {c.getLeft(), c.getRight()};
    }
    
    /**
     * Checks that the given formula is a disjunction.
     * 
     * @param formula The formula that must be a disjunction.
     * @return The nested formulas.
     */
    private static Formula[] assertDisjunction(Formula formula) {
        assertTrue(formula instanceof Disjunction);
        Disjunction c = (Disjunction) formula;
        return new Formula[] {c.getLeft(), c.getRight()};
    }

}
