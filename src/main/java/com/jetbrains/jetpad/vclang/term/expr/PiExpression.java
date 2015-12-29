package com.jetbrains.jetpad.vclang.term.expr;

import com.jetbrains.jetpad.vclang.term.definition.Binding;
import com.jetbrains.jetpad.vclang.term.definition.Name;
import com.jetbrains.jetpad.vclang.term.definition.TypedBinding;
import com.jetbrains.jetpad.vclang.term.definition.Universe;
import com.jetbrains.jetpad.vclang.term.expr.arg.TelescopeArgument;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;
import com.jetbrains.jetpad.vclang.term.expr.visitor.ExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.jetpad.vclang.term.expr.arg.Utils.trimToSize;

public class PiExpression extends Expression {
  private final List<TypeArgument> myArguments;
  private final Expression myCodomain;

  public PiExpression(Expression domain, Expression codomain) {
    this(new ArrayList<TypeArgument>(1), codomain.liftIndex(0, 1));
    myArguments.add(new TypeArgument(true, domain));
  }

  public PiExpression(List<TypeArgument> arguments, Expression codomain) {
    myArguments = arguments;
    myCodomain = codomain;
  }

  public List<TypeArgument> getArguments() {
    return myArguments;
  }

  public Expression getCodomain() {
    return myCodomain;
  }

  @Override
  public Expression getType(List<Binding> context) {
    Universe universe = new Universe.Type(0, Universe.Type.PROP);
    int origSize = context.size();
    for (TypeArgument argument : myArguments) {
      Expression type = argument.getType().getType(context);
      if (!(type instanceof UniverseExpression)) return null;
      universe = universe.max(((UniverseExpression) type).getUniverse());
      if (universe == null) return null;

      if (argument instanceof TelescopeArgument) {
        for (String name : ((TelescopeArgument) argument).getNames()) {
          context.add(new TypedBinding(name, argument.getType()));
        }
      } else {
        context.add(new TypedBinding((Name) null, argument.getType()));
      }
    }

    Expression type = myCodomain.getType(context);
    if (!(type instanceof UniverseExpression)) return null;
    universe = universe.max(((UniverseExpression) type).getUniverse());

    trimToSize(context, origSize);
    return universe == null ? null : new UniverseExpression(universe);
  }

  @Override
  public <P, R> R accept(ExpressionVisitor<? super P, ? extends R> visitor, P params) {
    return visitor.visitPi(this, params);
  }
}
