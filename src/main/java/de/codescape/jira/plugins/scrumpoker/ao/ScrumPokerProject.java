package de.codescape.jira.plugins.scrumpoker.ao;

import net.java.ao.Preload;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;

/**
 * Active Object to persist project specific settings into the database.
 */
@Preload
public interface ScrumPokerProject extends ScrumPokerEntity {

    /**
     * Id of the Jira project that is configured here.
     */
    @NotNull
    @Indexed
    Long getProjectId();

    void setProjectId(Long projectId);

    /**
     * Flag whether Scrum Poker is enabled for this project or not.
     */
    boolean isScrumPokerEnabled();

    void setScrumPokerEnabled(boolean scrumPokerEnabled);

}
