package com.jetbrains.jetpad.vclang.term;

import com.jetbrains.jetpad.vclang.module.Namespace;
import com.jetbrains.jetpad.vclang.module.RootModule;
import com.jetbrains.jetpad.vclang.term.definition.*;
import com.jetbrains.jetpad.vclang.term.expr.ArgumentExpression;
import com.jetbrains.jetpad.vclang.term.pattern.elimtree.BranchElimTreeNode;
import com.jetbrains.jetpad.vclang.term.expr.Expression;
import com.jetbrains.jetpad.vclang.term.expr.arg.Argument;
import com.jetbrains.jetpad.vclang.term.expr.arg.TypeArgument;

import java.util.*;

import static com.jetbrains.jetpad.vclang.term.expr.ExpressionFactory.*;

public class Prelude extends Namespace {
  public static ClassDefinition PRELUDE_CLASS;

  public static Namespace PRELUDE = new Prelude();

  public static DataDefinition NAT;
  public static Constructor ZERO, SUC;

  public static DataDefinition INTERVAL;
  public static Constructor LEFT, RIGHT;

  public static FunctionDefinition COERCE;

  public static DataDefinition PATH;
  public static FunctionDefinition PATH_INFIX;
  public static Constructor PATH_CON;

  public static FunctionDefinition AT;

  public static FunctionDefinition ISO;

  private static char[] specInfix = {'@', '='};
  private static String[] specPrefix = {"iso", "path", "Path"};

  static {
    PRELUDE_CLASS = new ClassDefinition(RootModule.ROOT, new Name("Prelude"));
    RootModule.ROOT.addDefinition(PRELUDE_CLASS);

    NAT = new DataDefinition(PRELUDE, new Name("Nat"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.SET), new ArrayList<TypeArgument>());
    Namespace natNamespace = PRELUDE.getChild(NAT.getName());
    ZERO = new Constructor(natNamespace, new Name("zero"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.PROP), new ArrayList<TypeArgument>(), NAT);
    SUC = new Constructor(natNamespace, new Name("suc"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.SET), args(TypeArg(DataCall(NAT))), NAT);
    NAT.addConstructor(ZERO);
    NAT.addConstructor(SUC);

    PRELUDE.addDefinition(NAT);
    PRELUDE.addDefinition(ZERO);
    PRELUDE.addDefinition(SUC);

    INTERVAL = new DataDefinition(PRELUDE, new Name("I"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.PROP), new ArrayList<TypeArgument>());
    Namespace intervalNamespace = PRELUDE.getChild(INTERVAL.getName());
    LEFT = new Constructor(intervalNamespace, new Name("left"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.PROP), new ArrayList<TypeArgument>(), INTERVAL);
    RIGHT = new Constructor(intervalNamespace, new Name("right"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.PROP), new ArrayList<TypeArgument>(), INTERVAL);
    Constructor abstractConstructor = new Constructor(intervalNamespace, new Name("<abstract>"), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(0, Universe.Type.PROP), new ArrayList<TypeArgument>(), INTERVAL);
    INTERVAL.addConstructor(LEFT);
    INTERVAL.addConstructor(RIGHT);
    INTERVAL.addConstructor(abstractConstructor);

    PRELUDE.addDefinition(INTERVAL);
    PRELUDE.addDefinition(LEFT);
    PRELUDE.addDefinition(RIGHT);

    List<Argument> coerceArguments = new ArrayList<>(3);
    coerceArguments.add(Tele(vars("type"), Pi(DataCall(INTERVAL), Universe(Universe.NO_LEVEL))));
    coerceArguments.add(Tele(vars("elem"), Apps(Index(0), ConCall(LEFT))));
    coerceArguments.add(Tele(vars("point"), DataCall(INTERVAL)));
    BranchElimTreeNode coerceElimTreeNode = branch(0, clause(LEFT, Abstract.Definition.Arrow.RIGHT, Index(0)));
    COERCE = new FunctionDefinition(PRELUDE, new Name("coe"), Abstract.Definition.DEFAULT_PRECEDENCE, coerceArguments, Apps(Index(2), Index(0)), coerceElimTreeNode);

    PRELUDE.addDefinition(COERCE);

    PATH = (DataDefinition) PRELUDE.getDefinition("Path");
    PATH_CON = (Constructor) PRELUDE.getDefinition("path");
    PATH_INFIX = (FunctionDefinition) PRELUDE.getDefinition("=");
    AT = (FunctionDefinition) PRELUDE.getDefinition("@");
    ISO = (FunctionDefinition) PRELUDE.getDefinition("iso");
 }

  private Prelude() {
    super("Prelude");
  }

  @Override
  public Collection<NamespaceMember> getMembers() {
    throw new IllegalStateException();
  }

  public NamespaceMember getMember(String name) {
    NamespaceMember result = super.getMember(name);
    if (result != null)
      return result;
    for (char sc : specInfix) {
      if (name.charAt(0) == sc) {
        for (char c : name.toCharArray()) {
          if (sc != c)
            return null;
        }
        generateLevel(name.length() - 1);
        return getMember(name);
      }
    }
    for (String sname : specPrefix) {
      if (name.startsWith(sname)) {
        if (name.length() == sname.length()) {
          generateLevel(0);
          return getMember(name);
        }
        Integer level = Integer.getInteger(name.substring(sname.length()));
        if (level != null && level > 0) {
          generateLevel(level);
          return getMember(name);
        }
      }
    }
    return null;
  }

  private void generateLevel(int i) {
    String suffix = i == 0 ? "" : Integer.toString(i);
    List<TypeArgument> PathParameters = new ArrayList<>(3);
    PathParameters.add(Tele(vars("A"), Pi(DataCall(INTERVAL), Universe(i, Universe.Type.NOT_TRUNCATED))));
    PathParameters.add(TypeArg(Apps(Index(0), ConCall(LEFT))));
    PathParameters.add(TypeArg(Apps(Index(1), ConCall(RIGHT))));
    DataDefinition path = new DataDefinition(PRELUDE, new Name("Path" + suffix), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(i, Universe.Type.NOT_TRUNCATED), PathParameters);
    PRELUDE.addDefinition(path);
    List<TypeArgument> pathArguments = new ArrayList<>(1);
    pathArguments.add(TypeArg(Pi("i", DataCall(INTERVAL), Apps(Index(3), Index(0)))));
    Constructor pathCon = new Constructor(PRELUDE.getChild(path.getName()), new Name("path" + suffix), Abstract.Definition.DEFAULT_PRECEDENCE, new Universe.Type(i, Universe.Type.NOT_TRUNCATED), pathArguments, path);
    path.addConstructor(pathCon);
    PRELUDE.addDefinition(pathCon);

    char[] chars = new char[i + 1];

    List<Argument> pathInfixArguments = new ArrayList<>(3);
    pathInfixArguments.add(Tele(false, vars("A"), Universe(i, Universe.Type.NOT_TRUNCATED)));
    pathInfixArguments.add(Tele(vars("a", "a'"), Index(0)));
    Expression pathInfixTerm = Apps(DataCall((DataDefinition) PRELUDE.getDefinition("Path" + suffix)), Lam(lamArgs(Tele(vars("_"), DataCall(INTERVAL))), Index(3)), Index(1), Index(0));
    Arrays.fill(chars, '=');
    FunctionDefinition pathInfix = new FunctionDefinition(PRELUDE, new Name(new String(chars), Abstract.Definition.Fixity.INFIX), new Abstract.Definition.Precedence(Abstract.Definition.Associativity.NON_ASSOC, (byte) 0), pathInfixArguments, Universe(i), leaf(pathInfixTerm));

    PRELUDE.addDefinition(pathInfix);

    List<Argument> atArguments = new ArrayList<>(5);
    atArguments.add(Tele(false, vars("A"), PathParameters.get(0).getType()));
    atArguments.add(Tele(false, vars("a"), PathParameters.get(1).getType()));
    atArguments.add(Tele(false, vars("a'"), PathParameters.get(2).getType()));
    atArguments.add(Tele(vars("p"), Apps(DataCall((DataDefinition) PRELUDE.getDefinition("Path" + suffix)), Index(2), Index(1), Index(0))));
    atArguments.add(Tele(vars("i"), DataCall(INTERVAL)));
    Expression atResultType = Apps(Index(4), Index(0));
    BranchElimTreeNode atElimTree = branch(0,
      clause(LEFT, Index(2)),
      clause(RIGHT, Index(1)),
      clause(null, branch(1, clause((Constructor) PRELUDE.getDefinition("path" + suffix), Apps(Index(1), Index(0)))))
    );
    Arrays.fill(chars, '@');
    FunctionDefinition at = new FunctionDefinition(PRELUDE, new Name(new String(chars), Abstract.Definition.Fixity.INFIX), new Abstract.Definition.Precedence(Abstract.Definition.Associativity.LEFT_ASSOC, (byte) 9), atArguments, atResultType, atElimTree);

    PRELUDE.addDefinition(at);

    List<Argument> isoArguments = new ArrayList<>(6);
    isoArguments.add(Tele(false, vars("A", "B"), Universe(i, Universe.Type.NOT_TRUNCATED)));
    isoArguments.add(Tele(vars("f"), Pi(Index(1), Index(0))));
    isoArguments.add(Tele(vars("g"), Pi(Index(1), Index(2))));
    isoArguments.add(Tele(vars("linv"), Pi(args(Tele(vars("a"), Index(3))), Apps(Apps(FunCall(pathInfix), new ArgumentExpression(Index(4), false, true)), Apps(Index(1), Apps(Index(2), Index(0)), Index(0))))));
    isoArguments.add(Tele(vars("rinv"), Pi(args(Tele(vars("b"), Index(3))), Apps(Apps(FunCall(pathInfix), new ArgumentExpression(Index(4), false, true)), Apps(Index(3), Apps(Index(2), Index(0)), Index(0))))));
    isoArguments.add(Tele(vars("i"), DataCall(INTERVAL)));
    Expression isoResultType = Universe(i, Universe.Type.NOT_TRUNCATED);
    BranchElimTreeNode isoElimTree = branch(0,
      clause(LEFT, Index(5)),
      clause(RIGHT, Index(4))
    );
    FunctionDefinition iso = new FunctionDefinition(PRELUDE, new Name("iso" + suffix), Abstract.Definition.DEFAULT_PRECEDENCE, isoArguments, isoResultType, isoElimTree);
    PRELUDE.addDefinition(iso);
  }

  public static boolean isAt(Abstract.Definition definition) {
    return isSpec(definition, "@");
  }

  public static boolean isPathCon(Abstract.Definition definition) {
    return isSpec(definition, "path");
  }

  public static boolean isPath(Abstract.Definition definition) {
    return isSpec(definition, "Path");
  }

  public static boolean isPathInfix(Abstract.Definition definition) {
    return isSpec(definition, "=");
  }

  public static boolean isIso(Abstract.Definition definition) {
    return isSpec(definition, "iso");
  }

  private static boolean isSpec(Abstract.Definition definition, String prefix) {
    return definition != null && definition == PRELUDE.getDefinition(definition.getName().name) && definition.getName().name.startsWith(prefix);
  }

  public static int getLevel(Abstract.Definition definition) {
    for (char c : specInfix) {
      if (isSpec(definition, Character.toString(c))) {
        return definition.getName().name.length() - 1;
      }
    }
    for (String name : specPrefix) {
      if (isSpec(definition, name)) {
        return definition.getName().name.length() == name.length() ? 0 : Integer.parseInt(definition.getName().name.substring(name.length()));
      }
    }

    throw new IllegalStateException();
  }
}
