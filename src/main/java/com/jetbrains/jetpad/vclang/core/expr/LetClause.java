package com.jetbrains.jetpad.vclang.core.expr;

import com.jetbrains.jetpad.vclang.core.context.binding.NamedBinding;
import com.jetbrains.jetpad.vclang.core.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.core.definition.Function;
import com.jetbrains.jetpad.vclang.core.pattern.elimtree.ElimTreeNode;
import com.jetbrains.jetpad.vclang.core.sort.Level;

import java.util.List;

public class LetClause extends NamedBinding implements Function {
  private List<Level> myPLevels;
  private DependentLink myParameters;
  private ElimTreeNode myElimTree;
  private Expression myResultType;

  public LetClause(String name, List<Level> pLevels, DependentLink parameters, Expression resultType, ElimTreeNode elimTree) {
    super(name);
    assert parameters != null;
    myPLevels = pLevels;
    myParameters = parameters;
    myResultType = resultType;
    myElimTree = elimTree;
  }

  @Override
  public ElimTreeNode getElimTree() {
    return myElimTree;
  }

  public void setElimTree(ElimTreeNode elimTree) {
    myElimTree = elimTree;
  }

  public List<Level> getPLevels() {
    return myPLevels;
  }

  public void setPLevels(List<Level> pLevels) {
    myPLevels = pLevels;
  }

  @Override
  public DependentLink getParameters() {
    return myParameters;
  }

  public Expression getResultType() {
    return myResultType;
  }

  public void setResultType(Expression resultType) {
    myResultType = resultType;
  }

  @Override
  public int getNumberOfRequiredArguments() {
    return DependentLink.Helper.size(myParameters);
  }

  @Override
  public Expression getType() {
    return myParameters.hasNext() ? new PiExpression(myPLevels, myParameters, myResultType) : myResultType;
  }
}
