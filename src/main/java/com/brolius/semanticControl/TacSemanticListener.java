package com.brolius.semanticControl;

import com.brolius.antlrtac.tacBaseListener;
import com.brolius.antlrtac.tacParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TacSemanticListener extends tacBaseListener {
    private tacParser parser;
    private String currentIndent;
    private String currentContext;
    private final String dataIndent = "\t\t\t\t";

    private List<VarElement> varListFromSource;
    private Stack<String> temporariesStack;
    private Stack<String> savedValuesStack;

    private String[] registersInUse;

    private List<tacParser.AssignStatementContext> assignments;
    private List<tacParser.IfStatementContext> ifs;
    private List<tacParser.WhileStatementContext> whiles;

    public TacSemanticListener(tacParser parser, List<VarElement> varListFromSource) {
        this.parser = parser;
        this.varListFromSource = varListFromSource;
        this.currentIndent = "";
        this.currentContext = "main";

        this.temporariesStack = new Stack<>();
        this.savedValuesStack = new Stack<>();
        fillRegistersStack();

        registersInUse = new String[3];

        assignments = new LinkedList<>();
        ifs = new LinkedList<>();
    }

    @Override
    public void enterProgram(tacParser.ProgramContext ctx) {
        // write headers of MIPS
        writeToMIPSFile(currentIndent + "# auto generated MIPS file.");
        writeToMIPSFile(currentIndent + "# VaaDecaf, a Decaf compiler made with Java, ANTLR and the Vaadin framework");
        writeToMIPSFile(currentIndent + "\n");
        writeToMIPSFile(currentIndent + ".text");
        writeToMIPSFile(currentIndent + ".globl main");
    }

    @Override
    public void exitProgram(tacParser.ProgramContext ctx) {
        // write data section
        writeToMIPSFile(currentIndent + "# ---------- data section ----------");
        writeToMIPSFile(currentIndent + ".data");

        // traverse varList and write in data section
        for (VarElement ve : this.varListFromSource) {
            String methodName = ve.getContext().getFirm();
            String varName = ve.getID();
            String type = ve.getVarType();

            // write data
            writeDataInMemory(methodName, varName, type);
        }
    }

    @Override
    public void enterMainDeclaration(tacParser.MainDeclarationContext ctx) {
        this.currentContext = "main";                            // current context = main
        writeToMIPSFile(currentIndent + "main:");           // main method declaration
        incrementIndent();                                       // increment indent
    }

    @Override
    public void exitMainDeclaration(tacParser.MainDeclarationContext ctx) {
        // write exit call
        writeToMIPSFile(currentIndent + "# ---------- Exit ----------\n" +
                currentIndent + "li $v0, 10\n" +
                currentIndent + "syscall");
        decrementIndent();                                       // decrement indent
        writeToMIPSFile(currentIndent + "\n");
    }

    @Override
    public void enterWhileStatement(tacParser.WhileStatementContext ctx) {
        System.out.println("Entering while stmt");
        if (!whiles.contains(ctx)) {
            // get objects
            List<tacParser.LabelContext> lbls = ctx.label();
            String lbl1 = lbls.get(0).getText();
            String lbl2 = lbls.get(1).getText();
            tacParser.AssignStatementContext assignStmt = ctx.assignStatement();
            List<tacParser.LocationContext> locations = ctx.location();
            String temp = locations.get(0).getText();
            tacParser.BlockContext falseBlock = ctx.block();
            temp = temp.replace("_", "$");

            // handle assignment before if
            writeToMIPSFile(currentIndent + lbl1 + ":");
            enterAssignStatement(assignStmt);
            assignments.add(assignStmt);

            // write MIPS branch
            writeToMIPSFile(currentIndent + "bgtz " + temp + ", " + lbl2);

            // TODO handle false block
            List<tacParser.StatementContext> fbStatements = falseBlock.statement();
            for (tacParser.StatementContext stmt : fbStatements) {
                if (stmt.assignStatement() != null) {
                    enterAssignStatement(stmt.assignStatement());
                    assignments.add(stmt.assignStatement());
                } else if (stmt.ifStatement() != null) {
                    enterIfStatement(stmt.ifStatement());
                    ifs.add(stmt.ifStatement());
                } else if (stmt.whileStatement() != null) {
                    enterWhileStatement(stmt.whileStatement());
                    whiles.add(stmt.whileStatement());
                }
            }

            // branch to label 1
            writeToMIPSFile(currentIndent + "b " + lbl1);

            // write label 2
            writeToMIPSFile(currentIndent + lbl2 + ":");

        }
    }

    @Override
    public void enterIfStatement(tacParser.IfStatementContext ctx) {
        if (!ifs.contains(ctx)) {
            // get involved variables and labels
            List<tacParser.LocationContext> locations = ctx.location();
            String temp = locations.get(0).getText();
            String lbl1 = locations.get(1).getText();
            String lbl2 = locations.get(2).getText();

            temp = temp.replace("_", "$");

            // write MIPS branch
            writeToMIPSFile(currentIndent + "bgtz " + temp + ", " + lbl1);

            // get blocks
            List<tacParser.BlockContext> blocks = ctx.block();
            tacParser.BlockContext falseBlock = blocks.get(0);
            tacParser.BlockContext trueBlock = blocks.get(1);

            // TODO handle false block
            List<tacParser.StatementContext> fbStatements = falseBlock.statement();
            for (tacParser.StatementContext stmt : fbStatements) {
                if (stmt.assignStatement() != null) {
                    enterAssignStatement(stmt.assignStatement());
                    assignments.add(stmt.assignStatement());
                } else if (stmt.ifStatement() != null) {
                    enterIfStatement(stmt.ifStatement());
                    ifs.add(stmt.ifStatement());
                } else if (stmt.whileStatement() != null) {
                    enterWhileStatement(stmt.whileStatement());
                    whiles.add(stmt.whileStatement());
                }
            }

            // make jump to label 2
            writeToMIPSFile(currentIndent + "b " + lbl2);

            // label 1
            writeToMIPSFile(currentIndent + lbl1 + ":");

            // TODO handle true block
            List<tacParser.StatementContext> tbStatements = trueBlock.statement();
            for (tacParser.StatementContext stmt : tbStatements) {
                if (stmt.assignStatement() != null) {
                    enterAssignStatement(stmt.assignStatement());
                    assignments.add(stmt.assignStatement());
                } else if (stmt.ifStatement() != null) {
                    enterIfStatement(stmt.ifStatement());
                    ifs.add(stmt.ifStatement());
                } else if (stmt.whileStatement() != null) {
                    enterWhileStatement(stmt.whileStatement());
                    whiles.add(stmt.whileStatement());
                }
            }

            // label 2
            writeToMIPSFile(currentIndent + lbl2 + ":");

            // add if context to list
            ifs.add(ctx);
        }
    }

    @Override
    public void enterAssignStatement(tacParser.AssignStatementContext ctx) {
        boolean isEqExpr = false;
        if (ctx.location().getText().equals(ctx.expression().getText()))
            isEqExpr = true;

        if (!assignments.contains(ctx) && !isEqExpr) {
            // separate location and expression
            tacParser.LocationContext location = ctx.location();
            tacParser.ExpressionContext expression = ctx.expression();

            String locRegister = "";
            String locRegisterData = "";
            // check if location has a termprary register in it
            if (location.getText().contains("_t")) {
                locRegister = location.getText().replace("_t", "$t");
                temporariesStack.remove(locRegister);                       // remove register from available registers
            } else {
                locRegister = getNextAvailableSVR();
                locRegisterData = location.getText() + "_" + currentContext;
            }

            // set assign register
            registersInUse[0] = locRegister;

            // get the operator for expression
            String operator = getOperatorFromExpression(expression);
            if (!operator.equals("$err_no_op")) {
                // expression has two operands
                // separate the operands
                String[] operands = splitTac(expression.getText());

                int i = 1;
                for (String operand : operands) {
                    if (operand.contains("_t")) {
                        registersInUse[i] = operand.replace("_t", "$t");
                    } else {
                        // operand is data in memory, so we should load it MIPS style

                        // first change the name of the variable so it matchs context
                        String varName = operand + "_" + currentContext;

                        // get a register where to load the data
                        String register = getNextAvailableTR();
                        registersInUse[i] = register;

                        // load it in MIPS
                        writeToMIPSFile(currentIndent + "lw " + register + ", " + varName + dataIndent + "# ld " +
                                "data " + varName);
                    }
                    i = i + 1;
                }

                // registersInUse now stores three registers: [0] contains register to store operation, [1] and [2] the values
                // find out what is the operation

                String operationIs = getMIPSOperation(operator);
                if (!operationIs.equals("$err_invalid_op")) {
                    if (!operationIs.equals("mult") && !operationIs.equals("div")) {
                        // write operation in MIPS
                        if (operationIs.equals("slt")) {
                            // write less than in MIPS and assign to register
                            writeToMIPSFile(currentIndent + operationIs + " " + registersInUse[0] + ", " + registersInUse[1] + ", "
                                    + registersInUse[2]);

                            this.temporariesStack.push(registersInUse[1]);
                            this.temporariesStack.push(registersInUse[2]);

                            // now check if is greater than zero. 1 is true, 0 is false or less than
                            // TODO

                        } else {
                            writeToMIPSFile(currentIndent + operationIs + " " + registersInUse[0] + ", " + registersInUse[1] + ", "
                                    + registersInUse[2]);

                            // free registers except [0]
                            this.temporariesStack.push(registersInUse[1]);
                            this.temporariesStack.push(registersInUse[2]);

                            // check if there's need to store
                            if (!locRegisterData.equals("")) {
                                // we need to store
                                writeToMIPSFile(currentIndent + "sw " + registersInUse[0] + ", " + locRegisterData + dataIndent +
                                        "# str data");
                                writeToMIPSFile("\n");

                                // since we did a store we can free the registers
                                this.savedValuesStack.push(registersInUse[0]);
                                this.temporariesStack.push(registersInUse[1]);
                                this.temporariesStack.push(registersInUse[2]);
                            }
                        }
                    } else {
                        writeToMIPSFile(currentIndent + operationIs + " " + registersInUse[1] + ", " + registersInUse[2]);
                        writeToMIPSFile(currentIndent + "mflo " + registersInUse[0]);

                        // check if there's need to store
                        if (!locRegisterData.equals("")) {
                            // we need to store
                            writeToMIPSFile(currentIndent + "sw " + registersInUse[0] + ", " + locRegisterData + dataIndent +
                                    "# str data");
                            writeToMIPSFile("\n");

                            // since we did a store we can free the registers
                            this.savedValuesStack.push(registersInUse[0]);
                        }

                        // free registers except [0]
                        this.temporariesStack.push(registersInUse[1]);
                        this.temporariesStack.push(registersInUse[2]);
                    }
                }

            } else {
                // TODO handle here possible immediate values and other stuff
                String exp = expression.getText();

                // check for inmmediate value
                try {
                    int immediate = Integer.parseInt(exp);

                    // check if there's need to store
                    if (!locRegisterData.equals("")) {
                        writeToMIPSFile(currentIndent + "li " + registersInUse[0] + ", " + String.valueOf(immediate));
                        // we need to store
                        writeToMIPSFile(currentIndent + "sw " + registersInUse[0] + ", " + locRegisterData + dataIndent +
                                "# str data");

                        // since we did a store we can free the registers
                        this.savedValuesStack.push(registersInUse[0]);
                    } else {
                        writeToMIPSFile(currentIndent + "li " + registersInUse[0] + ", " + String.valueOf(immediate));
                    }
                } catch (Exception e) {
                    // not an immediate value
                    if (exp.contains("_t")) {
                        registersInUse[1] = exp.replace("_t", "$t");
                    } else {
                        // operand is data in memory, so we should load it MIPS style

                        // first change the name of the variable so it matchs context
                        String varName = exp + "_" + currentContext;

                        // get a register where to load the data
                        String register = getNextAvailableTR();
                        registersInUse[1] = register;

                        // load it in MIPS
                        writeToMIPSFile(currentIndent + "lw " + register + ", " + varName + dataIndent + "# ld " +
                                "data " + varName);
                    }

                    // make assignment
                    if (!locRegisterData.equals("")) {
                        writeToMIPSFile(currentIndent + "move " + registersInUse[0] + ", " + registersInUse[1]);
                        // we need to store
                        writeToMIPSFile(currentIndent + "sw " + registersInUse[0] + ", " + locRegisterData + dataIndent +
                                "# str data");

                        // since we did a store we can free the registers
                        this.savedValuesStack.push(registersInUse[0]);
                        this.temporariesStack.push(registersInUse[1]);
                    } else {
                        writeToMIPSFile(currentIndent + "move " + registersInUse[0] + ", " + registersInUse[1]);
                    }
                }
            }
        }
    }

    private String getMIPSOperation(String operator) {
        if (operator.equals("+")) {
            return "add";
        } else if (operator.equals("&&")) {
            return "and";
        } else if (operator.equals("/")) {
            return "div";
        } else if (operator.equals("*")) {
            return "mult";
        } else if (operator.equals("||")) {
            return "or";
        } else if (operator.equals("-")) {
            return "sub";
        } else if (operator.equals("<")) {
            return "slt";
        } else return "$err_invalid_op";
    }

    private String getOperatorFromExpression(tacParser.ExpressionContext expression) {
        if (expression.and_op() != null) {
            return expression.and_op().getText();
        } else if (expression.or_op() != null) {
            return expression.or_op().getText();
        } else if (expression.eq_op() != null) {
            return expression.eq_op().getText();
        } else if (expression.rel_op() != null) {
            return expression.rel_op().getText();
        } else if (expression.modulus_op() != null) {
            return expression.modulus_op().getText();
        } else if (expression.div_op() != null) {
            return expression.div_op().getText();
        } else if (expression.mul_op() != null) {
            return expression.mul_op().getText();
        } else if (expression.arith_op_sum_subs() != null) {
            return expression.arith_op_sum_subs().getText();
        } else return "$err_no_op";
    }

    private String[] splitTac(String toSplit){
        return toSplit.split("\\+|-|\\*|/|%|&&|\\|\\||==|!=|<|<=|>|>=");
    }

    private void fillRegistersStack() {
        this.temporariesStack.clear();
        this.savedValuesStack.clear();

        for (int i = 9; i >= 0; i--) {
            this.temporariesStack.push("$t" + String.valueOf(i));
        }

        for (int i = 7; i >= 0; i--) {
            this.savedValuesStack.push("$s" + String.valueOf(i));
        }
    }

    // To obtain next available temporary register
    private String getNextAvailableTR() {
        if (!this.temporariesStack.isEmpty()) {
            return this.temporariesStack.pop();
        } else return "$err";
    }

    // To obtain next available saved value register
    private String getNextAvailableSVR() {
        if (!this.savedValuesStack.isEmpty()) {
            return this.savedValuesStack.pop();
        } else return "$err";
    }

    private void incrementIndent() {
        this.currentIndent = this.currentIndent + "\t";
    }

    private void decrementIndent() {
        this.currentIndent = this.currentIndent.substring(0, this.currentIndent.length()-1);
    }

    private void writeDataInMemory(String methodName, String varName, String type) {
        if (type.equals("int")) {
            writeToMIPSFile(currentIndent + varName + "_" + methodName + ":" + dataIndent + ".word 0");
        } else if (type.equals("char")) {
            writeToMIPSFile(currentIndent + varName + "_" + methodName + ":" + dataIndent + ".asciiz \"\"");
        }
    }

    private void writeToMIPSFile(String line) {
        try(FileWriter fw = new FileWriter("generated_mips.asm", true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(line);
        } catch (IOException e) {
        }
    }
}