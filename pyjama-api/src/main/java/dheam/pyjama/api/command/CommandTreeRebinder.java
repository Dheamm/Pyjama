package dheam.pyjama.api.command;

import java.util.List;

public interface CommandTreeRebinder {

    void rebind(String oldName, List<String> oldAliases, String newName, List<String> newAliases);
}