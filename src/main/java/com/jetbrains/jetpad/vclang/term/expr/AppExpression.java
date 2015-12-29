package com.jetbrains.jetpad.vclang.term.expr;

import com.jetbrains.jetpad.vclang.term.definition.Binding;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;
import com.jetbrains.jetpad.vclang.term.expr.visitor.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class AppExpression extends Expression {
  private final Expression myFunction;
  private final ArgumentExpression myArgument;

  public AppExpression(Expression function, ArgumentExpression argument) {
    myFunction = function;
    myArgument = argument;
  }

  public Expression getFunction() {
    return myFunction;
  }

  public ArgumentExpression getArgument() {
    return myArgument;
  }

  @Override
  public <P, R> R accept(ExpressionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitApp(this, params);
  }

  @Override
  public Expression getType(List<Binding> context) {
    List<Expression> arguments = new ArrayList<>();
    Expression function = getFunction(arguments);
    Expression type = function.getType(context);
    if (!(type instanceof PiExpression)) return null;
    return type.splitAt(arguments.size(), new ArrayList<TypeArgument>(arguments.size()), context).subst(arguments, 0);
  }
}
