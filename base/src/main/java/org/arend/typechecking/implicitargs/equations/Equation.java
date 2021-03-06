package org.arend.typechecking.implicitargs.equations;

import org.arend.core.context.binding.inference.InferenceVariable;
import org.arend.core.expr.Expression;
import org.arend.core.expr.InferenceReferenceExpression;
import org.arend.ext.core.ops.CMP;
import org.arend.term.concrete.Concrete;

public class Equation implements InferenceVariableListener {
  public Expression expr1;
  public Expression expr2;
  public Expression type;
  public final CMP cmp;
  public final Concrete.SourceNode sourceNode;

  public Equation(Expression expr1, Expression expr2, Expression type, CMP cmp, Concrete.SourceNode sourceNode) {
    this.expr1 = expr1;
    this.expr2 = expr2;
    this.type = type;
    this.cmp = cmp;
    this.sourceNode = sourceNode;
  }

  public Expression getLowerBound() {
    return cmp != CMP.GE ? expr1 : expr2;
  }

  public Expression getUpperBound() {
    return cmp != CMP.GE ? expr2 : expr1;
  }

  @Override
  public void solved(Equations equations, InferenceReferenceExpression referenceExpression) {
    InferenceVariable var1 = expr1.getInferenceVariable();
    InferenceVariable var2 = expr2.getInferenceVariable();
    if (var1 != null) {
      var1.removeListener(this);
    }
    if (var2 != null) {
      var2.removeListener(this);
    }
    if (equations.remove(this)) {
      equations.solve(expr1, expr2, type, cmp, sourceNode);
    }
  }
}
