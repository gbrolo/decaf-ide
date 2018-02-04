package com.brolius.semanticControl;

public class VarElement {
    private String varType;
    private String ID;
    private int NUM;
    private boolean isArray;

    private MethodElement context;

    public VarElement(String varType, String ID, MethodElement context) {
        this.context = context;
        this.ID = ID;
        this.varType = varType;
        isArray = false;
    }

    public boolean equals(VarElement ve) {
        if ((this.ID.equals(ve.ID)) && (this.context.equals(ve.context))) {
            return true;
        } else return false;
    }

    public void setNUM(String num) {
        this.NUM = Integer.parseInt(num);
        isArray = true;
    }

    public String getID() { return ID; }
    public String getVarType() { return varType; }
    public int getNUM() { return NUM; }
    public MethodElement getContext() { return context; }
    public boolean isArray() { return isArray; }
}
