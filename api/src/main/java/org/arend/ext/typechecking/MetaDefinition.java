package org.arend.ext.typechecking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetaDefinition {
  @Nullable
  default TypedExpression invoke(@NotNull ExpressionTypechecker typechecker, @NotNull ContextData contextData) {
    return null;
  }
}
