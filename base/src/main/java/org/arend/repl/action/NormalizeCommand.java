package org.arend.repl.action;

import org.arend.ext.core.ops.NormalizationMode;
import org.arend.repl.Repl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class NormalizeCommand implements ReplCommand {
  public static final @NotNull NormalizeCommand INSTANCE = new NormalizeCommand();

  private NormalizeCommand() {
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String description() {
    return "Modify the normalization level of printed expressions";
  }

  @Override
  public @Nls @NotNull String help(@NotNull Repl api) {
    return "Modify the normalization level of printed expressions (currently " + api.normalizationMode + ").\n" +
        "Available options (case insensitive) are:\n" +
        " NULL (do not normalize)\n" +
        " WHNF (Weak Head Normal Form)\n" +
        " NF (Normal Form)\n" +
        " RNF (the level best for pretty printing)." +
        "If you did not pass a command argument, NULL will be chosen.";
  }

  @Override
  public void invoke(@NotNull String line, @NotNull Repl api, @NotNull Supplier<@NotNull String> scanner) {
    loadNormalize(line, api, true);
  }

  public void loadNormalize(@NotNull String line, @NotNull Repl api, boolean log) {
    switch (line.toUpperCase()) {
      default:
        api.eprintln("[ERROR] Unrecognized normalization level `" + line + "`, see `:? normalize`");
        break;
      case "":
        if (log) {
          api.println("Normalization mode: " + api.normalizationMode + " (:? normalize for further instruction)");
          break;
        }
      case "NULL":
        if (log) api.println("[INFO] Unset normalization mode.");
        api.normalizationMode = null;
        break;
      case "WHNF":
        api.normalizationMode = NormalizationMode.WHNF;
        break;
      case "NF":
        api.normalizationMode = NormalizationMode.NF;
        break;
      case "RNF":
        api.normalizationMode = NormalizationMode.RNF;
        break;
    }
  }
}
