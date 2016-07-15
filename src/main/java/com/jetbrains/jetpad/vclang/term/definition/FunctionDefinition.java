package com.jetbrains.jetpad.vclang.term.definition;

import com.jetbrains.jetpad.vclang.naming.namespace.Namespace;
import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.context.param.EmptyDependentLink;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.FunCallExpression;
import com.jetbrains.jetpad.vclang.term.expr.sort.Sort;
import com.jetbrains.jetpad.vclang.term.expr.subst.LevelSubstitution;
import com.jetbrains.jetpad.vclang.term.expr.subst.Substitution;
import com.jetbrains.jetpad.vclang.term.pattern.elimtree.ElimTreeNode;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.FunCall;

public class FunctionDefinition extends Definition implements Function {
  private DependentLink myParameters;
  private Expression myResultType;
  private ElimTreeNode myElimTree;
  private boolean myTypeHasErrors;
  private final Namespace myOwnNamespace;

  public FunctionDefinition(String name, Abstract.Definition.Precedence precedence, Namespace ownNamespace) {
    super(name, precedence);
    myOwnNamespace = ownNamespace;
    myTypeHasErrors = true;
    myParameters = EmptyDependentLink.getInstance();
  }

  public FunctionDefinition(String name, Abstract.Definition.Precedence precedence, Namespace ownNamespace, DependentLink parameters, Expression resultType, ElimTreeNode elimTree) {
    super(name, precedence);
    assert parameters != null;
    myOwnNamespace = ownNamespace;
    hasErrors(false);
    myParameters = parameters;
    myResultType = resultType;
    myTypeHasErrors = false;
    myElimTree = elimTree;
  }

  public FunctionDefinition(String name, Abstract.Definition.Precedence precedence, Namespace ownNamespace, DependentLink parameters, Expression resultType, ElimTreeNode elimTree, Sort universe) {
    super(name, precedence, universe);
    assert parameters != null;
    myOwnNamespace = ownNamespace;
    hasErrors(false);
    myParameters = parameters;
    myResultType = resultType;
    myTypeHasErrors = false;
    myElimTree = elimTree;
  }

  @Override
  public ElimTreeNode getElimTree() {
    return myElimTree;
  }

  @Override
  public boolean isAbstract() {
    return myElimTree == null;
  }

  public void setElimTree(ElimTreeNode elimTree) {
    myElimTree = elimTree;
  }

  @Override
  public DependentLink getParameters() {
    return myParameters;
  }

  public void setParameters(DependentLink parameters) {
    assert parameters != null;
    myParameters = parameters;
  }

  @Override
  public Expression getResultType() {
    return myResultType;
  }

  @Override
  public int getNumberOfRequiredArguments() {
    return DependentLink.Helper.size(myParameters);
  }

  public void setResultType(Expression resultType) {
    myResultType = resultType;
  }

  public boolean typeHasErrors() {
    return myTypeHasErrors;
  }

  public void typeHasErrors(boolean has) {
    myTypeHasErrors = has;
  }

  @Override
  public Expression getType() {
    if (myTypeHasErrors) {
      return null;
    }
    return Function.Helper.getFunctionType(this);
  }

  @Override
  public FunCallExpression getDefCall() {
    return FunCall(this);
  }

  @Override
  public Namespace getOwnNamespace() {
    return myOwnNamespace;
  }

  @Override
  public FunctionDefinition substPolyParams(LevelSubstitution substitution) {
    if (!isPolymorphic() || myTypeHasErrors) {
      return this;
    }

    Substitution subst = new Substitution(substitution);
    DependentLink newParams = DependentLink.Helper.subst(myParameters, subst);

    return new FunctionDefinition(getName(), getPrecedence(), getOwnNamespace(), newParams,
        myResultType.subst(subst), myElimTree /* .subst(subst) /**/, getSort().subst(subst.LevelSubst));
  }
}