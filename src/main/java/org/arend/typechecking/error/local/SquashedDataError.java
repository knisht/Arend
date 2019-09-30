package org.arend.typechecking.error.local;

import org.arend.core.definition.DataDefinition;
import org.arend.core.sort.Level;
import org.arend.error.doc.LineDoc;
import org.arend.term.concrete.Concrete;
import org.arend.term.prettyprint.PrettyPrinterConfig;

import javax.annotation.Nonnull;

import static org.arend.error.doc.DocFactory.*;

public class SquashedDataError extends TypecheckingError {
  public final DataDefinition dataDef;
  private org.arend.core.sort.Level myLevel;
  private int myLevelSub;

  public SquashedDataError(DataDefinition dataDef, org.arend.core.sort.Level level, int levelSub, @Nonnull Concrete.SourceNode cause) {
    super("", cause);
    this.dataDef = dataDef;
    myLevel = level;
    myLevelSub = levelSub;
  }

  public org.arend.core.sort.Level getLevel() {
    if (myLevelSub == 0) {
      return myLevel;
    }
    if (myLevel.isInfinity()) {
      myLevelSub = 0;
      return myLevel;
    }

    if (myLevel.getConstant() >= myLevelSub) {
      myLevel = new org.arend.core.sort.Level(myLevel.getVar(), myLevel.getConstant() - myLevelSub, myLevel.getMaxConstant());
      myLevelSub = 0;
    }

    return myLevel;
  }

  @Override
  public LineDoc getShortHeaderDoc(PrettyPrinterConfig ppConfig) {
    return hList(text("Pattern matching on " + (dataDef.isTruncated() ? "truncated" : "squashed") + " data type '"), refDoc(dataDef.getReferable()), text("' is allowed only in \\sfunc and \\scase"));
  }
}
