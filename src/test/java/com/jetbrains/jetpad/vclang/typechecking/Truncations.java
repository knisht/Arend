package com.jetbrains.jetpad.vclang.typechecking;

import org.junit.Test;

import static com.jetbrains.jetpad.vclang.typechecking.TypeCheckingTestCase.typeCheckClass;
import static com.jetbrains.jetpad.vclang.typechecking.TypeCheckingTestCase.typeCheckDef;

public class Truncations {
  @Test
  public void elimInProp() {
    typeCheckDef("\\function inP-inv (P : \\Prop) (p : TrP P) : P <= \\elim p | inP p => p");
  }

  @Test
  public void setTruncationTests() {
    typeCheckClass(
        "\\static \\data TrS' (A : \\Type0)\n" +
        "    | inS' A\n" +
        "    | truncS' (a a' : TrS' A) (p q : a = a') I I\n" +
        "  \\with\n" +
        "    | truncS' a _ _ _ left _ => a\n" +
        "    | truncS' _ a' _ _ right _ => a'\n" +
        "    | truncS' _ _ p _ i left => p @ i\n" +
        "    | truncS' _ _ _ q i right => q @ i\n" +
        "\n" +
        "\\static \\function\n" +
        "set_trunc_test (A : \\Type0) (a a' : TrS' A) (p q : a = a') : TrS' A => truncS' a a' p q left left'n\n" +
        "\n" +
        "\\static \\function\n" +
        "set_trunc_test' (A : \\Type0) (a a' : TrS' A) (p q : a = a') : p = q => path (\\lam i => path (\\lam j => truncS' a a' p q j i))");
  }

  @Test
  public void dynamicSetTruncationTests() {
    typeCheckClass(
        "\\dynamic \\data TrS' (A : \\Type0)\n" +
        "    | inS' A\n" +
        "    | truncS' (a a' : TrS' A) (p q : a = a') I I\n" +
        "  \\with\n" +
        "    | truncS' a _ _ _ left _ => a\n" +
        "    | truncS' _ a' _ _ right _ => a'\n" +
        "    | truncS' _ _ p _ i left => p @ i\n" +
        "    | truncS' _ _ _ q i right => q @ i\n" +
        "\n" +
        "\\dynamic \\function\n" +
        "set_trunc_test (A : \\Type0) (a a' : TrS' A) (p q : a = a') : TrS' A => truncS' a a' p q left left'n\n" +
        "\n" +
        "\\dynamic \\function\n" +
        "set_trunc_test' (A : \\Type0) (a a' : TrS' A) (p q : a = a') : p = q => path (\\lam i => path (\\lam j => truncS' a a' p q j i))");
  }
}
