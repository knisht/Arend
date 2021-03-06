package org.arend.frontend.reference;

import org.arend.ext.reference.Precedence;
import org.arend.frontend.parser.Position;
import org.arend.naming.reference.LocatedReferable;
import org.arend.naming.reference.TCReferable;
import org.arend.term.group.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalConcreteLocatedReferable extends ConcreteLocatedReferable implements Group.InternalReferable {
  private final boolean myVisible;

  public InternalConcreteLocatedReferable(Position position, @NotNull String name, Precedence precedence, @Nullable String aliasName, Precedence aliasPrecedence, boolean isVisible, TCReferable parent, Kind kind) {
    super(position, name, precedence, aliasName, aliasPrecedence, parent, kind);
    myVisible = isVisible;
  }

  @Override
  public LocatedReferable getReferable() {
    return this;
  }

  @Override
  public boolean isVisible() {
    return myVisible;
  }
}
