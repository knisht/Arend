package org.arend.core.pattern;

import org.arend.core.context.param.DependentLink;
import org.arend.core.expr.Expression;
import org.arend.core.subst.ExprSubstitution;
import org.arend.core.subst.LevelSubstitution;
import org.arend.ext.error.ErrorReporter;
import org.arend.term.concrete.Concrete;
import org.arend.util.Decision;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ExpressionPattern extends Pattern {
  Expression toExpression();
  Decision match(Expression expression, List<Expression> result);
  boolean unify(ExprSubstitution idpSubst, ExpressionPattern other, ExprSubstitution substitution1, ExprSubstitution substitution2, ErrorReporter errorReporter, Concrete.SourceNode sourceNode);
  ExpressionPattern subst(ExprSubstitution exprSubst, LevelSubstitution levelSubst, Map<DependentLink, ExpressionPattern> patternSubst);
  Pattern removeExpressions();
  @Override @NotNull List<? extends ExpressionPattern> getSubPatterns();

  default Expression toPatternExpression() {
    return toExpression();
  }

  static List<Pattern> removeExpressions(List<? extends ExpressionPattern> patterns) {
    List<Pattern> result = new ArrayList<>();
    for (ExpressionPattern pattern : patterns) {
      result.add(pattern.removeExpressions());
    }
    return result;
  }

  static Decision match(List<? extends ExpressionPattern> patterns, List<? extends Expression> expressions, List<Expression> result) {
    assert patterns.size() == expressions.size();

    Decision decision = Decision.YES;
    for (int i = 0; i < patterns.size(); i++) {
      Decision subDecision = patterns.get(i).match(expressions.get(i), result);
      if (subDecision == Decision.NO) {
        return subDecision;
      }
      if (subDecision == Decision.MAYBE) {
        decision = Decision.MAYBE;
      }
    }

    return decision;
  }

  static boolean unify(List<? extends ExpressionPattern> patterns1, List<? extends ExpressionPattern> patterns2, ExprSubstitution idpSubst, ExprSubstitution substitution1, ExprSubstitution substitution2, ErrorReporter errorReporter, Concrete.SourceNode sourceNode) {
    assert patterns1.size() == patterns2.size();
    for (int i = 0; i < patterns1.size(); i++) {
      if (!patterns1.get(i).unify(idpSubst, patterns2.get(i), substitution1, substitution2, errorReporter, sourceNode)) {
        return false;
      }
    }
    return true;
  }
}
