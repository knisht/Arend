package com.jetbrains.jetpad.vclang.term.definition;

import com.jetbrains.jetpad.vclang.module.Namespace;
import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.expr.ArgumentExpression;
import com.jetbrains.jetpad.vclang.term.expr.ConCallExpression;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.arg.TelescopeArgument;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;
import com.jetbrains.jetpad.vclang.term.pattern.PatternArgument;

import java.util.List;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;
import static com.jetbrains.jetpad.vclang.term.expr.arg.Utils.numberOfVariables;
import static com.jetbrains.jetpad.vclang.term.pattern.Utils.constructorPatternsToExpressions;
import static com.jetbrains.jetpad.vclang.term.pattern.Utils.getNumArguments;

public class Constructor extends Definition {
  private DataDefinition myDataType;
  private List<TypeArgument> myArguments;
  private List<PatternArgument> myPatterns;

  public Constructor(Namespace parentNamespace, Name name, Abstract.Definition.Precedence precedence, DataDefinition dataType) {
    super(parentNamespace, name, precedence);
    myDataType = dataType;
  }

  public Constructor(Namespace parentNamespace, Name name, Abstract.Definition.Precedence precedence, Universe universe, List<TypeArgument> arguments, DataDefinition dataType, List<PatternArgument> patterns) {
    super(parentNamespace, name, precedence);
    setUniverse(universe);
    hasErrors(false);
    myDataType = dataType;
    myArguments = arguments;
    myPatterns = patterns;
  }

  public Constructor(Namespace parentNamespace, Name name, Abstract.Definition.Precedence precedence, Universe universe, List<TypeArgument> arguments, DataDefinition dataType) {
    this(parentNamespace, name, precedence, universe, arguments, dataType, null);
  }

  public List<PatternArgument> getPatterns() {
    return myPatterns;
  }

  public void setPatterns(List<PatternArgument> patterns) {
    myPatterns = patterns;
  }

  public List<TypeArgument> getArguments() {
    return myArguments;
  }

  public void setArguments(List<TypeArgument> arguments) {
    myArguments = arguments;
  }

  public DataDefinition getDataType() {
    return myDataType;
  }

  public void setDataType(DataDefinition dataType) {
    myDataType = dataType;
  }

  public int getNumberOfAllParameters() {
    if (myPatterns == null) {
      return myDataType.getNumberOfAllParameters();
    } else {
      return getNumArguments(myPatterns) + (myDataType.getThisClass() == null ? 0 : 1);
    }
  }

  @Override
  public Expression getBaseType() {
    Expression resultType = DataCall(myDataType);
    int numberOfVars = numberOfVariables(myArguments);
    int numberOfParams = numberOfVariables(myDataType.getParameters());
    if (myDataType.getThisClass() != null) {
      resultType = Apps(resultType, new ArgumentExpression(Index(numberOfVars + numberOfParams), true, false));
    }
    if (myPatterns == null) {
      for (int i = numberOfParams - 1, j = 0; i >= 0; ++j) {
        if (myDataType.getParameters().get(j) instanceof TelescopeArgument) {
          for (String ignored : ((TelescopeArgument) myDataType.getParameters().get(j)).getNames()) {
            resultType = Apps(resultType, new ArgumentExpression(Index(i-- + numberOfVars), myDataType.getParameters().get(j).getExplicit(), !myDataType.getParameters().get(j).getExplicit()));
          }
        } else {
          resultType = Apps(resultType, new ArgumentExpression(Index(i-- + numberOfVars), myDataType.getParameters().get(j).getExplicit(), !myDataType.getParameters().get(j).getExplicit()));
        }
      }
    } else {
      List<ArgumentExpression> args = constructorPatternsToExpressions(this);
      for (ArgumentExpression arg : args) {
        resultType = Apps(resultType, arg);
      }
    }
    return myArguments.isEmpty() ? resultType : Pi(myArguments, resultType);
  }

  @Override
  public ConCallExpression getDefCall() {
    return ConCall(this);
  }
}
