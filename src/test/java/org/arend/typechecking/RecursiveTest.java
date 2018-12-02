package org.arend.typechecking;

import org.arend.core.definition.Definition;
import org.arend.typechecking.error.TerminationCheckError;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class RecursiveTest extends TypeCheckingTestCase {
  @Test
  public void list() {
    assertSame(typeCheckDef("\\data List (A : \\Type0) | nil | cons A (List A)").status(), Definition.TypeCheckingStatus.NO_ERRORS);
  }

  @Test
  public void dataLeftError() {
    Definition def = typeCheckDef("\\data List (A : \\Type0) | nil | cons (List A -> A)", 1);
    assertEquals(Definition.TypeCheckingStatus.BODY_HAS_ERRORS, def.status());
  }

  @Test
  public void dataRightError() {
    Definition def = typeCheckDef("\\data List (B : \\oo-Type0 -> \\Type0) (A : \\Type0) | nil | cons (B (List B A))", 1);
    assertEquals(Definition.TypeCheckingStatus.BODY_HAS_ERRORS, def.status());
  }

  @Test
  public void plus() {
    assertSame(typeCheckDef("\\func \\infixr 9 + (x y : Nat) : Nat \\elim x | zero => y | suc x' => suc (x' + y)").status(), Definition.TypeCheckingStatus.NO_ERRORS);
  }

  @Test
  public void doubleRec() {
    assertSame(typeCheckDef("\\func \\infixr 9 + (x y : Nat) : Nat \\elim x | zero => y | suc zero => y | suc (suc x'') => x'' + (x'' + y)").status(), Definition.TypeCheckingStatus.NO_ERRORS);
  }

  @Test
  public void functionError() {
    assertSame(typeCheckDef("\\func \\infixr 9 + (x y : Nat) : Nat => x + y", 1).status(), Definition.TypeCheckingStatus.BODY_HAS_ERRORS);
  }

  @Test
  public void functionError2() {
    assertSame(typeCheckDef("\\func \\infixr 9 + (x y : Nat) : Nat \\elim x | zero => y | suc zero => y | suc (suc x'') => y + y", 1).status(), Definition.TypeCheckingStatus.BODY_HAS_ERRORS);
  }

  @Test
  public void functionPartiallyApplied() {
    assertSame(typeCheckDef("\\func foo (z : (Nat -> Nat) -> Nat) (x y : Nat) : Nat \\elim x | zero => y | suc x' => z (foo z x')").status(), Definition.TypeCheckingStatus.NO_ERRORS);
  }

  @Test
  public void cowithError() {
    typeCheckModule(
      "\\class C (x y : Nat)\n" +
      "\\func f (x : Nat) : C \\cowith | x => x | y => C.y {f x}", 1);
  }

  @Test
  public void mutualCowithError() {
    typeCheckModule(
      "\\class C (x y : Nat)\n" +
      "\\func f (x : Nat) : C \\cowith | x => x | y => C.y {g x}\n" +
      "\\func g (x : Nat) : C \\cowith | x => x | y => C.y {f x}", 2);
  }

  @Test
  public void headerBodyTest() {
    typeCheckModule(
      "\\func f (x : \\let t => g 0 \\in Nat) : \\Type | 0 => Nat | suc x => g x\n" +
      "\\func g (x : Nat) : \\Type | 0 => Nat | suc x => f x", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void bodyBodyTest() {
    typeCheckModule(
      "\\func f (x : Nat) : \\Type => g 0\n" +
      "\\func g (x : Nat) : \\Type => f 0", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void bodyBodyTest2() {
    typeCheckModule(
      "\\func f (x : Nat) : Nat => g 0\n" +
      "\\func g (x : Nat) : Nat => f 0", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void bodyBodyElimTest() {
    typeCheckModule(
      "\\func f (x : Nat) : \\Type | 0 => g 0 | suc _ => Nat\n" +
      "\\func g (x : Nat) : \\Type | 0 => f 0 | suc _ => Nat", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void bodyBodyElimTest2() {
    typeCheckModule(
      "\\func f (x : Nat) : Nat | 0 => g 0 | suc n => n \n" +
      "\\func g (x : Nat) : Nat | 0 => f 0 | suc n => n", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void headerTest() {
    typeCheckModule(
      "\\func f (n : \\let t => g 0 \\in Nat) : Nat | 0 => 0 | suc n => g n\n" +
      "\\func g (n : Nat) : Nat | 0 => 0 | suc n => f n", 2);
    assertThatErrorsAre(instanceOf(TerminationCheckError.class), instanceOf(TerminationCheckError.class));
  }

  @Test
  public void withType() {
    typeCheckDef("\\func f : Nat => f", 1);
  }

  @Test
  public void withoutType() {
    typeCheckDef("\\func f => f", 1);
  }

  @Test
  public void dataFunctionError() {
    typeCheckModule(
      "\\data D (n : Nat) : \\Type | con1 (f 1) | con2\n" +
      "\\func f (n : Nat) : \\Type => D n", 1);
  }

  @Test
  public void dataFunctionError2() {
    typeCheckModule(
      "\\data D (n : Nat) : \\Type | con1 (f 1) | con2\n" +
      "\\func f (n : Nat) : \\Type | 0 => Nat | suc n => D n", 1);
  }

  @Test
  public void dataFunctionError3() {
    typeCheckModule(
      "\\data D : \\Type | con1 | con2 (f (\\lam x => x))\n" +
      "\\func f (g : D -> D) : \\Type => g con1 = g con1", 2);
  }

  @Test
  public void mutualRecursionOrder() {
    typeCheckModule(
      "\\func g => D'\n" +
      "\\data D : \\Type | con1 | con2 (d : D) (D' d)\n" +
      "\\data D' (d : D) : \\Type \\with\n" +
      "  | con1 => con1'\n" +
      "  | con2 _ _ => con2'");
  }
}
