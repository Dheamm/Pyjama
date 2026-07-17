package dheam.pyjama.api.platform.command;

import java.util.List;

public interface CommandTreeRebinder {

    void rebind(String oldName, List<String> oldAliases, String newName, List<String> newAliases);
}