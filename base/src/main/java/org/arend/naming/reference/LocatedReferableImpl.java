package org.arend.naming.reference;

import org.arend.core.definition.Definition;
import org.arend.ext.reference.Precedence;
import org.arend.module.ModuleLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LocatedReferableImpl implements TCReferable {
  private final Precedence myPrecedence;
  private final String myName;
  private final LocatedReferable myParent;
  private final Kind myKind;
  private Definition myTypechecked;

  public LocatedReferableImpl(Precedence precedence, String name, @Nullable LocatedReferable parent, Kind kind) {
    assert kind.isTypecheckable() || kind == Kind.OTHER || parent instanceof TCReferable;
    myPrecedence = precedence;
    myName = name;
    myParent = parent;
    myKind = kind;
  }

  public LocatedReferableImpl(Precedence precedence, String name, @NotNull ModuleLocation parent, Kind kind) {
    myPrecedence = precedence;
    myName = name;
    myParent = new FullModuleReferable(parent);
    myKind = kind;
  }

  @NotNull
  @Override
  public Precedence getPrecedence() {
    return myPrecedence;
  }

  @NotNull
  @Override
  public String textRepresentation() {
    return myName;
  }

  @Override
  public @NotNull TCReferable getTypecheckable() {
    return myKind.isTypecheckable() || myKind == Kind.OTHER ? this : (TCReferable) myParent;
  }

  @Override
  public void setTypechecked(Definition definition) {
    myTypechecked = definition;
  }

  @Override
  public Definition getTypechecked() {
    return myTypechecked;
  }

  @NotNull
  @Override
  public Kind getKind() {
    return myKind;
  }

  @Nullable
  @Override
  public ModuleLocation getLocation() {
    return myParent == null ? null : myParent.getLocation();
  }

  @Nullable
  @Override
  public LocatedReferable getLocatedReferableParent() {
    return myParent;
  }

  @Override
  public String toString() {
    return myName;
  }

  @Nullable
  @Override
  public Object getData() {
    return null;
  }
}
