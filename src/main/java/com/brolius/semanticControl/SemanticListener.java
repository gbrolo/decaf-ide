package com.brolius.semanticControl;

import com.brolius.antlr.decafBaseListener;
import com.brolius.antlr.decafParser;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

import java.util.LinkedList;
import java.util.List;

public class SemanticListener extends decafBaseListener {
    private decafParser parser;
    private boolean foundMain; // control for existance of 'main' method

    private List<String> semanticErrorsList; // list for semantic errors found
    private List<MethodElement> methodFirms; // a list for the found methods
    private List<VarElement> varList;        // a list for the variables

    private MethodElement currentMethodContext; // to check the current context of variable declarations and operations

    public SemanticListener(decafParser parser) {
        this.parser = parser;
        this.foundMain = false;
        this.semanticErrorsList = new LinkedList<>();
        this.methodFirms = new LinkedList<>();
        this.varList = new LinkedList<>();

        currentMethodContext = new MethodElement("void", "global", new LinkedList<>());
    }

    @Override
    public void enterStatement(decafParser.StatementContext ctx) {
        if (ctx.getText().contains("return")) {
            String[] lineSplit = ctx.getText().replace(";", "").split("return");
            String returnVal = lineSplit[lineSplit.length-1];

            if (currentMethodContext.getType().equals("void")) {
                // should not return nothing
                semanticErrorsList.add("Method <strong>" + currentMethodContext.getFirm() + "</strong> " +
                        "declared as void. Invalid return statement.");
            }
        }

        // TODO if
        if (ctx.getText().contains("if(")) {
            System.out.println("found if statement");
        }
    }

    @Override
    public void enterParameter(decafParser.ParameterContext ctx) {
        String parameterType = "";
        String ID = "";

        if (ctx.parameterType() != null) {
            parameterType = ctx.parameterType().getText();
        }

        if (ctx.ID() != null) {
            ID = ctx.ID().getText();
        }

        // Check if variable has already been declared in the same context
        VarElement newVar = new VarElement(parameterType, ID, currentMethodContext);

        boolean hasVarBeenDeclared = false;

        if (varList.isEmpty()) {
            hasVarBeenDeclared = true;
            varList.add(newVar);
        } else {
            for (VarElement listVar : varList) {
                if (listVar.equals(newVar)) {
                    hasVarBeenDeclared = true;
                    semanticErrorsList.add("Variable <strong>\"" + ID + "\"</strong> has " +
                            "already been declared in the context of <strong>" + currentMethodContext.getFirm() + "</strong>");
                    break;
                }
            }
        }

        if (!hasVarBeenDeclared) { varList.add(newVar); }

    }

    @Override
    public void enterLocation(decafParser.LocationContext ctx) {
        String ID = "";

        if (ctx.ID() != null) {
            ID = ctx.ID().getText();
        }

        // verify if ID exits. If not, the location is invalid, since the variable does not exist.
        boolean isLocationDefined = false;
        // location can be a variable
        for (VarElement listVar : varList) {
            if ((listVar.getID().equals(ID)) && (listVar.getContext().equals(currentMethodContext))) {
                // the location exists
                isLocationDefined = true;
                break;
            } else if ((listVar.getID().equals(ID)) && (listVar.getContext().getFirm().equals("global"))) {
                // the location refers to a global variable
                isLocationDefined = true;
                break;
            }
        }

        // location can be a call to a function
//        for (MethodElement me : methodFirms) {
//            if (me.getFirm().equals(ID)) {
//                isLocationDefined = true;
//                break;
//            }
//        }

        if (!isLocationDefined) {
            semanticErrorsList.add("Variable <strong>\"" + ID + "\"</strong> is not defined. ");
        }
    }

    @Override
    public void enterVarDeclaration(decafParser.VarDeclarationContext ctx) {
        String varType = "";    // type
        String ID = "";         // name of var
        String num = "";        // if var is array, num is the number of elements

        if (ctx.varType() != null) {
            varType = ctx.varType().getText();
        }

        if (ctx.ID() != null) {
            ID = ctx.ID().getText();
        }

        if (ctx.NUM() != null) {
            num = ctx.NUM().getText();
        }

        // Check if variable has already been declared in the same context
        VarElement newVar = new VarElement(varType, ID, currentMethodContext);
        if (!num.equals("")) {
            newVar.setNUM(num);
            if (Integer.parseInt(num) == 0) {
                semanticErrorsList.add("<strong>\"" + ID + "\"</strong> length " +
                        "has to be bigger than 0.");
            }
        }

        boolean hasVarBeenDeclared = false;

        if (varList.isEmpty()) {
            hasVarBeenDeclared = true;
            varList.add(newVar);
        } else {
            for (VarElement listVar : varList) {
                if (listVar.equals(newVar)) {
                    hasVarBeenDeclared = true;
                    semanticErrorsList.add("Variable <strong>\"" + ID + "\"</strong> has " +
                            "already been declared in the context of <strong>" + currentMethodContext.getFirm() + "</strong>");
                    break;
                }
            }
        }

        if (!hasVarBeenDeclared) { varList.add(newVar); }

    }

    @Override
    public void enterMethodCall(decafParser.MethodCallContext ctx) {
        String firm = ""; // method firm
        if (ctx.ID() != null) {
            firm = ctx.ID().getText();
        }

        // verify if method call is valid, this means if the method has been correctly declared before
        boolean isMethodDeclared = false;
        for (MethodElement listMethod : methodFirms) {
            if (listMethod.getFirm().equals(firm)) { isMethodDeclared = true; break; }
        }

        if (!isMethodDeclared) { semanticErrorsList.add("Can't make call to undeclared method " + firm); }
    }

    @Override
    public void enterMethodDeclaration(decafParser.MethodDeclarationContext ctx) {
        // gather values
        TokenStream tokens = parser.getTokenStream();
        String type = ""; // method type
        String firm = ""; // method firm
        List<decafParser.ParameterContext> args = new LinkedList<>(); // args
        if (ctx.methodType()!= null) {
            type = tokens.getText(ctx.methodType());
        }

        if (ctx.ID() != null) {
            firm = ctx.ID().getText();
        }

        if (!ctx.parameter().isEmpty()) {
            args = ctx.parameter();
        }

        // existance of 'main' method
        if ((type.equals("void")) && firm.equals("main") && args.isEmpty()) {
            if (!foundMain) {
                foundMain = true;
                currentMethodContext = new MethodElement(type, firm, args);
                methodFirms.add(currentMethodContext); // add method to method list
                System.out.println(ctx.getRuleContext().toString());
            } else {
                semanticErrorsList.add("Method " + firm + " with arguments: " + args.toString() + " has" +
                        "already been declared.");
            }
        }
        if ((!type.equals("void")) && firm.equals("main") && args.isEmpty()) {
            foundMain = true;
            semanticErrorsList.add("'main' method must have a 'void' return type.");
        }
        if ((type.equals("void")) && firm.equals("main") && !args.isEmpty()) {
            foundMain = true;
            semanticErrorsList.add("'main' method can't have arguments.");
        }

        // rules for other methods that are not 'main'
        if (!firm.equals("main")) {
            MethodElement newMethod = new MethodElement(type, firm, args);
            currentMethodContext = newMethod;
            //System.out.println("nuevo metodo " + firm);
            //System.out.println(methodFirms.toString());

            boolean foundMethod = false;

            for (MethodElement listMethod : methodFirms) {
                if (listMethod.equals(newMethod)) {
                    semanticErrorsList.add("Method " + firm + " has " +
                            "already been declared.");
                    foundMethod = true;
                    break;
                }
            }

            if (!foundMethod) {
                methodFirms.add(newMethod);
            }

//            if (!methodFirms.contains(newMethod)) { methodFirms.add(newMethod); } else {
//                semanticErrorsList.add("Method " + firm + " with arguments: " + args.toString() + " has " +
//                        "already been declared.");
//            }
        }
    }

    public List<String> getSemanticErrorsList() {
        // existance of 'main' method
        if (!foundMain) { semanticErrorsList.add("No 'main' method declared."); }

        return semanticErrorsList;
    }
}
