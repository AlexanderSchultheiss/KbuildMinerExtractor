package net.ssehub.kernel_haven.kbuildminer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import net.ssehub.kernel_haven.build_model.BuildModel;
import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.logic.Conjunction;
import net.ssehub.kernel_haven.util.logic.Disjunction;
import net.ssehub.kernel_haven.util.logic.False;
import net.ssehub.kernel_haven.util.logic.Formula;
import net.ssehub.kernel_haven.util.logic.Negation;
import net.ssehub.kernel_haven.util.logic.Variable;
import net.ssehub.kernel_haven.util.logic.parser.ExpressionFormatException;
import net.ssehub.kernel_haven.util.logic.parser.Parser;
import net.ssehub.kernel_haven.util.logic.parser.VariableCache;
import net.ssehub.kernel_haven.variability_model.VariabilityModel;
import net.ssehub.kernel_haven.variability_model.VariabilityVariable;

/**
 * Converts the output of KbuildMiner to {@link BuildModel}.
 * 
 * @author Adam
 * @author Moritz
 *
 */
public class Converter {
    
    private static final Logger LOGGER = Logger.get();
    
    private VariabilityModel varModel;

    /**
     * Creates a new converter with the given variability model.
     * 
     * @param varModel The variability model. This is needed to check which variables are tristate.
     */
    public Converter(VariabilityModel varModel) {
        this.varModel = varModel;
    }
    
    /**
     * Replaces _MODULE variables that are not part of a tristate representation with {@link False}.
     * If the new {@link False} parts are part of a disjunction, then this is simplified via
     * {@link #simplifyDisjunctionsWithFalse(Disjunction)}.
     * 
     * @param formula The formula to remove the _MODULEs from.
     * @return The formula with without the _MODULEs.
     */
    private Formula removeNonTristateModules(Formula formula) {
        
        Formula result = formula;
        
        if (formula instanceof Disjunction) {
            Disjunction disjunction = (Disjunction) formula;
            result = new Disjunction(removeNonTristateModules(disjunction.getLeft()),
                    removeNonTristateModules(disjunction.getRight()));
            
            // simplify here, so that no necessary False objects are left in the formula.
            result = simplifyDisjunctionsWithFalse((Disjunction) result);
            
        } else if (formula instanceof Conjunction) {
            Conjunction conjunction = (Conjunction) formula;
            result = new Conjunction(removeNonTristateModules(conjunction.getLeft()),
                    removeNonTristateModules(conjunction.getRight()));
            
        } else if (formula instanceof Negation) {
            result = removeNonTristateModules(((Negation) formula).getFormula());
            
        } else if (formula instanceof Variable) {
            Variable var = (Variable) formula;
            if (var.getName().endsWith("_MODULE")) {
                // get the base name of the variable, without _MODULE.
                String baseName = var.getName().substring(0, var.getName().length() - "_MODULE".length());
                
                VariabilityVariable varVariable = varModel.getVariableMap().get(baseName);
                
                if (varVariable != null && !varVariable.getType().equals("tristate")) {
                    result = False.INSTANCE;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Simplifies disjunction where one part is {@link False}. Changes (A || false) to A.
     * This does not recursively descend into the formula, but rather only consider the disjunction
     * directly given.
     * 
     * @param formula The formula to remove the parts from.
     * @return The formula that is without the {@link False} in disjunctions.
     */
    private Formula simplifyDisjunctionsWithFalse(Disjunction formula) {
        Formula result = formula;
        
        if (formula.getLeft() instanceof False) {
            result = formula.getRight();
        } else if (formula.getRight() instanceof False) {
            result = formula.getLeft();
        }
            
        return result;
    }
    
    /**
     * Converts the given output file of KbuildMiner to {@link BuildModel}. Invalid presence
     * conditions get the presence condition {@link False}.
     * 
     * @param file The file that contains the output of KbuildMiner.
     * @return The {@link BuildModel}.
     * 
     * @throws IOException If reading the file fails. Never <code>null</code>.
     */
    public BuildModel convert(File file) throws IOException {
        BuildModel result = new BuildModel();
        
        VariableCache cache = new VariableCache();
        Parser<Formula> pcParser = new Parser<Formula>(new KbuildMinerPcGrammar(cache));
        
        LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(file)));
        String line;
        while ((line = in.readLine()) != null) {
            String filename = line.substring(0, line.indexOf(':'));
            
            File sourceFile = new File(filename);
            result.add(sourceFile, False.INSTANCE);
            
            String pc = line.substring(filename.length() + 2);
            
            if (pc.contains("InvalidExpression()")) {
                LOGGER.logWarning("Presence condition for file " + filename + " in line " + in.getLineNumber()
                    + " is invalid");
                
            } else {
                try {
                    Formula presenceCondition = pcParser.parse(pc);
                    presenceCondition = removeNonTristateModules(presenceCondition);
                    result.add(sourceFile, presenceCondition);
                } catch (ExpressionFormatException e) {
                    LOGGER.logException("Couldn't parse expression \"" + pc + "\" in line " + in.getLineNumber(), e);
                }
            }
        }
        
        in.close();
        
        return result;
    }

}
