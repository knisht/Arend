package com.jetbrains.jetpad.vclang.term.definition;

import com.jetbrains.jetpad.vclang.term.Abstract;
import com.jetbrains.jetpad.vclang.term.context.param.DependentLink;
import com.jetbrains.jetpad.vclang.term.context.param.EmptyDependentLink;
import com.jetbrains.jetpad.vclang.term.expr.ClassCallExpression;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.sort.SortMax;
import com.jetbrains.jetpad.vclang.term.expr.subst.LevelArguments;
import com.jetbrains.jetpad.vclang.term.expr.type.PiUniverseType;
import com.jetbrains.jetpad.vclang.term.expr.type.TypeMax;
import com.jetbrains.jetpad.vclang.term.internal.FieldSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.ClassCall;
import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.param;

public class ClassDefinition extends Definition {
  private final FieldSet myFieldSet;
  private final Set<ClassDefinition> mySuperClasses;
  private boolean myHasErrors;

  private ClassField myEnclosingThisField = null;

  public ClassDefinition(Abstract.ClassDefinition abstractDef) {
    this(abstractDef, new FieldSet(), new HashSet<ClassDefinition>());
  }

  public ClassDefinition(Abstract.ClassDefinition abstractDef, FieldSet fieldSet, Set<ClassDefinition> superClasses) {
    super(abstractDef);
    myFieldSet = fieldSet;
    mySuperClasses = superClasses;
    myHasErrors = false;
  }

  @Override
  public Abstract.ClassDefinition getAbstractDefinition() {
    return (Abstract.ClassDefinition) super.getAbstractDefinition();
  }

  public FieldSet getFieldSet() {
    return myFieldSet;
  }

  public SortMax getSorts() {
    return myFieldSet.getSorts(ClassCall(this, myFieldSet));
  }

  public boolean isSubClassOf(ClassDefinition classDefinition) {
    if (this.equals(classDefinition)) return true;
    for (ClassDefinition superClass : mySuperClasses) {
      if (superClass.isSubClassOf(classDefinition)) return true;
    }
    return false;
  }

  @Override
  public TypeMax getTypeWithParams(List<DependentLink> params, LevelArguments polyArguments) {
    DependentLink link = EmptyDependentLink.getInstance();
    if (getThisClass() != null) {
      link = param(ClassCall(getThisClass()));
    }
    return new PiUniverseType(link, getSorts().subst(polyArguments.toLevelSubstitution(this)));
  }

  @Override
  public ClassCallExpression getDefCall(LevelArguments polyArguments) {
    return ClassCall(this, polyArguments, myFieldSet);
  }

  @Override
  public ClassCallExpression getDefCall(LevelArguments polyArguments, List<Expression> args) {
    return new ClassCallExpression(this, polyArguments, myFieldSet);
  }

  @Override
  public void setThisClass(ClassDefinition enclosingClass) {
    assert myEnclosingThisField == null;
    super.setThisClass(enclosingClass);
    if (enclosingClass != null) {
      myEnclosingThisField = new ClassField(null, ClassCall(enclosingClass), this, param("\\this", ClassCall(this)));
      myEnclosingThisField.setThisClass(this);
      myFieldSet.addField(myEnclosingThisField, ClassCall(this, myFieldSet));
    }
  }

  @Override
  public boolean typeHasErrors() {
    return false;
  }

  @Override
  public TypeCheckingStatus hasErrors() {
    return myHasErrors ? TypeCheckingStatus.HAS_ERRORS : TypeCheckingStatus.NO_ERRORS;
  }

  public void hasErrors(boolean has) {
    myHasErrors = has;
  }

  public ClassField getEnclosingThisField() {
    return myEnclosingThisField;
  }
}
