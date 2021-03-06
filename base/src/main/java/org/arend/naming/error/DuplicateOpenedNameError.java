package org.arend.naming.error;

import org.arend.ext.error.NameResolverError;
import org.arend.ext.module.LongName;
import org.arend.ext.module.ModulePath;
import org.arend.ext.prettyprinting.PrettyPrinterConfig;
import org.arend.ext.prettyprinting.doc.LineDoc;
import org.arend.naming.reference.ModuleReferable;
import org.arend.naming.reference.Referable;
import org.arend.term.NamespaceCommand;

import static org.arend.ext.prettyprinting.doc.DocFactory.*;

public class DuplicateOpenedNameError extends NameResolverError {
  public final Referable referable;
  public final NamespaceCommand previousNamespaceCommand;

  public DuplicateOpenedNameError(Referable referable, NamespaceCommand previous, Object cause) {
    super(Level.WARNING, "", cause);
    this.referable = referable;
    previousNamespaceCommand = previous;
  }

  @Override
  public LineDoc getShortHeaderDoc(PrettyPrinterConfig ppConfig) {
    return hList(text("Definition '"), refDoc(referable), text("' is already imported from module "), previousNamespaceCommand.getKind() == NamespaceCommand.Kind.IMPORT ? refDoc(new ModuleReferable(new ModulePath(previousNamespaceCommand.getPath()))) : text(new LongName(previousNamespaceCommand.getPath()).toString()));
  }
}
