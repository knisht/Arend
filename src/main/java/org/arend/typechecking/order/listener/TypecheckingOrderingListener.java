package org.arend.typechecking.order.listener;

import org.arend.core.context.param.EmptyDependentLink;
import org.arend.core.context.param.TypedSingleDependentLink;
import org.arend.core.definition.*;
import org.arend.core.elimtree.Clause;
import org.arend.core.elimtree.LeafElimTree;
import org.arend.core.expr.ClassCallExpression;
import org.arend.core.expr.ErrorExpression;
import org.arend.core.expr.PiExpression;
import org.arend.core.sort.Sort;
import org.arend.error.CompositeErrorReporter;
import org.arend.error.CountingErrorReporter;
import org.arend.error.ErrorReporter;
import org.arend.library.Library;
import org.arend.naming.reference.GlobalReferable;
import org.arend.naming.reference.TCClassReferable;
import org.arend.naming.reference.TCReferable;
import org.arend.naming.reference.converter.IdReferableConverter;
import org.arend.term.concrete.Concrete;
import org.arend.term.group.Group;
import org.arend.typechecking.CancellationIndicator;
import org.arend.typechecking.DefinitionTypechecking;
import org.arend.typechecking.ThreadCancellationIndicator;
import org.arend.typechecking.TypecheckerState;
import org.arend.typechecking.error.CycleError;
import org.arend.typechecking.error.ProxyError;
import org.arend.typechecking.error.TerminationCheckError;
import org.arend.typechecking.error.local.ProxyErrorReporter;
import org.arend.typechecking.error.local.TypecheckingError;
import org.arend.typechecking.instance.pool.GlobalInstancePool;
import org.arend.typechecking.instance.provider.InstanceProviderSet;
import org.arend.typechecking.order.Ordering;
import org.arend.typechecking.order.PartialComparator;
import org.arend.typechecking.order.SCC;
import org.arend.typechecking.order.dependency.DependencyListener;
import org.arend.typechecking.order.dependency.DummyDependencyListener;
import org.arend.typechecking.termination.DefinitionCallGraph;
import org.arend.typechecking.termination.RecursiveBehavior;
import org.arend.typechecking.typecheckable.TypecheckingUnit;
import org.arend.typechecking.typecheckable.provider.ConcreteProvider;
import org.arend.typechecking.visitor.CheckTypeVisitor;
import org.arend.typechecking.visitor.DesugarVisitor;
import org.arend.typechecking.visitor.FindDefCallVisitor;
import org.arend.util.ComputationInterruptedException;
import org.arend.util.Pair;

import java.util.*;

public class TypecheckingOrderingListener implements OrderingListener {
  private final TypecheckerState myState;
  private final DependencyListener myDependencyListener;
  private final Map<GlobalReferable, Pair<CheckTypeVisitor,Boolean>> mySuspensions = new HashMap<>();
  private final ErrorReporter myErrorReporter;
  private final InstanceProviderSet myInstanceProviderSet;
  private final ConcreteProvider myConcreteProvider;
  private final PartialComparator<TCReferable> myComparator;
  private boolean myTypecheckingHeaders = false;

  public static CancellationIndicator CANCELLATION_INDICATOR = ThreadCancellationIndicator.INSTANCE;

  public static void setDefaultCancellationIndicator() {
    CANCELLATION_INDICATOR = ThreadCancellationIndicator.INSTANCE;
  }

  public TypecheckingOrderingListener(InstanceProviderSet instanceProviderSet, TypecheckerState state, ConcreteProvider concreteProvider, ErrorReporter errorReporter, DependencyListener dependencyListener, PartialComparator<TCReferable> comparator) {
    myState = state;
    myErrorReporter = errorReporter;
    myDependencyListener = dependencyListener;
    myInstanceProviderSet = instanceProviderSet;
    myConcreteProvider = concreteProvider;
    myComparator = comparator;
  }

  public TypecheckingOrderingListener(InstanceProviderSet instanceProviderSet, TypecheckerState state, ConcreteProvider concreteProvider, ErrorReporter errorReporter, PartialComparator<TCReferable> comparator) {
    this(instanceProviderSet, state, concreteProvider, errorReporter, DummyDependencyListener.INSTANCE, comparator);
  }

  public TypecheckingOrderingListener(Ordering ordering, ErrorReporter errorReporter) {
    myState = ordering.getTypecheckerState();
    myErrorReporter = errorReporter;
    myDependencyListener = ordering.getDependencyListener();
    myInstanceProviderSet = ordering.getInstanceProviderSet();
    myConcreteProvider = ordering.getConcreteProvider();
    myComparator = ordering.getComparator();
  }

  public boolean typecheckDefinitions(final Collection<? extends Concrete.Definition> definitions) {
    try {
      Ordering ordering = new Ordering(myInstanceProviderSet, myConcreteProvider, this, myDependencyListener, IdReferableConverter.INSTANCE, myState, myComparator, false);
      for (Concrete.Definition definition : definitions) {
        ordering.orderDefinition(definition);
      }
      return true;
    } catch (ComputationInterruptedException ignored) {
      return false;
    }
  }

  public boolean typecheckModules(final Collection<? extends Group> modules) {
    try {
      new Ordering(myInstanceProviderSet, myConcreteProvider, this, myDependencyListener, IdReferableConverter.INSTANCE, myState, myComparator, false).orderModules(modules);
      return true;
    } catch (ComputationInterruptedException ignored) {
      return false;
    }
  }

  public boolean typecheckLibrary(Library library) {
    try {
      return library.orderModules(new Ordering(myInstanceProviderSet, myConcreteProvider, this, myDependencyListener, IdReferableConverter.INSTANCE, myState, myComparator, false));
    } catch (ComputationInterruptedException ignored) {
      return false;
    }
  }

  public boolean typecheckCollected(CollectingOrderingListener collector) {
    try {
      collector.feed(this);
      return true;
    } catch (ComputationInterruptedException ignored) {
      return false;
    }
  }

  public void typecheckingHeaderStarted(TCReferable definition) {

  }

  public void typecheckingBodyStarted(TCReferable definition) {

  }

  public void typecheckingUnitStarted(TCReferable definition) {

  }

  public void typecheckingHeaderFinished(TCReferable referable, Definition definition) {

  }

  public void typecheckingBodyFinished(TCReferable referable, Definition definition) {

  }

  public void typecheckingUnitFinished(TCReferable referable, Definition definition) {

  }

  private Definition newDefinition(Concrete.Definition definition) {
    Definition typechecked;
    if (definition instanceof Concrete.DataDefinition) {
      typechecked = new DataDefinition(definition.getData());
      ((DataDefinition) typechecked).setSort(Sort.SET0);
      typechecked.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
      for (Concrete.ConstructorClause constructorClause : ((Concrete.DataDefinition) definition).getConstructorClauses()) {
        for (Concrete.Constructor constructor : constructorClause.getConstructors()) {
          Constructor tcConstructor = new Constructor(constructor.getData(), (DataDefinition) typechecked);
          tcConstructor.setParameters(EmptyDependentLink.getInstance());
          tcConstructor.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
          ((DataDefinition) typechecked).addConstructor(tcConstructor);
          myState.record(constructor.getData(), tcConstructor);
        }
      }
    } else if (definition instanceof Concrete.FunctionDefinition || definition instanceof Concrete.Instance) {
      typechecked = new FunctionDefinition(definition.getData());
      ((FunctionDefinition) typechecked).setResultType(new ErrorExpression(null, null));
      typechecked.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
    } else if (definition instanceof Concrete.ClassDefinition) {
      typechecked = new ClassDefinition((TCClassReferable) definition.getData());
      typechecked.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
      for (Concrete.ClassField field : ((Concrete.ClassDefinition) definition).getFields()) {
        ClassField classField = new ClassField(field.getData(), (ClassDefinition) typechecked);
        classField.setType(new PiExpression(Sort.PROP, new TypedSingleDependentLink(false, "this", new ClassCallExpression((ClassDefinition) typechecked, Sort.STD)), new ErrorExpression(null, null)));
        classField.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
        ((ClassDefinition) typechecked).addPersonalField(classField);
        myState.record(classField.getReferable(), classField);
      }
    } else {
      throw new IllegalStateException();
    }
    typechecked.setStatus(Definition.TypeCheckingStatus.HEADER_HAS_ERRORS);
    myState.record(definition.getData(), typechecked);
    return typechecked;
  }

  @Override
  public void sccFound(SCC scc) {
    for (TypecheckingUnit unit : scc.getUnits()) {
      if (!TypecheckingUnit.hasHeader(unit.getDefinition())) {
        List<TCReferable> cycle = new ArrayList<>();
        for (TypecheckingUnit unit1 : scc.getUnits()) {
          Concrete.Definition definition = unit1.getDefinition();
          if (cycle.isEmpty() || cycle.get(cycle.size() - 1) != definition.getData()) {
            cycle.add(definition.getData());
          }

          Definition typechecked = myState.getTypechecked(definition.getData());
          if (typechecked == null) {
            typechecked = newDefinition(definition);
          }
          if (typechecked.status() == Definition.TypeCheckingStatus.NO_ERRORS) {
            typechecked.setStatus(Definition.TypeCheckingStatus.HAS_ERRORS);
          }

          if (!unit1.isHeader()) {
            typecheckingUnitStarted(definition.getData());
            if (TypecheckingUnit.hasHeader(definition)) {
              mySuspensions.remove(definition.getData());
            }
            typecheckingUnitFinished(definition.getData(), typechecked);
          }
        }
        myErrorReporter.report(new CycleError(cycle));
        return;
      }
    }

    boolean ok = typecheckHeaders(scc);
    List<Concrete.Definition> definitions = new ArrayList<>(scc.getUnits().size());
    for (TypecheckingUnit unit : scc.getUnits()) {
      if (!unit.isHeader()) {
        definitions.add(unit.getDefinition());
      }
    }
    if (!definitions.isEmpty()) {
      typecheckBodies(definitions, ok);
    }
  }

  @Override
  public void unitFound(TypecheckingUnit unit, Recursion recursion) {
    if (recursion == Recursion.IN_HEADER) {
      typecheckingUnitStarted(unit.getDefinition().getData());
      myErrorReporter.report(new CycleError(Collections.singletonList(unit.getDefinition().getData())));
      typecheckingUnitFinished(unit.getDefinition().getData(), newDefinition(unit.getDefinition()));
    } else {
      typecheck(unit, recursion == Recursion.IN_BODY);
    }
  }

  private boolean typecheckHeaders(SCC scc) {
    int numberOfHeaders = 0;
    TypecheckingUnit unit = null;
    for (TypecheckingUnit unit1 : scc.getUnits()) {
      if (unit1.isHeader()) {
        unit = unit1;
        numberOfHeaders++;
      }
    }

    if (numberOfHeaders == 0) {
      return true;
    }

    if (numberOfHeaders == 1) {
      typecheckingHeaderStarted(unit.getDefinition().getData());

      CountingErrorReporter countingErrorReporter = new CountingErrorReporter();
      CheckTypeVisitor visitor = new CheckTypeVisitor(myState, new LinkedHashMap<>(), new ProxyErrorReporter(unit.getDefinition().getData(), new CompositeErrorReporter(myErrorReporter, countingErrorReporter)), null);
      if (unit.getDefinition().hasErrors()) {
        visitor.setHasErrors();
      }
      DesugarVisitor.desugar(unit.getDefinition(), myConcreteProvider, visitor.getErrorReporter());
      Definition oldTypechecked = visitor.getTypecheckingState().getTypechecked(unit.getDefinition().getData());
      Definition typechecked = new DefinitionTypechecking(visitor).typecheckHeader(oldTypechecked, new GlobalInstancePool(myState, myInstanceProviderSet.get(unit.getDefinition().getData()), visitor), unit.getDefinition());
      if (typechecked.status() == Definition.TypeCheckingStatus.BODY_NEEDS_TYPE_CHECKING) {
        mySuspensions.put(unit.getDefinition().getData(), new Pair<>(visitor, oldTypechecked == null));
      }

      typecheckingHeaderFinished(unit.getDefinition().getData(), typechecked);
      return typechecked.status().headerIsOK();
    }

    if (myTypecheckingHeaders) {
      List<Concrete.Definition> cycle = new ArrayList<>(scc.getUnits().size());
      for (TypecheckingUnit unit1 : scc.getUnits()) {
        cycle.add(unit1.getDefinition());
      }

      myErrorReporter.report(CycleError.fromConcrete(cycle));
      for (Concrete.Definition definition : cycle) {
        typecheckingHeaderStarted(definition.getData());
        typecheckingHeaderFinished(definition.getData(), newDefinition(definition));
      }
      return false;
    }

    myTypecheckingHeaders = true;
    Ordering ordering = new Ordering(myInstanceProviderSet, myConcreteProvider, this, myDependencyListener, IdReferableConverter.INSTANCE, myState, myComparator, true);
    boolean ok = true;
    for (TypecheckingUnit unit1 : scc.getUnits()) {
      if (unit1.isHeader()) {
        Concrete.Definition definition = unit1.getDefinition();
        ordering.orderDefinition(definition);
        if (ok && !myState.getTypechecked(definition.getData()).status().headerIsOK()) {
          ok = false;
        }
      }
    }
    myTypecheckingHeaders = false;
    return ok;
  }

  private void typecheckBodies(List<Concrete.Definition> definitions, boolean headersAreOK) {
    Map<FunctionDefinition,Concrete.Definition> functionDefinitions = new HashMap<>();
    Map<FunctionDefinition, List<Clause>> clausesMap = new HashMap<>();
    Set<DataDefinition> dataDefinitions = new HashSet<>();
    List<Concrete.Definition> orderedDefinitions = new ArrayList<>(definitions.size());
    List<Concrete.Definition> otherDefs = new ArrayList<>();
    for (Concrete.Definition definition : definitions) {
      Definition typechecked = myState.getTypechecked(definition.getData());
      if (typechecked instanceof DataDefinition) {
        dataDefinitions.add((DataDefinition) typechecked);
        orderedDefinitions.add(definition);
      } else {
        otherDefs.add(definition);
      }
    }
    orderedDefinitions.addAll(otherDefs);

    DefinitionTypechecking typechecking = new DefinitionTypechecking(null);
    for (Concrete.Definition definition : orderedDefinitions) {
      typecheckingBodyStarted(definition.getData());

      Definition def = myState.getTypechecked(definition.getData());
      Pair<CheckTypeVisitor, Boolean> pair = mySuspensions.remove(definition.getData());
      if (headersAreOK && pair != null) {
        typechecking.setVisitor(pair.proj1);
        List<Clause> clauses = typechecking.typecheckBody(def, definition, dataDefinitions, pair.proj2);
        if (clauses != null) {
          functionDefinitions.put((FunctionDefinition) def, definition);
          clausesMap.put((FunctionDefinition) def, clauses);
        }
      }

      typecheckingBodyFinished(definition.getData(), def);
    }

    if (!functionDefinitions.isEmpty()) {
      FindDefCallVisitor visitor = new FindDefCallVisitor(dataDefinitions);
      Iterator<Map.Entry<FunctionDefinition, Concrete.Definition>> it = functionDefinitions.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<FunctionDefinition, Concrete.Definition> entry = it.next();
        visitor.findDefinition(entry.getKey().getBody());
        if (visitor.getFoundDefinition() != null) {
          entry.getKey().setBody(null);
          if (entry.getKey().status().headerIsOK()) {
            entry.getKey().setStatus(Definition.TypeCheckingStatus.BODY_HAS_ERRORS);
          }
          myErrorReporter.report(new ProxyError(entry.getKey().getReferable(), new TypecheckingError("Mutually recursive function refers to data type '" + visitor.getFoundDefinition().getName() + "'", entry.getValue())));
          it.remove();
          visitor.clear();
        }
      }

      if (!functionDefinitions.isEmpty()) {
        checkRecursiveFunctions(functionDefinitions, clausesMap);
      }
    }
  }

  private void typecheck(TypecheckingUnit unit, boolean recursive) {
    typecheckingUnitStarted(unit.getDefinition().getData());

    CheckTypeVisitor checkTypeVisitor = new CheckTypeVisitor(myState, new LinkedHashMap<>(), new ProxyErrorReporter(unit.getDefinition().getData(), myErrorReporter), null);
    DesugarVisitor.desugar(unit.getDefinition(), myConcreteProvider, checkTypeVisitor.getErrorReporter());
    GlobalInstancePool pool = new GlobalInstancePool(myState, myInstanceProviderSet.get(unit.getDefinition().getData()), checkTypeVisitor);
    checkTypeVisitor.setInstancePool(pool);
    List<Clause> clauses = unit.getDefinition().accept(new DefinitionTypechecking(checkTypeVisitor), recursive);
    Definition typechecked = myState.getTypechecked(unit.getDefinition().getData());

    if (recursive && typechecked instanceof FunctionDefinition) {
      checkRecursiveFunctions(Collections.singletonMap((FunctionDefinition) typechecked, unit.getDefinition()), Collections.singletonMap((FunctionDefinition) typechecked, clauses));
    }

    typecheckingUnitFinished(unit.getDefinition().getData(), typechecked);
  }

  private void checkRecursiveFunctions(Map<FunctionDefinition,Concrete.Definition> definitions, Map<FunctionDefinition,List<Clause>> clauses) {
    DefinitionCallGraph definitionCallGraph = new DefinitionCallGraph();
    for (Map.Entry<FunctionDefinition, Concrete.Definition> entry : definitions.entrySet()) {
      List<Clause> functionClauses = clauses.get(entry.getKey());
      if (functionClauses != null) {
        definitionCallGraph.add(entry.getKey(), functionClauses, definitions.keySet());
      }
    }
    DefinitionCallGraph callCategory = new DefinitionCallGraph(definitionCallGraph);
    if (!callCategory.checkTermination()) {
      for (FunctionDefinition definition : definitions.keySet()) {
        definition.setStatus(Definition.TypeCheckingStatus.BODY_HAS_ERRORS);
      }
      for (Map.Entry<Definition, Set<RecursiveBehavior<Definition>>> entry : callCategory.myErrorInfo.entrySet()) {
        myErrorReporter.report(new TerminationCheckError(entry.getKey(), Collections.singleton(entry.getKey()), entry.getValue()));
      }
    }
  }
}
